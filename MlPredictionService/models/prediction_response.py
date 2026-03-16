"""
Modèle de données pour une réponse de prédiction
"""
from typing import List, Dict, Optional
from dataclasses import dataclass, field


@dataclass
class PredictionResult:
    """
    Résultat d'une prédiction individuelle (une maladie avec son spécialiste)
    """
    rank: int
    disease: str  # Nom en anglais (original)
    probability: float
    specialist: str  # Nom en anglais (original)
    specialist_probability: float
    description: str
    disease_fr: Optional[str] = None
    specialist_fr: Optional[str] = None
    disease_en: Optional[str] = None
    specialist_en: Optional[str] = None
    
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
    metadata: Dict[str, any]
    
    def to_dict(self) -> dict:
        """Convertit en dictionnaire pour la réponse JSON"""
        return {
            "predictions": [pred.to_dict() for pred in self.predictions],
            "language": self.language,
            "metadata": self.metadata
        }