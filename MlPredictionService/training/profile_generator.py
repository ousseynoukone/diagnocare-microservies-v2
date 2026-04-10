"""
Génération de profils patients synthétiques alignés sur le type de maladie (dataset d'entraînement).
"""
import numpy as np

from utils.text_utils import TextUtils


class ProfileGenerator:
    """
    Générateur de profils patients synthétiques basés sur le type de maladie
    """

    def __init__(self, random_seed: int = 42):
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

        if 'urinary tract' in disease_lower or 'uti' in disease_lower:
            gender = np.random.choice(['Male', 'Female'], p=[0.3, 0.7])
        else:
            gender = np.random.choice(['Male', 'Female'], p=[0.5, 0.5])

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

        if bp == 'High':
            tension_moyenne = np.random.normal(145, 10)
        elif bp == 'Low':
            tension_moyenne = np.random.normal(100, 8)
        else:
            tension_moyenne = np.random.normal(120, 8)
        tension_moyenne = max(80, min(180, int(tension_moyenne)))

        if chol == 'High':
            cholesterole_moyen = np.random.normal(240, 20)
        elif chol == 'Low':
            cholesterole_moyen = np.random.normal(150, 15)
        else:
            cholesterole_moyen = np.random.normal(190, 15)
        cholesterole_moyen = max(100, min(300, int(cholesterole_moyen)))

        if any(word in disease_lower for word in ['heart', 'cardiac', 'liver']):
            alcohol = np.random.choice(['None', 'Moderate', 'Heavy'], p=[0.4, 0.4, 0.2])
        else:
            alcohol = np.random.choice(['None', 'Moderate', 'Heavy'], p=[0.5, 0.4, 0.1])

        if any(word in disease_lower for word in ['diabetes', 'heart', 'cardiac', 'cancer']):
            family_history = np.random.choice(['No', 'Yes'], p=[0.3, 0.7])
        else:
            family_history = np.random.choice(['No', 'Yes'], p=[0.6, 0.4])

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
