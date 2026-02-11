"""
Modèle de données pour une requête de prédiction
"""
from typing import List, Optional, Dict, Any
from dataclasses import dataclass, field


@dataclass
class PredictionRequest:
    """
    Modèle de requête pour une prédiction de maladie
    """
    symptoms: List[str]
    language: str = 'fr'
    age: Optional[int] = None
    weight: Optional[float] = None
    bmi: Optional[float] = None
    tension_moyenne: Optional[float] = None
    cholesterole_moyen: Optional[float] = None
    gender: Optional[str] = None
    blood_pressure: Optional[str] = None
    cholesterol_level: Optional[str] = None
    outcome_variable: Optional[str] = None
    smoking: Optional[str] = None
    alcohol: Optional[str] = None
    sedentarite: Optional[str] = None
    family_history: Optional[str] = None
    height: Optional[float] = None  # Pour calculer BMI si non fourni
    
    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> 'PredictionRequest':
        """
        Crée une instance depuis un dictionnaire (ex: request.json)
        Args:
            data: Dictionnaire contenant les données de la requête
        Returns:
            PredictionRequest: Instance de la requête
        """
        return cls(
            symptoms=data.get('symptoms', []),
            language=data.get('language', 'fr').lower(),
            age=data.get('age'),
            weight=data.get('weight'),
            bmi=data.get('bmi') or data.get('IMC'),
            tension_moyenne=data.get('tension_moyenne'),
            cholesterole_moyen=data.get('cholesterole_moyen'),
            gender=data.get('gender'),
            blood_pressure=data.get('blood_pressure'),
            cholesterol_level=data.get('cholesterol_level'),
            outcome_variable=data.get('outcome_variable'),
            smoking=data.get('smoking'),
            alcohol=data.get('alcohol'),
            sedentarite=data.get('sedentarite'),
            family_history=data.get('family_history'),
            height=data.get('height')
        )
    
    def validate(self) -> tuple[bool, Optional[str]]:
        """
        Valide la requête
        Returns:
            tuple[bool, Optional[str]]: (est_valide, message_erreur)
        """
        if not self.symptoms:
            return False, "symptoms is required and must be in English"
        
        if isinstance(self.symptoms, str):
            return False, "symptoms must be an array of strings"
        
        if self.language not in ['fr', 'en']:
            self.language = 'fr'  # Valeur par défaut
        
        return True, None