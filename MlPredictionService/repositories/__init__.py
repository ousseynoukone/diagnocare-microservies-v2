"""
Couche de récupération des données (repositories)
"""
from .model_repository import ModelRepository
from .translation_repository import TranslationRepository

__all__ = ['ModelRepository', 'TranslationRepository']