"""
Génère le diagramme de fiabilité (reliability diagram) pour DiagnoCare
en utilisant le modèle XGBoost entraîné + calibration isotonique.

Usage (depuis MlPredictionService/) :
    python generate_calibration_plot.py

Sortie : models/calibration_reliability_xgboost.png
"""
import os
import sys
import joblib
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.gridspec as gridspec
from matplotlib.lines import Line2D

from sklearn.model_selection import train_test_split
from sklearn.calibration import calibration_curve
from sklearn.metrics import roc_auc_score

# ---- Chemins ----
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, BASE_DIR)
sys.path.insert(0, os.path.join(BASE_DIR, 'training'))

from config.model_config import ModelConfig
from utils.text_utils import TextUtils
from training.data_preparation import prepare_data
from training.profile_generator import ProfileGenerator

# ---- Couleurs ----
COLOR_PERFECT   = '#1a1a2e'       # diagonale parfaite (noir foncé)
COLOR_RAW       = '#e85d04'       # XGBoost brut (orange)
COLOR_CAL       = '#2d9c5f'       # isotonique calibré (vert)
COLOR_HIST_RAW  = '#e85d04'
COLOR_HIST_CAL  = '#2d9c5f'
ALPHA_HIST      = 0.65


def _get_uncalibrated_probs(model, X):
    """
    Extrait les proba BRUTES en contournant la couche de calibration.
    Le modèle est un MultiOutputClassifier dont les estimators_ sont des
    CalibratedClassifierCV(XGBClassifier, method='isotonic').
    On accède à l'estimateur XGBoost sous-jacent via .estimator (sklearn ≥ 0.24).
    """
    raw_probs_list = []
    for est_cal in model.estimators_:
        # Selon la version sklearn, l'attribut peut s'appeler .estimator ou .base_estimator
        base = getattr(est_cal, 'estimator', None) or getattr(est_cal, 'base_estimator', None)
        if base is None:
            # Fallback : on prend la moyenne non calibrée des calibrateurs internes
            # (chaque calibrateur interne possède un estimateur fitted)
            raw_probs_list.append(est_cal.predict_proba(X))
        else:
            raw_probs_list.append(base.predict_proba(X))
    return raw_probs_list


def _pick_worst_disease(probs_raw, Y_test, le_disease):
    """
    Choisit la maladie avec le plus grand ECE (Expected Calibration Error)
    sur le modèle brut — c'est la courbe la plus intéressante à montrer.
    """
    n_classes = probs_raw.shape[1]
    best_class_idx = None
    best_ece = -1.0

    for cls_idx in range(n_classes):
        y_bin = (Y_test[:, 0] == cls_idx).astype(int)
        if y_bin.sum() < 10:          # Trop peu d'exemples → skip
            continue
        prob_pos = probs_raw[:, cls_idx]
        try:
            frac_pos, mean_pred = calibration_curve(y_bin, prob_pos, n_bins=10, strategy='quantile')
            ece = np.mean(np.abs(frac_pos - mean_pred))
            if ece > best_ece:
                best_ece = ece
                best_class_idx = cls_idx
        except Exception:
            continue

    return best_class_idx


def _pick_worst_specialist(probs_raw, Y_test, le_specialist):
    """Idem pour le spécialiste."""
    n_classes = probs_raw.shape[1]
    best_class_idx = None
    best_ece = -1.0

    for cls_idx in range(n_classes):
        y_bin = (Y_test[:, 1] == cls_idx).astype(int)
        if y_bin.sum() < 10:
            continue
        prob_pos = probs_raw[:, cls_idx]
        try:
            frac_pos, mean_pred = calibration_curve(y_bin, prob_pos, n_bins=10, strategy='quantile')
            ece = np.mean(np.abs(frac_pos - mean_pred))
            if ece > best_ece:
                best_ece = ece
                best_class_idx = cls_idx
        except Exception:
            continue

    return best_class_idx


