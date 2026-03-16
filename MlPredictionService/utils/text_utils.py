"""
Utilitaires pour le traitement de texte
"""
import re
import pandas as pd


class TextUtils:
    """
    Classe utilitaire pour le nettoyage et la normalisation de texte
    """
    
    @staticmethod
    def clean_text(text: str) -> str:
        """
        Nettoie et normalise un texte pour correspondre au format du modèle ML
        Args:
            text: Texte à nettoyer
        Returns:
            str: Texte nettoyé et normalisé
        """
        if pd.isna(text):
            return ""
        
        # Normalisation: minuscules, suppression des caractères spéciaux
        normalized = str(text).strip().lower()
        normalized = normalized.replace("-", " ").replace("/", " ")
        
        # Remplacement des espaces par des underscores
        return "_".join(re.sub(r'[^\w\s]', '', normalized).split())
    
    @staticmethod
    def normalize_symptom_name(symptom_name: str) -> str:
        """
        Normalise un nom de symptôme pour correspondre au format du modèle ML
        Args:
            symptom_name: Nom du symptôme à normaliser
        Returns:
            str: Nom normalisé
        """
        if not symptom_name:
            return ""
        
        # Conversion en minuscules et remplacement des espaces par des underscores
        normalized = re.sub(r'[^\w\s]', '', str(symptom_name).strip().lower())
        normalized = "_".join(normalized.split())
        return normalized
    
    @staticmethod
    def format_symptom_for_display(symptom_name: str) -> str:
        """
        Formate un nom de symptôme pour l'affichage (remplace _ par des espaces)
        Args:
            symptom_name: Nom du symptôme à formater
        Returns:
            str: Nom formaté pour l'affichage
        """
        if not symptom_name:
            return ""
        return symptom_name.replace("_", " ").strip()