"""
Orchestration de l’entraînement : préparation des données, entraînement du modèle,
évaluation, puis sauvegarde des artefacts (.joblib + noms des colonnes).
"""
import os
import sys
import joblib
import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from config.model_config import ModelConfig
from utils.text_utils import TextUtils

# Imports locaux (même dossier training/)
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from profile_generator import ProfileGenerator
from data_preparation import prepare_data
from evaluation import evaluate_and_report


class ModelTrainer:
    """
    Enchaîne : chargement des données, construction des features, split train/test,
    fit du RandomForest, évaluation (métriques + matrices de confusion), sauvegarde.
    """

    def __init__(self, config: ModelConfig = None):
        if config is None:
            base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
            config = ModelConfig(base_dir=base_dir)
        self.config = config
        self.text_utils = TextUtils()
        self.profile_generator = ProfileGenerator()

    def train(self) -> None:
        # --- Données : symptômes, profils, mapping maladie -> spécialiste, features ---
        (
            df_features,
            Y_combined,
            mlb,
            scaler,
            le_disease,
            le_specialist,
            df_filtered,
            feature_columns,
        ) = prepare_data(self.config, self.text_utils, self.profile_generator)

        # --- Split stratifié par maladie (pour avoir les mêmes proportions en train et test) ---
        print("\n6. Entraînement du modèle (split stratifié par maladie)...")
        y_disease_only = df_filtered['Disease'].values
        X_train, X_test, Y_train, Y_test = train_test_split(
            df_features, Y_combined, test_size=0.2, random_state=42, stratify=y_disease_only
        )

        # --- Entraînement du RandomForest (multi-output : maladie + spécialiste) ---
        model = RandomForestClassifier(
            n_estimators=300,
            max_depth=20,
            min_samples_split=5,
            min_samples_leaf=2,
            class_weight='balanced',
            random_state=42,
            n_jobs=-1
        )
        model.fit(X_train, Y_train)

        # --- Métriques, rapports, matrices de confusion et feature importances ---
        evaluate_and_report(
            model, X_test, Y_test, le_disease, le_specialist, self.config,
            feature_columns=feature_columns,
        )

        # --- Sauvegarde de tout ce dont l’API a besoin pour l’inférence ---
        print("\n7. Sauvegarde des artefacts...")
        os.makedirs(self.config.MODELS_DIR, exist_ok=True)
        joblib.dump(model, self.config.get_model_path('model'))
        joblib.dump(mlb, self.config.get_model_path('mlb'))
        joblib.dump(scaler, self.config.get_model_path('scaler'))
        joblib.dump(le_disease, self.config.get_model_path('le_disease'))
        joblib.dump(le_specialist, self.config.get_model_path('le_specialist'))
        joblib.dump(feature_columns, self.config.get_model_path('feature_columns'))
        print(f"   - Tous les artefacts sauvegardés dans {self.config.MODELS_DIR}/")