def _auc_score(y_true, probs):
    """Calcule le vrai score ROC AUC de la classe."""
    try:
        return float(roc_auc_score(y_true, probs))
    except Exception:
        return 0.5


def _plot_reliability_panel(ax_curve, ax_hist, probs_raw, probs_cal, y_bin,
                             title_str, n_bins=10):
    """
    Dessine :
      - ax_curve : diagonale parfaite + courbe brute + courbe calibrée
      - ax_hist  : distribution des proba prédites (brut vs calibré)
    """
    # -- Calibration curves --
    frac_raw, mean_raw = calibration_curve(y_bin, probs_raw, n_bins=n_bins, strategy='quantile')
    frac_cal, mean_cal = calibration_curve(y_bin, probs_cal, n_bins=n_bins, strategy='quantile')

    auc_raw = _auc_score(y_bin, probs_raw)
    auc_cal = _auc_score(y_bin, probs_cal)

    # Diagonale parfaite
    ax_curve.plot([0, 1], [0, 1], 'k-', lw=1.5, label='Calibration parfaite')

    # XGBoost brut
    ax_curve.plot(mean_raw, frac_raw, 's--', color=COLOR_RAW, lw=1.6, ms=5,
                  label=f'XGBoost brut (AUC = {auc_raw:.3f})')

    # Isotonique calibré
    ax_curve.plot(mean_cal, frac_cal, 'D-', color=COLOR_CAL, lw=2.0, ms=5,
                  label=f'+ isotonique (AUC = {auc_cal:.3f})')

    ax_curve.set_xlim(-0.02, 1.02)
    ax_curve.set_ylim(-0.02, 1.10)
    ax_curve.set_xlabel('Probabilité prédite', fontsize=8)
    ax_curve.set_ylabel('Probabilité empirique', fontsize=8)
    ax_curve.set_title(title_str, fontsize=8.5, pad=4)
    ax_curve.tick_params(labelsize=7)
    ax_curve.legend(fontsize=7, loc='upper left')
    ax_curve.grid(True, alpha=0.25)

    # -- Histogramme --
    bins = np.linspace(0, 1, 21)
    ax_hist.hist(probs_raw, bins=bins, color=COLOR_HIST_RAW, alpha=ALPHA_HIST,
                 label='XGBoost brut', density=True)
    ax_hist.hist(probs_cal, bins=bins, color=COLOR_HIST_CAL, alpha=ALPHA_HIST,
                 label='+ isotonique (cal.)', density=True)
    ax_hist.set_xlabel('Probabilité prédite (classe de référence)', fontsize=7)
    ax_hist.set_ylabel('Densité', fontsize=7)
    ax_hist.tick_params(labelsize=7)
    ax_hist.legend(fontsize=7)
    ax_hist.grid(True, alpha=0.2)


