"""
Modèle de données pour une requête de traduction
"""
from typing import List, Dict, Any, Optional
from dataclasses import dataclass, field


@dataclass
class TranslationRequest:
    """
    Modèle de requête pour la traduction de symptômes, maladies et spécialistes
    """
    language: str = 'fr'
    symptoms: List[str] = field(default_factory=list)
    diseases: List[str] = field(default_factory=list)
    specialists: List[str] = field(default_factory=list)
    
    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> 'TranslationRequest':
        """
        Crée une instance depuis un dictionnaire
        Args:
            data: Dictionnaire contenant les données de la requête
        Returns:
            TranslationRequest: Instance de la requête
        """
        language = data.get('language', 'fr').lower()
        if language not in ['fr', 'en']:
            language = 'fr'
        
        symptoms = data.get('symptoms', []) or []
        diseases = data.get('diseases', []) or []
        specialists = data.get('specialists', []) or []
        
        return cls(
            language=language,
            symptoms=symptoms if isinstance(symptoms, list) else [],
            diseases=diseases if isinstance(diseases, list) else [],
            specialists=specialists if isinstance(specialists, list) else []
        )
    
    def validate(self) -> tuple[bool, Optional[str]]:
        """
        Valide la requête
        Returns:
            tuple[bool, Optional[str]]: (est_valide, message_erreur)
        """
        if not isinstance(self.symptoms, list) or not isinstance(self.diseases, list) or not isinstance(self.specialists, list):
            return False, "symptoms, diseases, and specialists must be arrays"
        
        return True, None