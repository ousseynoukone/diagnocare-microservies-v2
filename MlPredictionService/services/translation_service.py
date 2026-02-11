"""
Service de traduction pour les symptômes, maladies et spécialistes
"""
import logging
from typing import Dict, List
from repositories.translation_repository import TranslationRepository
from utils.text_utils import TextUtils


class TranslationService:
    """
    Service pour gérer les traductions
    """
    
    def __init__(self, translation_repository: TranslationRepository):
        """
        Initialise le service de traduction
        Args:
            translation_repository: Repository pour accéder aux traductions
        """
        self.translation_repository = translation_repository
        self.text_utils = TextUtils()
        self.logger = logging.getLogger(__name__)
    
    def translate_disease(self, disease_name_en: str, target_lang: str = 'fr') -> str:
        """
        Traduit un nom de maladie
        Args:
            disease_name_en: Nom de la maladie en anglais
            target_lang: Langue cible ('fr' ou 'en')
        Returns:
            str: Nom traduit ou original si traduction non trouvée
        """
        if target_lang == 'en':
            return disease_name_en
        
        translations = self.translation_repository.get_translations()
        diseases = translations.get('diseases', {})
        return diseases.get(disease_name_en, disease_name_en)
    
    def translate_specialist(self, specialist_name_en: str, target_lang: str = 'fr') -> str:
        """
        Traduit un nom de spécialiste
        Args:
            specialist_name_en: Nom du spécialiste en anglais
            target_lang: Langue cible ('fr' ou 'en')
        Returns:
            str: Nom traduit ou original si traduction non trouvée
        """
        if target_lang == 'en':
            return specialist_name_en
        
        translations = self.translation_repository.get_translations()
        specialists = translations.get('specialists', {})
        return specialists.get(specialist_name_en, specialist_name_en)
    
    def translate_symptom(self, symptom_name_en: str, target_lang: str = 'fr') -> str:
        """
        Traduit un nom de symptôme
        Args:
            symptom_name_en: Nom du symptôme en anglais
            target_lang: Langue cible ('fr' ou 'en')
        Returns:
            str: Nom traduit ou formaté pour l'affichage
        """
        normalized = self.text_utils.normalize_symptom_name(symptom_name_en)
        
        if target_lang == 'en':
            return self.text_utils.format_symptom_for_display(normalized)
        
        translations = self.translation_repository.get_translations()
        symptoms_map = translations.get('symptoms', {})
        translated = symptoms_map.get(normalized)
        
        if translated:
            return translated
        
        return self.text_utils.format_symptom_for_display(normalized)
    
    def translate_symptoms(self, symptoms_en: List[str], target_lang: str = 'fr') -> List[str]:
        """
        Traduit une liste de symptômes
        Args:
            symptoms_en: Liste des noms de symptômes en anglais
            target_lang: Langue cible ('fr' ou 'en')
        Returns:
            List[str]: Liste des symptômes traduits
        """
        return [
            self.translate_symptom(s, target_lang=target_lang) 
            for s in symptoms_en if s
        ]