"""
Script de training pour le modèle ML de prédiction de maladies
"""
import os
import sys

import joblib
import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import accuracy_score
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder, MultiLabelBinarizer, StandardScaler

# Ajout du répertoire parent au path pour les imports
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from config.model_config import ModelConfig
from training.profile_generator import ProfileGenerator
from utils.text_utils import TextUtils


class ModelTrainer:
    """
    Classe pour entraîner le modèle ML de prédiction
    """

    def __init__(self, config: ModelConfig = None):
        """
        Initialise le trainer
        Args:
            config: Configuration des modèles (par défaut: nouvelle instance)
        """
        if config is None:
            base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
            config = ModelConfig(base_dir=base_dir)

        self.config = config
        self.text_utils = TextUtils()
        self.profile_generator = ProfileGenerator()

        self.dataset_path = os.path.join(config.DATA_DIR, 'dataset.csv')
        self.mapping_path = os.path.join(config.DATA_DIR, 'Doctor_Versus_Disease.csv')

    def train(self):
        """
        Entraîne le modèle ML complet
        """
        print("1. Chargement du dataset...")
        df = pd.read_csv(self.dataset_path)

        df.columns = df.columns.str.strip()

        symptom_cols = [c for c in df.columns if 'Symptom' in c]
        print(f"   - {len(symptom_cols)} colonnes de symptômes trouvées")

        print("\n2. Traitement des symptômes...")
        symptom_lists = []
        for _, row in df.iterrows():
            cleaned = [
                self.text_utils.clean_text(row[col])
                for col in symptom_cols
                if pd.notna(row[col])
            ]
            symptom_lists.append([s for s in cleaned if s])

        mlb = MultiLabelBinarizer()
        X_symptoms = mlb.fit_transform(symptom_lists)
        df_symptoms = pd.DataFrame(X_symptoms, columns=mlb.classes_)
        print(f"   - {len(mlb.classes_)} symptômes uniques encodés")

        print("\n3. Génération de profils patients synthétiques...")
        profiles = [
            self.profile_generator.generate(row['Disease'])
            for _, row in df.iterrows()
        ]
        df_profiles = pd.DataFrame(profiles)

        print("\n4. Mapping Maladie -> Spécialiste...")
        df_map = pd.read_csv(
            self.mapping_path,
            header=None,
            names=['Disease', 'Specialist'],
            encoding='cp1252'
        )
        df_map['Disease_clean'] = df_map['Disease'].apply(self.text_utils.clean_text)
        mapping_dict = dict(zip(df_map['Disease_clean'], df_map['Specialist']))

        df['Disease_clean'] = df['Disease'].apply(self.text_utils.clean_text)
        df['Target_Specialist'] = df['Disease_clean'].map(mapping_dict)

        df = df[df['Target_Specialist'].notna()]
        print(f"   - {len(df)} échantillons appariés avec des spécialistes")

        print("\n5. Combinaison des features...")
        numerical_cols = ['Age', 'Weight', 'BMI', 'Tension_Moyenne', 'Cholesterole_Moyen']
        scaler = StandardScaler()
        df_profiles_subset = df_profiles.iloc[df.index].reset_index(drop=True)

        numerical_normalized = scaler.fit_transform(df_profiles_subset[numerical_cols])
        df_numerical = pd.DataFrame(
            numerical_normalized,
            columns=[f'{col}_normalized' for col in numerical_cols]
        )

        categorical_cols = [
            'Gender', 'Blood Pressure', 'Cholesterol Level', 'Outcome Variable',
            'Smoking', 'Alcohol', 'Sedentarite', 'Family_History'
        ]
        df_categorical = pd.get_dummies(
            df_profiles_subset[categorical_cols],
            prefix=categorical_cols
        )

        df_symptoms_subset = df_symptoms.iloc[df.index].reset_index(drop=True)
        df_features = pd.concat([df_symptoms_subset, df_numerical, df_categorical], axis=1)

        le_disease = LabelEncoder()
        le_specialist = LabelEncoder()

        y_disease = le_disease.fit_transform(df['Disease'])
        y_specialist = le_specialist.fit_transform(df['Target_Specialist'])
        Y_combined = np.column_stack((y_disease, y_specialist))

        print(f"   - Forme finale des features: {df_features.shape}")

        print("\n6. Entraînement du modèle...")
        X_train, X_test, Y_train, Y_test = train_test_split(
            df_features, Y_combined, test_size=0.2, random_state=42
        )

        model = RandomForestClassifier(
            n_estimators=200,
            max_depth=15,
            min_samples_split=10,
            min_samples_leaf=5,
            random_state=42,
            n_jobs=-1,
        )
        model.fit(X_train, Y_train)

        Y_pred = model.predict(X_test)
        acc_disease = accuracy_score(Y_test[:, 0], Y_pred[:, 0])
        acc_specialist = accuracy_score(Y_test[:, 1], Y_pred[:, 1])
        print(f"   - Précision Maladie (hold-out): {acc_disease*100:.2f}%")
        print(f"   - Précision Spécialiste (hold-out): {acc_specialist*100:.2f}%")

        print("\n7. Sauvegarde des artefacts...")
        os.makedirs(self.config.MODELS_DIR, exist_ok=True)

        joblib.dump(model, self.config.get_model_path('model'))
        joblib.dump(mlb, self.config.get_model_path('mlb'))
        joblib.dump(scaler, self.config.get_model_path('scaler'))
        joblib.dump(le_disease, self.config.get_model_path('le_disease'))
        joblib.dump(le_specialist, self.config.get_model_path('le_specialist'))

        feature_columns = df_features.columns.tolist()
        joblib.dump(feature_columns, self.config.get_model_path('feature_columns'))

        print(f"   - Tous les artefacts sauvegardés dans {self.config.MODELS_DIR}/")


def main():
    """Point d'entrée principal pour le script de training"""
    trainer = ModelTrainer()
    trainer.train()


if __name__ == "__main__":
    main()
