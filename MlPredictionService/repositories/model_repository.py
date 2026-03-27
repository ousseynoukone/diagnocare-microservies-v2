"""
Repository pour le chargement des modèles ML
"""
import joblib
import os
import logging
import pandas as pd
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
        self._disease_specialist_map: Optional[Dict[str, str]] = None
    
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
    def tfidf(self):
        """Raccourci pour accéder au TfidfVectorizer"""
        return self.get_model('tfidf')
    
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
    
    @property
    def disease_specialist_map(self) -> Dict[str, str]:
        """
        Mapping maladie (nettoyée) -> spécialiste, chargé depuis Doctor_Versus_Disease.csv.
        Utilisé pour associer le bon spécialiste à chaque maladie prédite,
        en évitant la désynchronisation par rang indépendant.
        """
        if self._disease_specialist_map is None:
            self._disease_specialist_map = self._load_disease_specialist_map()
        return self._disease_specialist_map

    def _load_disease_specialist_map(self) -> Dict[str, str]:
        try:
            from training.data_cleaning import clean_specialist_label
            from utils.text_utils import TextUtils

            mapping_path = os.path.join(self.config.DATA_DIR, 'Doctor_Versus_Disease.csv')
            if not os.path.exists(mapping_path):
                self.logger.warning(f"Fichier de mapping introuvable: {mapping_path}")
                return {}

            text_utils = TextUtils()
            df_map = pd.read_csv(
                mapping_path, header=None,
                names=['Disease', 'Specialist'], encoding='cp1252'
            )
            df_map['Disease_clean'] = df_map['Disease'].apply(text_utils.clean_text)
            df_map['Specialist'] = df_map['Specialist'].apply(clean_specialist_label)
            mapping = dict(zip(df_map['Disease_clean'], df_map['Specialist']))
            self.logger.info(f"Mapping maladie->spécialiste chargé: {len(mapping)} entrées")
            return mapping
        except Exception as e:
            self.logger.error(f"Erreur lors du chargement du mapping maladie->spécialiste: {e}")
            return {}

    def is_loaded(self) -> bool:
        """
        Vérifie si les modèles sont chargés
        Returns:
            bool: True si les modèles sont chargés
        """
        return self._loaded