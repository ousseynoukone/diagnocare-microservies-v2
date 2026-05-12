"""
Orchestration de l'entrainement : preparation des donnees, entrainement du modele,
evaluation, puis sauvegarde des artefacts (.joblib + noms des colonnes).

Utilise XGBoost au lieu de RandomForest :
  - Gere nativement les valeurs "0" comme des donnees manquantes
    (quand un patient ne mentionne que 3 symptomes sur 131, les 128 autres
    ne sont pas "absents" mais "inconnus" — XGBoost fait la difference)
  - Meilleur gradient boosting => predictions plus fines

Calibration des probas (par estimateur, cv=3 isotonique) :
  - Un RF/XGBoost qui dit "90%" ne veut pas dire "probabilite medicale de 90%"
  - La calibration aligne la confiance du modele sur la realite observee
"""
import os
import sys
import joblib
import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.calibration import CalibratedClassifierCV
from sklearn.frozen import FrozenEstimator
from sklearn.multioutput import MultiOutputClassifier
from xgboost import XGBClassifier

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from config.model_config import ModelConfig
from utils.text_utils import TextUtils

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from profile_generator import ProfileGenerator
from data_preparation import prepare_data
from evaluation import evaluate_and_report


class ModelTrainer:

    def __init__(self, config: ModelConfig = None):
        if config is None:
            base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
            config = ModelConfig(base_dir=base_dir)
        self.config = config
        self.text_utils = TextUtils()
        self.profile_generator = ProfileGenerator()

    def train(self) -> None:
        (
            df_features,
            Y_combined,
            encoders,
            scaler,
            le_disease,
            le_specialist,
            df_filtered,
            feature_columns,
        ) = prepare_data(self.config, self.text_utils, self.profile_generator)

        mlb, tfidf = encoders

        # --- Split stratifie 80/20 ---
        print("\n9. Split des donnees (80/20 : train / test)...")
        y_disease_only = df_filtered['Disease'].values

        X_train, X_test, Y_train, Y_test = train_test_split(
            df_features, Y_combined, test_size=0.2, random_state=42, stratify=y_disease_only
        )
        print(f"   - Train: {len(X_train)}, Test: {len(X_test)}")

        # --- XGBoost multi-output ---
        print("\n10. Entrainement XGBoost multi-output...")
        base_xgb = XGBClassifier(
            n_estimators=400,
            max_depth=8,
            learning_rate=0.1,
            subsample=0.8,
            colsample_bytree=0.8,
            min_child_weight=3,
            reg_alpha=0.1,
            reg_lambda=1.0,
            tree_method='hist',
            random_state=42,
            n_jobs=-1,
            verbosity=0,
        )

        # --- Split train / calibration ---
        # La calibration isotonique DOIT se faire sur des donnees que le modele
        # n'a PAS vues pendant l'entrainement. Sinon l'isotonique memorise X_train
        # (regression monotone non-parametrique) => AUC interne = 1.0 (leakage).
        # On utilise cv='prefit' : le XGBoost est entraine sur X_tr, puis l'isotonique
        # est ajuste sur X_cal (20% du train), puis evalue sur X_test (totalement separe).
        print("   - Split train/calibration (80/20 interne pour eviter le leakage isotonique)...")
        X_tr, X_cal, Y_tr, Y_cal = train_test_split(
            X_train, Y_train, test_size=0.2, random_state=0,
            stratify=Y_train[:, 0]
        )
        print(f"   - Train effectif: {len(X_tr)}, Calibration: {len(X_cal)}")

        model = MultiOutputClassifier(base_xgb, n_jobs=1)
        model.fit(X_tr, Y_tr)

        # --- Feature importances (extraites du modele brut, avant calibration) ---
        raw_importances = np.zeros(len(feature_columns))
        for est in model.estimators_:
            raw_importances += est.feature_importances_
        raw_importances /= len(model.estimators_)

        # --- Calibration de chaque sous-estimateur ---
        # Dans scikit-learn moderne (1.6+), pour calibrer un estimateur pre-entraine sans
        # le re-entrainer ni causer de fuite de donnees (leakage), on l'enveloppe dans un
        # FrozenEstimator. La calibration isotonique s'ajuste alors uniquement sur X_cal.
        print("   - Calibration isotonique sur split dedie via FrozenEstimator...")
        for i, est in enumerate(model.estimators_):
            cal = CalibratedClassifierCV(FrozenEstimator(est), method='isotonic')
            cal.fit(X_cal, Y_cal[:, i])
            model.estimators_[i] = cal

        # --- Evaluation sur le jeu de test ---
        evaluate_and_report(
            model, X_test, Y_test, le_disease, le_specialist, self.config,
            feature_columns=feature_columns,
            precomputed_importances=raw_importances,
        )

        # --- Sauvegarde ---
        print("\n11. Sauvegarde des artefacts...")
        os.makedirs(self.config.MODELS_DIR, exist_ok=True)
        joblib.dump(model, self.config.get_model_path('model'))
        joblib.dump(mlb, self.config.get_model_path('mlb'))
        joblib.dump(tfidf, self.config.get_model_path('tfidf'))
        joblib.dump(scaler, self.config.get_model_path('scaler'))
        joblib.dump(le_disease, self.config.get_model_path('le_disease'))
        joblib.dump(le_specialist, self.config.get_model_path('le_specialist'))
        joblib.dump(feature_columns, self.config.get_model_path('feature_columns'))
        print(f"   - Tous les artefacts sauvegardes dans {self.config.MODELS_DIR}/")
