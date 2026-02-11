"""
Modèles de données pour le service ML Prediction
"""
from .prediction_request import PredictionRequest
from .prediction_response import PredictionResponse, PredictionResult
from .symptom_extraction_request import SymptomExtractionRequest
from .translation_request import TranslationRequest

__all__ = [
    'PredictionRequest',
    'PredictionResponse',
    'PredictionResult',
    'SymptomExtractionRequest',
    'TranslationRequest'
]