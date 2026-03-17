"""
Génération de profils patients synthétiques pour l'entraînement.

Comme le dataset ne contient que des symptômes et des maladies, on invente des profils
(âge, tension, cholestérol, etc.) cohérents avec le type de pathologie pour que
le modèle ait des features supplémentaires réalistes.
"""
import os
import sys
import numpy as np

# Pour importer les utils du projet (parent de training/)
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from utils.text_utils import TextUtils


class ProfileGenerator:
    """
    Génère un profil patient (âge, poids, tension, etc.) à partir du nom de la maladie.
    Les distributions sont biaisées selon la pathologie (ex. plus de fumeurs pour le cardiaque).
    """

    def __init__(self, random_seed: int = 42):
        np.random.seed(random_seed)
        self.text_utils = TextUtils()

    def generate(self, disease_name: str) -> dict:
        """
        Un profil par maladie : on adapte âge, tension, cholestérol, etc.
        pour que ça ressemble à des vrais patients typiques de cette pathologie.
        """
        disease_lower = disease_name.lower()

        # --- Âge et indicateurs cardio / style de vie selon le type de maladie ---
        if any(w in disease_lower for w in ['heart', 'cardiac', 'hypertension', 'myocardial']):
            # Cardiaque : plutôt âgé, tension/cholestérol/sédentarité souvent élevés
            age = int(np.random.normal(55, 10))
            age = max(40, min(80, age))
            bp = np.random.choice(['Low', 'Normal', 'High'], p=[0.1, 0.2, 0.7])
            chol = np.random.choice(['Low', 'Normal', 'High'], p=[0.1, 0.3, 0.6])
            smoking = np.random.choice(['No', 'Yes'], p=[0.3, 0.7])
            sedentarite = np.random.choice(['Low', 'Moderate', 'High'], p=[0.2, 0.3, 0.5])
        elif any(w in disease_lower for w in ['acne', 'chicken pox', 'rubella']):
            # Maladies plutôt jeunes : âge plus bas, moins de facteurs de risque
            age = int(np.random.normal(20, 8))
            age = max(10, min(35, age))
            bp = np.random.choice(['Low', 'Normal', 'High'], p=[0.3, 0.5, 0.2])
            chol = np.random.choice(['Low', 'Normal', 'High'], p=[0.4, 0.5, 0.1])
            smoking = np.random.choice(['No', 'Yes'], p=[0.7, 0.3])
            sedentarite = np.random.choice(['Low', 'Moderate', 'High'], p=[0.5, 0.3, 0.2])
        elif any(w in disease_lower for w in ['diabetes', 'thyroid']):
            age = int(np.random.normal(45, 12))
            age = max(25, min(70, age))
            bp = np.random.choice(['Low', 'Normal', 'High'], p=[0.2, 0.4, 0.4])
            chol = np.random.choice(['Low', 'Normal', 'High'], p=[0.2, 0.4, 0.4])
            smoking = np.random.choice(['No', 'Yes'], p=[0.5, 0.5])
            sedentarite = np.random.choice(['Low', 'Moderate', 'High'], p=[0.2, 0.4, 0.4])
        else:
            # Cas par défaut : distribution neutre
            age = int(np.random.normal(35, 15))
            age = max(18, min(75, age))
            bp = np.random.choice(['Low', 'Normal', 'High'], p=[0.25, 0.5, 0.25])
            chol = np.random.choice(['Low', 'Normal', 'High'], p=[0.25, 0.5, 0.25])
            smoking = np.random.choice(['No', 'Yes'], p=[0.6, 0.4])
            sedentarite = np.random.choice(['Low', 'Moderate', 'High'], p=[0.3, 0.4, 0.3])

        # Genre : certaines pathologies touchent plus les femmes (ex. UTI)
        if 'urinary tract' in disease_lower or 'uti' in disease_lower:
            gender = np.random.choice(['Male', 'Female'], p=[0.3, 0.7])
        else:
            gender = np.random.choice(['Male', 'Female'], p=[0.5, 0.5])

        # Poids et IMC : on part d’une base selon genre/âge puis on décale selon la maladie
        if gender == 'Male':
            base_weight = 75 + (age - 35) * 0.3
            avg_height = 175
        else:
            base_weight = 65 + (age - 35) * 0.2
            avg_height = 162

        if any(w in disease_lower for w in ['diabetes', 'heart', 'cardiac', 'hypertension']):
            weight = int(np.random.normal(base_weight + 10, 12))
        elif any(w in disease_lower for w in ['acne', 'chicken pox', 'rubella']):
            weight = int(np.random.normal(base_weight - 5, 8))
        else:
            weight = int(np.random.normal(base_weight, 10))
        weight = max(40, min(150, weight))
        bmi = weight / ((avg_height / 100) ** 2)

        # Tension moyenne en mmHg, cohérente avec la catégorie BP
        if bp == 'High':
            tension_moyenne = np.random.normal(145, 10)
        elif bp == 'Low':
            tension_moyenne = np.random.normal(100, 8)
        else:
            tension_moyenne = np.random.normal(120, 8)
        tension_moyenne = max(80, min(180, int(tension_moyenne)))

        # Cholestérol en mg/dL
        if chol == 'High':
            cholesterole_moyen = np.random.normal(240, 20)
        elif chol == 'Low':
            cholesterole_moyen = np.random.normal(150, 15)
        else:
            cholesterole_moyen = np.random.normal(190, 15)
        cholesterole_moyen = max(100, min(300, int(cholesterole_moyen)))

        # Alcool : un peu plus de consommation pour certaines pathologies (cardio, foie)
        if any(w in disease_lower for w in ['heart', 'cardiac', 'liver']):
            alcohol = np.random.choice(['None', 'Moderate', 'Heavy'], p=[0.4, 0.4, 0.2])
        else:
            alcohol = np.random.choice(['None', 'Moderate', 'Heavy'], p=[0.5, 0.4, 0.1])

        # Antécédents familiaux : plus fréquents pour diabète, cardiaque, cancer
        if any(w in disease_lower for w in ['diabetes', 'heart', 'cardiac', 'cancer']):
            family_history = np.random.choice(['No', 'Yes'], p=[0.3, 0.7])
        else:
            family_history = np.random.choice(['No', 'Yes'], p=[0.6, 0.4])

        # Variable “outcome” (positif/négatif) : plus souvent positif pour maladies graves
        if any(w in disease_lower for w in ['cancer', 'stroke', 'heart attack']):
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
