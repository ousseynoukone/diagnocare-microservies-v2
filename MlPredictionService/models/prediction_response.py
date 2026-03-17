"""
Modèle de données pour une réponse de prédiction
"""
from typing import List, Dict, Optional
from dataclasses import dataclass


@dataclass
class PredictionResult:
    """
    Résultat d'une prédiction individuelle (une maladie avec son spécialiste)
    """
    rank: int
    disease: str  # Nom dans la langue demandée (fr ou en)
    probability: float
    specialist: str  # Nom dans la langue demandée (fr ou en)
    specialist_probability: float
    description: str
    disease_fr: Optional[str] = None  # Nom en français (toujours disponible)
    specialist_fr: Optional[str] = None  # Nom en français (toujours disponible)
    disease_en: Optional[str] = None  # Nom en anglais (toujours disponible)
    specialist_en: Optional[str] = None  # Nom en anglais (toujours disponible)
    
    def to_dict(self) -> dict:
        """Convertit en dictionnaire pour la réponse JSON"""
        result = {
            'rank': self.rank,
            'disease': self.disease,
            'probability': self.probability,
            'specialist': self.specialist,
            'specialist_probability': self.specialist_probability,
            'description': self.description
        }
        
        if self.disease_fr:
            result['disease_fr'] = self.disease_fr
        if self.specialist_fr:
            result['specialist_fr'] = self.specialist_fr
        if self.disease_en:
            result['disease_en'] = self.disease_en
        if self.specialist_en:
            result['specialist_en'] = self.specialist_en
        
        return result


@dataclass
class PredictionResponse:
    """
    Réponse complète d'une prédiction
    """
    predictions: List[PredictionResult]
    language: str
    metadata: Dict[str, Any]
    confidence_level: str = "high"
    confidence_note: Optional[str] = None
    
    def to_dict(self) -> dict:
        """Convertit en dictionnaire pour la réponse JSON"""
        result = {
            "predictions": [pred.to_dict() for pred in self.predictions],
            "language": self.language,
            "metadata": self.metadata,
            "confidence_level": self.confidence_level,
        }
        if self.confidence_note:
            result["confidence_note"] = self.confidence_note
        return result