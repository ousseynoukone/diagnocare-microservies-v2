"""
Chargement des données et construction des features pour l'entraînement.

On lit le CSV des maladies/symptômes, on nettoie les libellés, on génère les profils
patients, on associe chaque maladie à un spécialiste, puis on assemble le tout
en une grosse matrice de features (symptômes binaires + numériques normalisés + one-hot).
"""
import os
import sys
import pandas as pd
import numpy as np
from sklearn.preprocessing import MultiLabelBinarizer, LabelEncoder, StandardScaler

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from config.model_config import ModelConfig
from utils.text_utils import TextUtils

# On va typer le résultat de prepare_data pour que ce soit clair
from typing import Tuple, Any

# Référence au type du générateur de profils (évite import circulaire au runtime)
def prepare_data(
    config: ModelConfig,
    text_utils: TextUtils,
    profile_generator: Any,
) -> Tuple[pd.DataFrame, np.ndarray, MultiLabelBinarizer, StandardScaler, LabelEncoder, LabelEncoder, pd.DataFrame, list]:
    """
    Charge le dataset, nettoie les symptômes, génère les profils, mappe maladie -> spécialiste,
    construit la matrice de features et les cibles encodées.

    Returns:
        df_features: matrice (n_samples, n_features)
        Y_combined: (n_samples, 2) = [disease_id, specialist_id]
        mlb: MultiLabelBinarizer pour les symptômes
        scaler: StandardScaler pour les numériques
        le_disease: LabelEncoder des maladies
        le_specialist: LabelEncoder des spécialistes
        df_filtered: dataframe filtré (une ligne par échantillon valide)
        feature_columns: liste des noms de colonnes de df_features (pour l’inférence)
    """
    dataset_path = os.path.join(config.DATA_DIR, 'dataset.csv')
    mapping_path = os.path.join(config.DATA_DIR, 'Doctor_Versus_Disease.csv')

    # --- 1. Chargement et colonnes symptômes ---
    print("1. Chargement du dataset...")
    df = pd.read_csv(dataset_path)
    df.columns = df.columns.str.strip()
    symptom_cols = [c for c in df.columns if 'Symptom' in c]
    print(f"   - {len(symptom_cols)} colonnes de symptômes trouvées")

    # --- 2. Symptômes : nettoyage puis encodage binaire (multi-label) ---
    symptom_lists = []
    for _, row in df.iterrows():
        cleaned = [
            text_utils.clean_text(str(row[col]).strip())
            for col in symptom_cols
            if pd.notna(row[col]) and str(row[col]).strip()
        ]
        symptom_lists.append([s for s in cleaned if s])

    print("\n2. Traitement des symptômes (nettoyage des libellés)...")
    mlb = MultiLabelBinarizer()
    X_symptoms = mlb.fit_transform(symptom_lists)
    df_symptoms = pd.DataFrame(X_symptoms, columns=mlb.classes_)
    print(f"   - {len(mlb.classes_)} symptômes uniques encodés")

    # --- 3. Un profil patient synthétique par ligne (selon la maladie) ---
    print("\n3. Génération de profils patients synthétiques...")
    profiles = [profile_generator.generate(row['Disease']) for _, row in df.iterrows()]
    df_profiles = pd.DataFrame(profiles)

    # --- 4. Mapping maladie -> spécialiste (fichier séparé) ---
    df_map = pd.read_csv(mapping_path, header=None, names=['Disease', 'Specialist'], encoding='cp1252')
    df_map['Disease_clean'] = df_map['Disease'].apply(text_utils.clean_text)
    mapping_dict = dict(zip(df_map['Disease_clean'], df_map['Specialist']))

    df['Disease_clean'] = df['Disease'].apply(text_utils.clean_text)
    print("\n4. Mapping Maladie -> Spécialiste...")
    df['Target_Specialist'] = df['Disease_clean'].map(mapping_dict)
    df = df[df['Target_Specialist'].notna()].copy()
    print(f"   - {len(df)} échantillons appariés avec des spécialistes")

    # Réindexer les profils et symptômes pour ne garder que les lignes conservées
    df_profiles_subset = df_profiles.iloc[df.index].reset_index(drop=True)
    df_symptoms_subset = df_symptoms.iloc[df.index].reset_index(drop=True)
    df = df.reset_index(drop=True)

    # --- 5. Features numériques : normalisation (même échelle pour le modèle) ---
    numerical_cols = ['Age', 'Weight', 'BMI', 'Tension_Moyenne', 'Cholesterole_Moyen']
    scaler = StandardScaler()
    numerical_normalized = scaler.fit_transform(df_profiles_subset[numerical_cols])
    df_numerical = pd.DataFrame(
        numerical_normalized,
        columns=[f'{col}_normalized' for col in numerical_cols]
    )

    # --- 6. Variables catégorielles en one-hot ---
    categorical_cols = [
        'Gender', 'Blood Pressure', 'Cholesterol Level', 'Outcome Variable',
        'Smoking', 'Alcohol', 'Sedentarite', 'Family_History'
    ]
    df_categorical = pd.get_dummies(df_profiles_subset[categorical_cols], prefix=categorical_cols)

    # --- 7. Tout coller ensemble : symptômes + numériques + catégorielles ---
    print("\n5. Combinaison des features...")
    df_features = pd.concat([df_symptoms_subset, df_numerical, df_categorical], axis=1)
    feature_columns = df_features.columns.tolist()
    print(f"   - Forme finale des features: {df_features.shape}")

    # --- 8. Cibles : maladie et spécialiste encodés en entiers ---
    le_disease = LabelEncoder()
    le_specialist = LabelEncoder()
    y_disease = le_disease.fit_transform(df['Disease'])
    y_specialist = le_specialist.fit_transform(df['Target_Specialist'])
    Y_combined = np.column_stack((y_disease, y_specialist))

    return (
        df_features,
        Y_combined,
        mlb,
        scaler,
        le_disease,
        le_specialist,
        df,
        feature_columns,
    )
