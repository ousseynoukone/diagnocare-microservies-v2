"""
Script de training pour le modèle ML de prédiction de maladies
"""
import pandas as pd
import numpy as np
import joblib
import os
import sys
from sklearn.preprocessing import MultiLabelBinarizer, LabelEncoder, StandardScaler
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import accuracy_score

# Ajout du répertoire parent au path pour les imports
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from config.model_config import ModelConfig
from utils.text_utils import TextUtils


class ProfileGenerator:
    """
    Générateur de profils patients synthétiques basés sur le type de maladie
    """
    
    def __init__(self, random_seed: int = 42):
        """
        Initialise le générateur
        Args:
            random_seed: Graine pour la reproductibilité
        """
        np.random.seed(random_seed)
        self.text_utils = TextUtils()
    
    def generate(self, disease_name: str) -> dict:
        """
        Génère un profil patient synthétique basé sur le type de maladie
        Args:
            disease_name: Nom de la maladie
        Returns:
            dict: Profil patient généré
        """
        disease_lower = disease_name.lower()
        
        # Âge selon le type de maladie
        if any(word in disease_lower for word in ['heart', 'cardiac', 'hypertension', 'myocardial']):
            age = int(np.random.normal(55, 10))
            age = max(40, min(80, age))
            bp = np.random.choice(['Low', 'Normal', 'High'], p=[0.1, 0.2, 0.7])
            chol = np.random.choice(['Low', 'Normal', 'High'], p=[0.1, 0.3, 0.6])
            smoking = np.random.choice(['No', 'Yes'], p=[0.3, 0.7])
            sedentarite = np.random.choice(['Low', 'Moderate', 'High'], p=[0.2, 0.3, 0.5])
        elif any(word in disease_lower for word in ['acne', 'chicken pox', 'rubella']):
            age = int(np.random.normal(20, 8))
            age = max(10, min(35, age))
            bp = np.random.choice(['Low', 'Normal', 'High'], p=[0.3, 0.5, 0.2])
            chol = np.random.choice(['Low', 'Normal', 'High'], p=[0.4, 0.5, 0.1])
            smoking = np.random.choice(['No', 'Yes'], p=[0.7, 0.3])
            sedentarite = np.random.choice(['Low', 'Moderate', 'High'], p=[0.5, 0.3, 0.2])
        elif any(word in disease_lower for word in ['diabetes', 'thyroid']):
            age = int(np.random.normal(45, 12))
            age = max(25, min(70, age))
            bp = np.random.choice(['Low', 'Normal', 'High'], p=[0.2, 0.4, 0.4])
            chol = np.random.choice(['Low', 'Normal', 'High'], p=[0.2, 0.4, 0.4])
            smoking = np.random.choice(['No', 'Yes'], p=[0.5, 0.5])
            sedentarite = np.random.choice(['Low', 'Moderate', 'High'], p=[0.2, 0.4, 0.4])
        else:
            age = int(np.random.normal(35, 15))
            age = max(18, min(75, age))
            bp = np.random.choice(['Low', 'Normal', 'High'], p=[0.25, 0.5, 0.25])
            chol = np.random.choice(['Low', 'Normal', 'High'], p=[0.25, 0.5, 0.25])
            smoking = np.random.choice(['No', 'Yes'], p=[0.6, 0.4])
            sedentarite = np.random.choice(['Low', 'Moderate', 'High'], p=[0.3, 0.4, 0.3])
        
        # Genre
        if 'urinary tract' in disease_lower or 'uti' in disease_lower:
            gender = np.random.choice(['Male', 'Female'], p=[0.3, 0.7])
        else:
            gender = np.random.choice(['Male', 'Female'], p=[0.5, 0.5])
        
        # Poids & BMI
        if gender == 'Male':
            base_weight = 75 + (age - 35) * 0.3
            avg_height = 175
        else:
            base_weight = 65 + (age - 35) * 0.2
            avg_height = 162
        
        if any(word in disease_lower for word in ['diabetes', 'heart', 'cardiac', 'hypertension']):
            weight = int(np.random.normal(base_weight + 10, 12))
        elif any(word in disease_lower for word in ['acne', 'chicken pox', 'rubella']):
            weight = int(np.random.normal(base_weight - 5, 8))
        else:
            weight = int(np.random.normal(base_weight, 10))
        
        weight = max(40, min(150, weight))
        bmi = weight / ((avg_height / 100) ** 2)
        
        # Tension Moyenne
        if bp == 'High':
            tension_moyenne = np.random.normal(145, 10)
        elif bp == 'Low':
            tension_moyenne = np.random.normal(100, 8)
        else:
            tension_moyenne = np.random.normal(120, 8)
        tension_moyenne = max(80, min(180, int(tension_moyenne)))
        
        # Cholestérol Moyen
        if chol == 'High':
            cholesterole_moyen = np.random.normal(240, 20)
        elif chol == 'Low':
            cholesterole_moyen = np.random.normal(150, 15)
        else:
            cholesterole_moyen = np.random.normal(190, 15)
        cholesterole_moyen = max(100, min(300, int(cholesterole_moyen)))
        
        # Alcool
        if any(word in disease_lower for word in ['heart', 'cardiac', 'liver']):
            alcohol = np.random.choice(['None', 'Moderate', 'Heavy'], p=[0.4, 0.4, 0.2])
        else:
            alcohol = np.random.choice(['None', 'Moderate', 'Heavy'], p=[0.5, 0.4, 0.1])
        
        # Antécédents familiaux
        if any(word in disease_lower for word in ['diabetes', 'heart', 'cardiac', 'cancer']):
            family_history = np.random.choice(['No', 'Yes'], p=[0.3, 0.7])
        else:
            family_history = np.random.choice(['No', 'Yes'], p=[0.6, 0.4])
        
        # Outcome
        if any(word in disease_lower for word in ['cancer', 'stroke', 'heart attack']):
            outcome = np.random.choice(['Negative', 'Positive'], p=[0.2, 0.8])
        else:
            outcome = np.random.choice(['Negative', 'Positive'], p=[0.5, 0.5])
        
        return {
            'Age': age,
            'Gender': gender,
            'Blood Pressure': bp,
            'Cholesterol Level': chol,
            'Outcome Variable': outcome,
            'Smoking': smoking,
            'Weight': weight,
            'BMI': round(bmi, 1),
            'Tension_Moyenne': tension_moyenne,
            'Cholesterole_Moyen': cholesterole_moyen,
            'Alcohol': alcohol,
            'Sedentarite': sedentarite,
            'Family_History': family_history
        }


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
        
        # Chemins des fichiers de données
        self.dataset_path = os.path.join(config.DATA_DIR, 'dataset.csv')
        self.mapping_path = os.path.join(config.DATA_DIR, 'Doctor_Versus_Disease.csv')
    
    def train(self):
        """
        Entraîne le modèle ML complet
        """
        print("1. Chargement du dataset...")
        df = pd.read_csv(self.dataset_path)
        
        # Correction des espaces dans les noms de colonnes
        df.columns = df.columns.str.strip()
        
        # Identification des colonnes de symptômes
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
        
        # Suppression des lignes sans mapping de spécialiste
        df = df[df['Target_Specialist'].notna()]
        print(f"   - {len(df)} échantillons appariés avec des spécialistes")
        
        print("\n5. Combinaison des features...")
        # Normalisation des features numériques
        numerical_cols = ['Age', 'Weight', 'BMI', 'Tension_Moyenne', 'Cholesterole_Moyen']
        scaler = StandardScaler()
        df_profiles_subset = df_profiles.iloc[df.index].reset_index(drop=True)
        
        numerical_normalized = scaler.fit_transform(df_profiles_subset[numerical_cols])
        df_numerical = pd.DataFrame(
            numerical_normalized, 
            columns=[f'{col}_normalized' for col in numerical_cols]
        )
        
        # Encodage one-hot des features catégorielles
        categorical_cols = [
            'Gender', 'Blood Pressure', 'Cholesterol Level', 'Outcome Variable',
            'Smoking', 'Alcohol', 'Sedentarite', 'Family_History'
        ]
        df_categorical = pd.get_dummies(
            df_profiles_subset[categorical_cols], 
            prefix=categorical_cols
        )
        
        # Combinaison
        df_symptoms_subset = df_symptoms.iloc[df.index].reset_index(drop=True)
        df_features = pd.concat([df_symptoms_subset, df_numerical, df_categorical], axis=1)
        
        # Targets
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
            random_state=42
        )
        model.fit(X_train, Y_train)
        
        # Évaluation rapide
        Y_pred = model.predict(X_test)
        acc_disease = accuracy_score(Y_test[:, 0], Y_pred[:, 0])
        acc_specialist = accuracy_score(Y_test[:, 1], Y_pred[:, 1])
        print(f"   - Précision Maladie: {acc_disease*100:.2f}%")
        print(f"   - Précision Spécialiste: {acc_specialist*100:.2f}%")
        
        print("\n7. Sauvegarde des artefacts...")
        os.makedirs(self.config.MODELS_DIR, exist_ok=True)
        
        joblib.dump(model, self.config.get_model_path('model'))
        joblib.dump(mlb, self.config.get_model_path('mlb'))
        joblib.dump(scaler, self.config.get_model_path('scaler'))
        joblib.dump(le_disease, self.config.get_model_path('le_disease'))
        joblib.dump(le_specialist, self.config.get_model_path('le_specialist'))
        
        # Sauvegarde des noms de colonnes pour assurer l'ordre cohérent lors de l'inférence
        feature_columns = df_features.columns.tolist()
        joblib.dump(feature_columns, self.config.get_model_path('feature_columns'))
        
        print(f"   - Tous les artefacts sauvegardés dans {self.config.MODELS_DIR}/")


def main():
    """Point d'entrée principal pour le script de training"""
    trainer = ModelTrainer()
    trainer.train()


if __name__ == "__main__":
    main()