"""
Configuration pour le chargement des modèles ML
"""
import os
from typing import Dict


class ModelConfig:
    """
    Configuration pour les chemins des modèles et fichiers de données
    """
    
    def __init__(self, base_dir: str = None):
        """
        Initialise la configuration des modèles
        Args:
            base_dir: Répertoire de base (par défaut: répertoire courant)
        """
        if base_dir is None:
            base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
        
        self.BASE_DIR = base_dir
        self.MODELS_DIR = os.path.join(base_dir, 'models')
        self.DATA_DIR = os.path.join(base_dir, 'data')
        self.TRANSLATIONS_FILE = os.path.join(self.DATA_DIR, 'translations.json')
        
        # Noms des fichiers de modèles
        self.MODEL_FILES = {
            'model': 'model.joblib',
            'mlb': 'mlb.joblib',
            'scaler': 'scaler.joblib',
            'le_disease': 'le_disease.joblib',
            'le_specialist': 'le_specialist.joblib',
            'feature_columns': 'feature_columns.joblib'
        }
    
    def get_model_path(self, model_name: str) -> str:
        """
        Retourne le chemin complet d'un modèle
        Args:
            model_name: Nom du modèle (clé dans MODEL_FILES)
        Returns:
            str: Chemin complet du fichier modèle
        """
        if model_name not in self.MODEL_FILES:
            raise ValueError(f"Modèle inconnu: {model_name}")
        
        return os.path.join(self.MODELS_DIR, self.MODEL_FILES[model_name])
    
    def get_all_model_paths(self) -> Dict[str, str]:
        """
        Retourne tous les chemins des modèles
        Returns:
            Dict[str, str]: Dictionnaire {nom_modèle: chemin}
        """
        return {name: self.get_model_path(name) for name in self.MODEL_FILES.keys()}