"""
Repository pour le chargement des modèles ML
"""
import joblib
import os
import logging
from typing import Dict, Any, Optional
from config.model_config import ModelConfig


class ModelRepository:
    """
    Repository pour charger et gérer les modèles ML
    """
    
    def __init__(self, config: ModelConfig):
        """
        Initialise le repository avec la configuration
        Args:
            config: Configuration des modèles
        """
        self.config = config
        self.logger = logging.getLogger(__name__)
        self._models: Dict[str, Any] = {}
        self._loaded = False
    
    def load_all(self) -> bool:
        """
        Charge tous les modèles depuis le disque
        Returns:
            bool: True si le chargement a réussi, False sinon
        """
        try:
            self.logger.info("Chargement des modèles...")
            
            model_paths = self.config.get_all_model_paths()
            
            for model_name, model_path in model_paths.items():
                if not os.path.exists(model_path):
                    self.logger.error(f"Fichier modèle introuvable: {model_path}")
                    return False
                
                self._models[model_name] = joblib.load(model_path)
                self.logger.debug(f"Modèle chargé: {model_name}")
            
            self._loaded = True
            self.logger.info("Tous les modèles ont été chargés avec succès.")
            return True
            
        except Exception as e:
            self.logger.error(f"Erreur lors du chargement des modèles: {e}")
            return False
    
    def get_model(self, model_name: str) -> Optional[Any]:
        """
        Récupère un modèle par son nom
        Args:
            model_name: Nom du modèle
        Returns:
            Modèle chargé ou None si non trouvé
        """
        if not self._loaded:
            if not self.load_all():
                return None
        
        return self._models.get(model_name)
    
    @property
    def model(self):
        """Raccourci pour accéder au modèle principal"""
        return self.get_model('model')
    
    @property
    def mlb(self):
        """Raccourci pour accéder au MultiLabelBinarizer"""
        return self.get_model('mlb')
    
    @property
    def scaler(self):
        """Raccourci pour accéder au StandardScaler"""
        return self.get_model('scaler')
    
    @property
    def le_disease(self):
        """Raccourci pour accéder au LabelEncoder des maladies"""
        return self.get_model('le_disease')
    
    @property
    def le_specialist(self):
        """Raccourci pour accéder au LabelEncoder des spécialistes"""
        return self.get_model('le_specialist')
    
    @property
    def feature_columns(self):
        """Raccourci pour accéder aux colonnes de features"""
        return self.get_model('feature_columns')
    
    def is_loaded(self) -> bool:
        """
        Vérifie si les modèles sont chargés
        Returns:
            bool: True si les modèles sont chargés
        """
        return self._loaded