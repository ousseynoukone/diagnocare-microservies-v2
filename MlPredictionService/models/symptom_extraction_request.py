"""
Modèle de données pour une requête d'extraction de symptômes
"""
from typing import Dict, Any, Optional
from dataclasses import dataclass


@dataclass
class SymptomExtractionRequest:
    """
    Modèle de requête pour l'extraction de symptômes depuis un texte
    """
    raw_description: str
    language: str = 'fr'
    
    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> 'SymptomExtractionRequest':
        """
        Crée une instance depuis un dictionnaire
        Args:
            data: Dictionnaire contenant les données de la requête
        Returns:
            SymptomExtractionRequest: Instance de la requête
        """
        language = data.get('language', 'fr').lower()
        if language not in ['fr', 'en']:
            language = 'fr'
        
        return cls(
            raw_description=data.get('raw_description', ''),
            language=language
        )
    
    def validate(self) -> tuple[bool, Optional[str]]:
        """
        Valide la requête
        Returns:
            tuple[bool, Optional[str]]: (est_valide, message_erreur)
        """
        if not self.raw_description:
            return False, "raw_description is required"
        
        return True, None