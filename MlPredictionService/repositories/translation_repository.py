"""
Repository pour le chargement des traductions
"""
import json
import os
import logging
from typing import Dict, Optional
from config.model_config import ModelConfig


class TranslationRepository:
    """
    Repository pour charger et gérer les traductions
    """
    
    def __init__(self, config: ModelConfig):
        """
        Initialise le repository avec la configuration
        Args:
            config: Configuration des modèles (contient le chemin des traductions)
        """
        self.config = config
        self.logger = logging.getLogger(__name__)
        self._translations: Dict[str, Dict[str, str]] = {}
        self._loaded = False
    
    def load(self) -> bool:
        """
        Charge les traductions depuis le fichier JSON
        Returns:
            bool: True si le chargement a réussi, False sinon
        """
        try:
            self.logger.info("Chargement des traductions...")
            
            if not os.path.exists(self.config.TRANSLATIONS_FILE):
                self.logger.warning(f"Fichier de traduction introuvable: {self.config.TRANSLATIONS_FILE}")
                self._translations = {"diseases": {}, "specialists": {}, "symptoms": {}}
                self._loaded = True
                return True
            
            with open(self.config.TRANSLATIONS_FILE, 'r', encoding='utf-8') as f:
                self._translations = json.load(f)
            
            # S'assurer que toutes les clés existent
            if 'diseases' not in self._translations:
                self._translations['diseases'] = {}
            if 'specialists' not in self._translations:
                self._translations['specialists'] = {}
            if 'symptoms' not in self._translations:
                self._translations['symptoms'] = {}
            
            self._loaded = True
            self.logger.info("Traductions chargées avec succès.")
            return True
            
        except json.JSONDecodeError as e:
            self.logger.error(f"Erreur de décodage JSON: {e}")
            self._translations = {"diseases": {}, "specialists": {}, "symptoms": {}}
            self._loaded = True
            return False
        except Exception as e:
            self.logger.error(f"Erreur lors du chargement des traductions: {e}")
            self._translations = {"diseases": {}, "specialists": {}, "symptoms": {}}
            self._loaded = True
            return False
    
    def get_translations(self) -> Dict[str, Dict[str, str]]:
        """
        Récupère toutes les traductions
        Returns:
            Dict: Dictionnaire des traductions
        """
        if not self._loaded:
            self.load()
        
        return self._translations
    
    def is_loaded(self) -> bool:
        """
        Vérifie si les traductions sont chargées
        Returns:
            bool: True si les traductions sont chargées
        """
        return self._loaded