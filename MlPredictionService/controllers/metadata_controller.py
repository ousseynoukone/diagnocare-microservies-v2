"""
Contrôleur pour les endpoints de métadonnées
"""
from flask import jsonify
from repositories.model_repository import ModelRepository
from services.translation_service import TranslationService


class MetadataController:
    """
    Contrôleur pour les endpoints de métadonnées
    """
    
    def __init__(
        self,
        model_repository: ModelRepository,
        translation_service: TranslationService
    ):
        """
        Initialise le contrôleur de métadonnées
        Args:
            model_repository: Repository pour accéder aux modèles
            translation_service: Service pour les traductions
        """
        self.model_repository = model_repository
        self.translation_service = translation_service
    
    def get_features_metadata(self):
        """
        Retourne les métadonnées des symptômes et features
        Returns:
            tuple: (réponse JSON, code HTTP)
        """
        mlb = self.model_repository.mlb
        symptoms = sorted([str(s) for s in mlb.classes_])
        
        symptoms_en = [
            {"id": s, "label": self.translation_service.translate_symptom(s, target_lang="en")}
            for s in symptoms
        ]
        symptoms_fr = [
            {"id": s, "label": self.translation_service.translate_symptom(s, target_lang="fr")}
            for s in symptoms
        ]
        
        numeric_features = [
            {"key": "age", "name_en": "Age", "name_fr": "Âge", "unit_en": "years", "unit_fr": "ans", "range": "10-80"},
            {"key": "weight", "name_en": "Weight", "name_fr": "Poids", "unit_en": "kg", "unit_fr": "kg", "range": "40-150"},
            {"key": "bmi", "name_en": "BMI", "name_fr": "IMC", "unit_en": "kg/m²", "unit_fr": "kg/m²", "range": "12-50"},
            {"key": "tension_moyenne", "name_en": "Mean blood pressure", "name_fr": "Tension moyenne", "unit_en": "mmHg", "unit_fr": "mmHg", "range": "80-180"},
            {"key": "cholesterole_moyen", "name_en": "Mean cholesterol", "name_fr": "Cholestérol moyen", "unit_en": "mg/dL", "unit_fr": "mg/dL", "range": "100-300"}
        ]
        
        categorical_features = [
            {"key": "gender", "name_en": "Gender", "name_fr": "Genre",
             "values_en": ["Male", "Female"], "values_fr": ["Homme", "Femme"]},
            {"key": "blood_pressure", "name_en": "Blood Pressure", "name_fr": "Pression artérielle",
             "values_en": ["Low", "Normal", "High"], "values_fr": ["Faible", "Normale", "Élevée"]},
            {"key": "cholesterol_level", "name_en": "Cholesterol Level", "name_fr": "Niveau de cholestérol",
             "values_en": ["Low", "Normal", "High"], "values_fr": ["Faible", "Normal", "Élevé"]},
            {"key": "outcome_variable", "name_en": "Outcome Variable", "name_fr": "Variable de résultat",
             "values_en": ["Negative", "Positive"], "values_fr": ["Négatif", "Positif"]},
            {"key": "smoking", "name_en": "Smoking", "name_fr": "Tabagisme",
             "values_en": ["No", "Yes"], "values_fr": ["Non", "Oui"]},
            {"key": "alcohol", "name_en": "Alcohol", "name_fr": "Alcool",
             "values_en": ["None", "Moderate", "Heavy"], "values_fr": ["Aucun", "Modéré", "Élevé"]},
            {"key": "sedentarite", "name_en": "Sedentariness", "name_fr": "Sédentarité",
             "values_en": ["Low", "Moderate", "High"], "values_fr": ["Faible", "Modérée", "Élevée"]},
            {"key": "family_history", "name_en": "Family History", "name_fr": "Antécédents familiaux",
             "values_en": ["No", "Yes"], "values_fr": ["Non", "Oui"]}
        ]
        
        return jsonify({
            "symptoms": {
                "count": len(symptoms),
                "en": symptoms_en,
                "fr": symptoms_fr
            },
            "features": {
                "numeric": numeric_features,
                "categorical": categorical_features
            },
            "languages": ["en", "fr"]
        }), 200