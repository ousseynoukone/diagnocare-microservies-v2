"""
Chargement des donnees et construction des features pour l'entrainement.

Pipeline :
  1. Lire le CSV symptomes/maladies
  2. Nettoyer les libelles de symptomes
  3. Data augmentation (light + heavy drop de symptomes)
  4. Encoder les symptomes via TF-IDF (poids les symptomes rares plus fort)
  5. Generer des profils patients synthetiques
  6. Mapper maladie -> specialiste (avec correction des doublons/typos)
  7. Creer des feature interactions (profil x symptomes cles)
  8. Construire la matrice de features finale
  9. Encoder les cibles (maladie + specialiste)
"""
import os
import sys
import pandas as pd
import numpy as np
from sklearn.preprocessing import MultiLabelBinarizer, LabelEncoder, StandardScaler
from sklearn.feature_extraction.text import TfidfVectorizer
from typing import Tuple, Any

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from config.model_config import ModelConfig
from utils.text_utils import TextUtils
from data_cleaning import clean_specialist_label
from data_augmentation import augment_symptom_lists


# Symptomes "cles" : ceux qui, croises avec le profil, sont medicalement pertinents
KEY_SYMPTOMS_FOR_INTERACTIONS = [
    'chest_pain', 'breathlessness', 'high_fever', 'fatigue',
    'weight_loss', 'weight_gain', 'headache', 'joint_pain',
    'skin_rash', 'vomiting', 'dizziness', 'sweating',
]


