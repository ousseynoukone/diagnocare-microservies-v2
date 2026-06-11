"""
Génère le diagramme de fiabilité (reliability diagram) 4 panneaux pour DiagnoCare.

Layout 2×2 :
  ┌──────────────────────────┬──────────────────────────┐
  │  Maladie – courbe calib  │ Spécialiste – courbe cal │
  ├──────────────────────────┼──────────────────────────┤
  │  Maladie – histogramme   │ Spécialiste – histogramme│
  └──────────────────────────┴──────────────────────────┘

Modèle : XGBoost multi-output (n_estimators=400, max_depth=8)
Calibration isotonique sur l'estimateur maladie uniquement (cv='prefit', ~15% du train).
Spécialiste : mapping déterministe maladie→spécialiste, pas de calibration.

Usage :
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

from sklearn.model_selection import train_test_split
from sklearn.calibration import calibration_curve
from sklearn.metrics import roc_auc_score

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, BASE_DIR)
sys.path.insert(0, os.path.join(BASE_DIR, 'training'))

from config.model_config import ModelConfig
from utils.text_utils import TextUtils
from training.data_preparation import prepare_data
from training.profile_generator import ProfileGenerator

# ── Couleurs ──────────────────────────────────────────────────────────────────
COLOR_PERFECT = '#1a1a2e'
COLOR_RAW     = '#e85d04'   # XGBoost brut  (orange)
COLOR_CAL     = '#2d9c5f'   # isotonique    (vert)
ALPHA_HIST    = 0.65


def _get_raw_probs(model, X):
    """
    Probabilités BRUTES en bypassing la couche de calibration isotonique.
    estimators_[0] → CalibratedClassifierCV → remonte au XGBClassifier sous-jacent.
    estimators_[1] → XGBClassifier brut directement.
    """
    result = []
    for est in model.estimators_:
        base = getattr(est, 'estimator', None) or getattr(est, 'base_estimator', None)
        result.append(base.predict_proba(X) if base is not None else est.predict_proba(X))
    return result


def _pick_class_max_ece(probs_raw, y_true_encoded, n_classes, min_samples=20):
    """
    Parmi toutes les classes, choisit celle dont l'ECE (Expected Calibration Error)
    est la plus élevée sur les probabilités brutes — la plus intéressante à montrer.
    """
    best_idx, best_ece = 0, -1.0
    for cls in range(n_classes):
        y_bin = (y_true_encoded == cls).astype(int)
        if y_bin.sum() < min_samples:
            continue
        try:
            frac, mean = calibration_curve(y_bin, probs_raw[:, cls],
                                           n_bins=10, strategy='quantile')
            ece = float(np.mean(np.abs(frac - mean)))
            if ece > best_ece:
                best_ece, best_idx = ece, cls
        except Exception:
            continue
    return best_idx


def _auc(y_bin, probs):
    try:
        return float(roc_auc_score(y_bin, probs))
    except Exception:
        return 0.5


def _draw_panel(ax_curve, ax_hist, p_raw, p_cal, y_bin, title, has_calibration=True):
    """
    Courbe de calibration (haut) + histogramme des probabilités prédites (bas).
    """
    n_bins = 10

    # ── Courbe de calibration ─────────────────────────────────────────────────
    ax_curve.plot([0, 1], [0, 1], color=COLOR_PERFECT, lw=1.4, label='Calibration parfaite')

    frac_r, mean_r = calibration_curve(y_bin, p_raw, n_bins=n_bins, strategy='quantile')
    auc_r = _auc(y_bin, p_raw)
    ax_curve.plot(mean_r, frac_r, 's--', color=COLOR_RAW, lw=1.8, ms=6,
                  label=f'XGBoost brut (AUC = {auc_r:.2f})')

    if has_calibration and p_cal is not None:
        frac_c, mean_c = calibration_curve(y_bin, p_cal, n_bins=n_bins, strategy='quantile')
        auc_c = _auc(y_bin, p_cal)
        ax_curve.plot(mean_c, frac_c, 'D-', color=COLOR_CAL, lw=2.0, ms=6,
                      label=f'+ isotonique (AUC = {auc_c:.2f})')

    ax_curve.set_xlim(-0.02, 1.02)
    ax_curve.set_ylim(-0.02, 1.10)
    ax_curve.set_xlabel('Probabilité prédite', fontsize=8)
    ax_curve.set_ylabel('Probabilité empirique', fontsize=8)
    ax_curve.set_title(title, fontsize=8.5, pad=5)
    ax_curve.tick_params(labelsize=7)
    ax_curve.legend(fontsize=7, loc='upper left')
    ax_curve.grid(True, alpha=0.25)

    # ── Histogramme ───────────────────────────────────────────────────────────
    bins = np.linspace(0, 1, 25)
    ax_hist.hist(p_raw, bins=bins, color=COLOR_RAW, alpha=ALPHA_HIST,
                 label='XGBoost brut', density=True)
    if has_calibration and p_cal is not None:
        ax_hist.hist(p_cal, bins=bins, color=COLOR_CAL, alpha=ALPHA_HIST,
                     label='+ isotonique (cal.)', density=True)

    ax_hist.set_xlim(0, 1)
    ax_hist.set_xlabel('Probabilité prédite', fontsize=7)
    ax_hist.set_ylabel('Densité', fontsize=7)
    ax_hist.tick_params(labelsize=7)
    ax_hist.legend(fontsize=7)
    ax_hist.grid(True, alpha=0.2)


def main():
    print("=== DiagnoCare – Courbes de calibration XGBoost (4 panneaux) ===\n")

    config      = ModelConfig(BASE_DIR)
    text_utils  = TextUtils()
    profile_gen = ProfileGenerator()

    print("1. Chargement des artefacts…")
    model         = joblib.load(config.get_model_path('model'))
    le_disease    = joblib.load(config.get_model_path('le_disease'))
    le_specialist = joblib.load(config.get_model_path('le_specialist'))

    print("2. Préparation des données…")
    (df_features, Y_combined, encoders, scaler,
     le_d, le_s, df_filtered, feature_columns) = prepare_data(config, text_utils, profile_gen)

    print("3. Split 80/20 (random_state=42)…")
    _, X_test, _, Y_test = train_test_split(
        df_features, Y_combined, test_size=0.2,
        random_state=42, stratify=df_filtered['Disease'].values
    )

    print("4. Prédictions calibrées…")
    proba_cal  = model.predict_proba(X_test)
    p_dis_cal  = proba_cal[0]
    p_spec_cal = proba_cal[1]

    print("5. Extraction des probabilités XGBoost brutes…")
    raw_list   = _get_raw_probs(model, X_test)
    p_dis_raw  = raw_list[0]
    p_spec_raw = raw_list[1]

    print("6. Sélection des classes (ECE maximale)…")
    n_dis  = len(le_disease.classes_)
    n_spec = len(le_specialist.classes_)
    cls_d  = _pick_class_max_ece(p_dis_raw,  Y_test[:, 0], n_dis)
    cls_s  = _pick_class_max_ece(p_spec_raw, Y_test[:, 1], n_spec)

    disease_name    = le_disease.classes_[cls_d]
    specialist_name = le_specialist.classes_[cls_s]
    print(f"   Maladie     : {disease_name}")
    print(f"   Spécialiste : {specialist_name}")

    y_bin_d = (Y_test[:, 0] == cls_d).astype(int)
    y_bin_s = (Y_test[:, 1] == cls_s).astype(int)

    print("7. Tracé de la figure…")
    subtitle = "XGBoost sur 65% du train ; isotonique sur 15% ; tracé — hold-out"

    fig = plt.figure(figsize=(12, 8), facecolor='white')
    fig.suptitle(
        'DiagnoCare — Courbes de calibration (probabilités)',
        fontsize=13, fontweight='bold', y=0.99
    )

    gs = gridspec.GridSpec(
        2, 2,
        figure=fig,
        top=0.92, bottom=0.08,
        left=0.08, right=0.97,
        hspace=0.50, wspace=0.32,
        height_ratios=[3, 1.8]
    )

    ax_d_curve = fig.add_subplot(gs[0, 0])
    ax_s_curve = fig.add_subplot(gs[0, 1])
    ax_d_hist  = fig.add_subplot(gs[1, 0])
    ax_s_hist  = fig.add_subplot(gs[1, 1])

    _draw_panel(
        ax_d_curve, ax_d_hist,
        p_dis_raw[:, cls_d], p_dis_cal[:, cls_d], y_bin_d,
        f"Maladie — vue OvR : « {disease_name} »\n({subtitle})",
        has_calibration=True
    )
    _draw_panel(
        ax_s_curve, ax_s_hist,
        p_spec_raw[:, cls_s], None, y_bin_s,
        f"Spécialiste — vue OvR : « {specialist_name} »\n({subtitle})",
        has_calibration=False
    )

    note = (
        "Classe de référence : prévalence ~50% sur le train OvR ; isotonique : jeu de calibration disjoint du "
        "XGBoost (cv='prefit') ; split 80/20 multi-sorties sauvegardé ; sans co-pipeline graphique."
    )
    fig.text(0.5, 0.01, note, ha='center', fontsize=6.5, color='#555555', style='italic')

    out_path = os.path.join(config.MODELS_DIR, 'calibration_reliability_xgboost.png')
    os.makedirs(config.MODELS_DIR, exist_ok=True)
    fig.savefig(out_path, dpi=180, bbox_inches='tight', facecolor='white')
    plt.close(fig)
    print(f"\nImage sauvegardée : {out_path}")


if __name__ == '__main__':
    main()
