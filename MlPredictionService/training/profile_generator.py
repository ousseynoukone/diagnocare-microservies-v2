"""
Génération de profils patients synthétiques pour l'entraînement.

IMPORTANT : les distributions ne doivent PAS être trop corrélées à la maladie.
Si 100% des cardiaques ont 55+ ans et hypertension, le modèle n'apprend rien
du profil — il trouve un raccourci plus direct via les symptômes et ignore le profil.

L'idée : les maladies cardiaques sont PLUS FRÉQUENTES chez les vieux fumeurs,
mais elles EXISTENT AUSSI chez des jeunes en bonne santé. On reflète ça avec
des distributions qui se CHEVAUCHENT entre catégories de maladies, avec un biais
léger (pas écrasant) vers le profil typique.
"""
import os
import sys
import numpy as np

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from utils.text_utils import TextUtils


# Distributions par catégorie de maladie.
# Chaque catégorie définit un BIAIS léger, pas un déterminisme.
# La clé "default" sert de base ; les catégories ne font que décaler légèrement.

DISEASE_CATEGORIES = {
    'cardiac':   {'keywords': ['heart', 'cardiac', 'hypertension', 'myocardial'],
                  'age_mean': 50, 'age_std': 15, 'age_min': 25, 'age_max': 80,
                  'bp': [0.15, 0.35, 0.50], 'chol': [0.15, 0.35, 0.50],
                  'smoking': [0.40, 0.60], 'sedentarite': [0.25, 0.35, 0.40],
                  'weight_offset': 5, 'family_yes': 0.55, 'outcome_pos': 0.65},

    'young':     {'keywords': ['acne', 'chicken pox', 'rubella'],
                  'age_mean': 22, 'age_std': 10, 'age_min': 10, 'age_max': 50,
                  'bp': [0.30, 0.45, 0.25], 'chol': [0.35, 0.45, 0.20],
                  'smoking': [0.65, 0.35], 'sedentarite': [0.40, 0.35, 0.25],
                  'weight_offset': -3, 'family_yes': 0.35, 'outcome_pos': 0.45},

    'metabolic': {'keywords': ['diabetes', 'thyroid'],
                  'age_mean': 42, 'age_std': 14, 'age_min': 20, 'age_max': 75,
                  'bp': [0.20, 0.40, 0.40], 'chol': [0.20, 0.40, 0.40],
                  'smoking': [0.50, 0.50], 'sedentarite': [0.25, 0.40, 0.35],
                  'weight_offset': 5, 'family_yes': 0.55, 'outcome_pos': 0.55},

    'hepatic':   {'keywords': ['hepatitis', 'liver', 'jaundice', 'cholestasis'],
                  'age_mean': 40, 'age_std': 15, 'age_min': 18, 'age_max': 75,
                  'bp': [0.25, 0.45, 0.30], 'chol': [0.20, 0.45, 0.35],
                  'smoking': [0.50, 0.50], 'sedentarite': [0.30, 0.40, 0.30],
                  'weight_offset': 0, 'family_yes': 0.40, 'outcome_pos': 0.50,
                  'alcohol_heavy': 0.20},

    'default':   {'age_mean': 38, 'age_std': 16, 'age_min': 18, 'age_max': 75,
                  'bp': [0.25, 0.50, 0.25], 'chol': [0.25, 0.50, 0.25],
                  'smoking': [0.55, 0.45], 'sedentarite': [0.30, 0.40, 0.30],
                  'weight_offset': 0, 'family_yes': 0.40, 'outcome_pos': 0.50},
}


class ProfileGenerator:
    """
    Génère un profil patient à partir du nom de la maladie.
    Les distributions se CHEVAUCHENT entre catégories pour forcer le modèle
    à utiliser le profil comme contexte, pas comme raccourci.
    """

    def __init__(self, random_seed: int = 42):
        np.random.seed(random_seed)
        self.text_utils = TextUtils()

    def _get_category(self, disease_name: str) -> dict:
        disease_lower = disease_name.lower()
        for cat_name, cat in DISEASE_CATEGORIES.items():
            if cat_name == 'default':
                continue
            if any(w in disease_lower for w in cat.get('keywords', [])):
                return cat
        return DISEASE_CATEGORIES['default']

    def generate(self, disease_name: str) -> dict:
        cat = self._get_category(disease_name)
        disease_lower = disease_name.lower()

        # Âge : large écart-type pour que les distributions se chevauchent
        age = int(np.random.normal(cat['age_mean'], cat['age_std']))
        age = max(cat['age_min'], min(cat['age_max'], age))

        bp = np.random.choice(['Low', 'Normal', 'High'], p=cat['bp'])
        chol = np.random.choice(['Low', 'Normal', 'High'], p=cat['chol'])
        smoking = np.random.choice(['No', 'Yes'], p=cat['smoking'])
        sedentarite = np.random.choice(['Low', 'Moderate', 'High'], p=cat['sedentarite'])

        # Genre : léger biais pour UTI, sinon 50/50
        if 'urinary tract' in disease_lower or 'uti' in disease_lower:
            gender = np.random.choice(['Male', 'Female'], p=[0.35, 0.65])
        else:
            gender = np.random.choice(['Male', 'Female'], p=[0.50, 0.50])

        # Poids et IMC
        if gender == 'Male':
            base_weight = 75 + (age - 35) * 0.3
            avg_height = 175
        else:
            base_weight = 65 + (age - 35) * 0.2
            avg_height = 162

        weight = int(np.random.normal(base_weight + cat['weight_offset'], 12))
        weight = max(40, min(150, weight))
        bmi = weight / ((avg_height / 100) ** 2)

        # Tension et cholestérol numériques (cohérents avec la catégorie)
        tension_map = {'High': (140, 15), 'Low': (100, 12), 'Normal': (120, 12)}
        t_mean, t_std = tension_map[bp]
        tension_moyenne = max(80, min(180, int(np.random.normal(t_mean, t_std))))

        chol_map = {'High': (235, 25), 'Low': (155, 20), 'Normal': (190, 20)}
        c_mean, c_std = chol_map[chol]
        cholesterole_moyen = max(100, min(300, int(np.random.normal(c_mean, c_std))))

        # Alcool
        heavy_p = cat.get('alcohol_heavy', 0.10)
        rest = 1.0 - heavy_p
        alcohol = np.random.choice(['None', 'Moderate', 'Heavy'],
                                   p=[rest * 0.55, rest * 0.45, heavy_p])

        # Antécédents familiaux
        fam_yes = cat['family_yes']
        family_history = np.random.choice(['No', 'Yes'], p=[1 - fam_yes, fam_yes])

        # Outcome
        out_pos = cat['outcome_pos']
        outcome = np.random.choice(['Negative', 'Positive'], p=[1 - out_pos, out_pos])

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