def prepare_data(
    config: ModelConfig,
    text_utils: TextUtils,
    profile_generator: Any,
) -> Tuple[pd.DataFrame, np.ndarray, Any, StandardScaler, LabelEncoder, LabelEncoder, pd.DataFrame, list]:
    dataset_path = os.path.join(config.DATA_DIR, 'dataset.csv')
    mapping_path = os.path.join(config.DATA_DIR, 'Doctor_Versus_Disease.csv')

    # ---- 1. Chargement ----
    print("1. Chargement du dataset...")
    df = pd.read_csv(dataset_path)
    df.columns = df.columns.str.strip()
    symptom_cols = [c for c in df.columns if 'Symptom' in c]
    print(f"   - {len(symptom_cols)} colonnes de symptomes trouvees")

    # ---- 2. Nettoyage des symptomes ----
    print("\n2. Nettoyage des libelles de symptomes...")
    symptom_lists = []
    for _, row in df.iterrows():
        cleaned = [
            text_utils.clean_text(str(row[col]).strip())
            for col in symptom_cols
            if pd.notna(row[col]) and str(row[col]).strip()
        ]
        symptom_lists.append([s for s in cleaned if s])

    original_count = len(symptom_lists)

    # ---- 3. Data augmentation (light + heavy drop) ----
    print("\n3. Data augmentation (simulation de saisies incompletes)...")
    symptom_lists, diseases_series = augment_symptom_lists(
        symptom_lists,
        df['Disease'],
        n_light_copies=2,
        max_light_drop=2,
        n_heavy_copies=1,
        keep_min=2,
        keep_max=3,
        random_seed=42,
    )
    augmented_count = len(symptom_lists) - original_count
    print(f"   - {original_count} originaux + {augmented_count} augmentes = {len(symptom_lists)} total")

    df_aug = pd.DataFrame({'Disease': diseases_series})

    # ---- 4. TF-IDF sur les symptomes ----
    # On joint les symptomes en "phrase" pour TfidfVectorizer.
    # TF-IDF reduit le poids de "fatigue" (present partout) et booste
    # "silver_like_dusting" (quasi-exclusif au Psoriasis).
    print("\n4. Encodage TF-IDF des symptomes...")
    symptom_docs = [' '.join(s) for s in symptom_lists]
    tfidf = TfidfVectorizer(
        analyzer='word',
        token_pattern=r'[a-z_]+',
        use_idf=True,
        norm='l2',
        sublinear_tf=True,
    )
    X_symptoms = tfidf.fit_transform(symptom_docs)
    symptom_feature_names = tfidf.get_feature_names_out().tolist()
    df_symptoms = pd.DataFrame(X_symptoms.toarray(), columns=symptom_feature_names)
    print(f"   - {len(symptom_feature_names)} symptomes encodes en TF-IDF")

    # On garde aussi le MLB pour que le service sache quels symptomes sont valides
    mlb = MultiLabelBinarizer()
    mlb.fit(symptom_lists)

    # ---- 5. Profils patients synthetiques ----
    print("\n5. Generation de profils patients synthetiques...")
    profiles = [profile_generator.generate(disease) for disease in df_aug['Disease']]
    df_profiles = pd.DataFrame(profiles)

    # ---- 6. Mapping maladie -> specialiste ----
    print("\n6. Mapping Maladie -> Specialiste (nettoyage des labels)...")
    df_map = pd.read_csv(mapping_path, header=None, names=['Disease', 'Specialist'], encoding='cp1252')
    df_map['Disease_clean'] = df_map['Disease'].apply(text_utils.clean_text)
    df_map['Specialist'] = df_map['Specialist'].apply(clean_specialist_label)
    mapping_dict = dict(zip(df_map['Disease_clean'], df_map['Specialist']))

    unique_specialists = sorted(set(df_map['Specialist']))
    print(f"   - {len(unique_specialists)} specialistes uniques apres nettoyage: {unique_specialists}")

    df_aug['Disease_clean'] = df_aug['Disease'].apply(text_utils.clean_text)
    df_aug['Target_Specialist'] = df_aug['Disease_clean'].map(mapping_dict)
    df_aug = df_aug[df_aug['Target_Specialist'].notna()].copy()
    print(f"   - {len(df_aug)} echantillons apparies avec des specialistes")

    valid_idx = df_aug.index
    df_symptoms = df_symptoms.iloc[valid_idx].reset_index(drop=True)
    df_profiles = df_profiles.iloc[valid_idx].reset_index(drop=True)
    df_aug = df_aug.reset_index(drop=True)

    # ---- 7. Feature interactions (profil x symptomes cles) ----
    # "chest_pain chez un homme de 60 ans fumeur" != "chest_pain chez une femme de 20 ans"
    print("\n7. Creation des feature interactions (profil x symptomes)...")
    df_interactions = _build_interactions(df_symptoms, df_profiles, symptom_feature_names)
    print(f"   - {df_interactions.shape[1]} features d'interaction creees")

    # ---- 8. Construction des features ----
    print("\n8. Construction de la matrice de features...")

    numerical_cols = ['Age', 'Weight', 'BMI', 'Tension_Moyenne', 'Cholesterole_Moyen']
    scaler = StandardScaler()
    numerical_normalized = scaler.fit_transform(df_profiles[numerical_cols])
    df_numerical = pd.DataFrame(
        numerical_normalized,
        columns=[f'{col}_normalized' for col in numerical_cols]
    )

    categorical_cols = [
        'Gender', 'Blood Pressure', 'Cholesterol Level', 'Outcome Variable',
        'Smoking', 'Alcohol', 'Sedentarite', 'Family_History'
    ]
    df_categorical = pd.get_dummies(df_profiles[categorical_cols], prefix=categorical_cols)

    df_features = pd.concat([df_symptoms, df_numerical, df_categorical, df_interactions], axis=1)
    feature_columns = df_features.columns.tolist()
    print(f"   - Forme finale: {df_features.shape}")

    # ---- 9. Encodage des cibles ----
    le_disease = LabelEncoder()
    le_specialist = LabelEncoder()
    y_disease = le_disease.fit_transform(df_aug['Disease'])
    y_specialist = le_specialist.fit_transform(df_aug['Target_Specialist'])
    Y_combined = np.column_stack((y_disease, y_specialist))

    return (
        df_features,
        Y_combined,
        (mlb, tfidf),  # on retourne les deux : mlb pour validation, tfidf pour encoding
        scaler,
        le_disease,
        le_specialist,
        df_aug,
        feature_columns,
    )


def _build_interactions(
    df_symptoms: pd.DataFrame,
    df_profiles: pd.DataFrame,
    symptom_feature_names: list,
) -> pd.DataFrame:
    """
    Croise les symptomes cles avec les variables de profil numeriques.
    Exemple : Age_normalized * chest_pain_tfidf
    -> un score eleve signifie "patient age AVEC chest_pain" (signal cardiaque fort)
    """
    interactions = {}

    age_norm = (df_profiles['Age'] - df_profiles['Age'].mean()) / (df_profiles['Age'].std() + 1e-8)
    is_male = (df_profiles['Gender'] == 'Male').astype(float)
    is_smoker = (df_profiles['Smoking'] == 'Yes').astype(float)
    bp_high = (df_profiles['Blood Pressure'] == 'High').astype(float)

    profile_signals = {
        'age': age_norm,
        'male': is_male,
        'smoker': is_smoker,
        'bp_high': bp_high,
    }

    for symptom in KEY_SYMPTOMS_FOR_INTERACTIONS:
        if symptom not in symptom_feature_names:
            continue
        s_values = df_symptoms[symptom].values
        for pname, pvalues in profile_signals.items():
            interactions[f'IX_{symptom}_x_{pname}'] = s_values * pvalues.values

    return pd.DataFrame(interactions)
