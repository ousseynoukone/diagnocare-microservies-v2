"""
Contrôleur pour les endpoints de prédiction, extraction et traduction
"""
from flask import request, jsonify
from services.prediction_service import PredictionService
from services.nlp_service import NLPService
from services.translation_service import TranslationService
from repositories.model_repository import ModelRepository
from models.prediction_request import PredictionRequest
from models.symptom_extraction_request import SymptomExtractionRequest
from models.translation_request import TranslationRequest


class PredictionController:
    """
    Contrôleur pour les endpoints de prédiction
    """
    
    def __init__(
        self,
        prediction_service: PredictionService,
        nlp_service: NLPService,
        translation_service: TranslationService,
        model_repository: ModelRepository
    ):
        """
        Initialise le contrôleur de prédiction
        Args:
            prediction_service: Service de prédiction
            nlp_service: Service NLP
            translation_service: Service de traduction
            model_repository: Repository pour accéder aux modèles
        """
        self.prediction_service = prediction_service
        self.nlp_service = nlp_service
        self.translation_service = translation_service
        self.model_repository = model_repository
    
    def predict(self):
        """
        Endpoint pour effectuer une prédiction
        Returns:
            tuple: (réponse JSON, code HTTP)
        """
        try:
            data = request.json
            pred_request = PredictionRequest.from_dict(data)
            
            response = self.prediction_service.predict(pred_request)
            return jsonify(response.to_dict()), 200
            
        except ValueError as e:
            return jsonify({"error": str(e)}), 400
        except Exception as e:
            return jsonify({"error": str(e)}), 500
    
    def extract_symptoms(self):
        """
        Endpoint pour extraire les symptômes depuis un texte
        Returns:
            tuple: (réponse JSON, code HTTP)
        """
        try:
            data = request.json
            extract_request = SymptomExtractionRequest.from_dict(data)
            
            is_valid, error_msg = extract_request.validate()
            if not is_valid:
                return jsonify({"error": error_msg}), 400
            
            # Récupération des symptômes disponibles depuis le modèle
            available_symptoms = list(self.model_repository.mlb.classes_)
            
            # Extraction des symptômes
            extracted_symptoms = self.nlp_service.extract_symptoms_from_text(
                extract_request.raw_description,
                available_symptoms,
                language=extract_request.language
            )
            
            return jsonify({
                "symptoms": extracted_symptoms,
                "language": extract_request.language
            }), 200
            
        except Exception as e:
            return jsonify({"error": str(e)}), 500
    
    def translate(self):
        """
        Endpoint pour traduire des symptômes, maladies et spécialistes
        Returns:
            tuple: (réponse JSON, code HTTP)
        """
        try:
            data = request.json or {}
            translate_request = TranslationRequest.from_dict(data)
            
            is_valid, error_msg = translate_request.validate()
            if not is_valid:
                return jsonify({"error": error_msg}), 400
            
            translated_symptoms = [
                self.translation_service.translate_symptom(
                    s, target_lang=translate_request.language
                )
                for s in translate_request.symptoms if s
            ]
            
            translated_diseases = [
                self.translation_service.translate_disease(
                    d, target_lang=translate_request.language
                )
                for d in translate_request.diseases if d
            ]
            
            translated_specialists = [
                self.translation_service.translate_specialist(
                    s, target_lang=translate_request.language
                )
                for s in translate_request.specialists if s
            ]
            
            return jsonify({
                "language": translate_request.language,
                "symptoms": translated_symptoms,
                "diseases": translated_diseases,
                "specialists": translated_specialists
            }), 200
            
        except Exception as e:
            return jsonify({"error": str(e)}), 500