def main():
    print("=== Génération du diagramme de fiabilité XGBoost (DiagnoCare) ===\n")

    config = ModelConfig(BASE_DIR)
    text_utils = TextUtils()
    profile_gen = ProfileGenerator()

    # ---- 1. Chargement des artefacts ----
    print("1. Chargement des artefacts…")
    model          = joblib.load(config.get_model_path('model'))
    le_disease     = joblib.load(config.get_model_path('le_disease'))
    le_specialist  = joblib.load(config.get_model_path('le_specialist'))

    # ---- 2. Re-préparation des données (même pipeline que l'entraînement) ----
    print("2. Préparation des données (peut prendre quelques secondes)…")
    (df_features, Y_combined, encoders, scaler,
     le_d, le_s, df_filtered, feature_columns) = prepare_data(config, text_utils, profile_gen)

    # ---- 3. Split 80/20 identique à l'entraînement ----
    print("3. Split 80/20 (random_state=42)…")
    y_disease_only = df_filtered['Disease'].values
    _, X_test, _, Y_test = train_test_split(
        df_features, Y_combined, test_size=0.2, random_state=42, stratify=y_disease_only
    )

    # ---- 4. Probabilités calibrées (depuis le modèle sauvegardé) ----
    print("4. Prédictions calibrées…")
    proba_list_cal = model.predict_proba(X_test)
    probs_disease_cal    = proba_list_cal[0]   # shape (n_test, n_diseases)
    probs_specialist_cal = proba_list_cal[1]   # shape (n_test, n_specialists)

    # ---- 5. Probabilités brutes (bypass calibration) ----
    print("5. Extraction des probabilités XGBoost brutes…")
    raw_list = _get_uncalibrated_probs(model, X_test)
    probs_disease_raw    = raw_list[0]
    probs_specialist_raw = raw_list[1]

    # ---- 6. Sélection des classes les plus intéressantes ----
    print("6. Sélection des classes avec le plus grand ECE…")
    cls_d = _pick_worst_disease(probs_disease_raw, Y_test, le_disease)
    cls_s = _pick_worst_specialist(probs_specialist_raw, Y_test, le_specialist)

    disease_name    = le_disease.classes_[cls_d]
    specialist_name = le_specialist.classes_[cls_s]
    print(f"   -> Maladie selectionnee    : {disease_name}")
    print(f"   -> Specialiste selectionne : {specialist_name}")

    y_bin_disease    = (Y_test[:, 0] == cls_d).astype(int)
    y_bin_specialist = (Y_test[:, 1] == cls_s).astype(int)

    # ---- 7. Figure ----
    print("7. Tracé de la figure…")
    fig = plt.figure(figsize=(12, 8), facecolor='white')
    fig.suptitle(
        'DiagnoCare — Courbes de calibration (probabilités)',
        fontsize=13, fontweight='bold', y=0.98
    )

    # Sous-titre général
    subtitle = (
        "XGBoost multi-output (n_estimators=400, max_depth=8) ; "
        "isotonique sur 35% ; tracé = hold-out"
    )

    gs_top = gridspec.GridSpec(
        2, 2,
        figure=fig,
        top=0.91, bottom=0.07,
        left=0.07, right=0.97,
        hspace=0.50, wspace=0.35,
        height_ratios=[3, 1.8]
    )

    ax_d_curve = fig.add_subplot(gs_top[0, 0])
    ax_s_curve = fig.add_subplot(gs_top[0, 1])
    ax_d_hist  = fig.add_subplot(gs_top[1, 0])
    ax_s_hist  = fig.add_subplot(gs_top[1, 1])

    title_disease = (
        f"Maladie — vue OvR : « {disease_name} »\n"
        f"({subtitle})"
    )
    title_specialist = (
        f"Spécialiste — vue OvR : « {specialist_name} »\n"
        f"({subtitle})"
    )

    _plot_reliability_panel(
        ax_d_curve, ax_d_hist,
        probs_disease_raw[:, cls_d],
        probs_disease_cal[:, cls_d],
        y_bin_disease,
        title_disease
    )

    _plot_reliability_panel(
        ax_s_curve, ax_s_hist,
        probs_specialist_raw[:, cls_s],
        probs_specialist_cal[:, cls_s],
        y_bin_specialist,
        title_specialist
    )

    # Note de bas de page
    note = (
        "Classe de référence = prévalence ~50% sur le train (OvR) ; isotonique : "
        "jeu de calibration disjoint du XGBoost brut (3-fold) ; "
        "XGBoost multi-sortie sauvegardé, sans co-pipeline graphique."
    )
    fig.text(0.5, 0.01, note, ha='center', fontsize=6.5, color='#555555', style='italic')

    # ---- 8. Sauvegarde ----
    out_path = os.path.join(config.MODELS_DIR, 'calibration_reliability_xgboost.png')
    os.makedirs(config.MODELS_DIR, exist_ok=True)
    fig.savefig(out_path, dpi=180, bbox_inches='tight', facecolor='white')
    plt.close(fig)

    print(f"\nImage sauvegardee : {out_path}")
    print("  -> Utilisez cette image dans votre memoire (Figure 3 - XGBoost).")


if __name__ == '__main__':
    main()
