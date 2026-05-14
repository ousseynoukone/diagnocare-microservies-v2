"""
Courbes de calibration ciblées sur des maladies CONFUSES
(symptômes récurrents / partagés entre maladies).

Objectif : démontrer l'impact réel de la calibration isotonique
sur des cas difficiles (Hepatitis D, Hepatitis E, Pneumonia, Dengue…).

Usage (depuis MlPredictionService/) :
    python generate_calibration_confusing.py

Sortie : models/calibration_confusing_diseases.png
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
from sklearn.metrics import roc_auc_score, brier_score_loss

# ---- Chemins ----
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, BASE_DIR)
sys.path.insert(0, os.path.join(BASE_DIR, 'training'))

from config.model_config import ModelConfig
from utils.text_utils import TextUtils
from training.data_preparation import prepare_data
from training.profile_generator import ProfileGenerator

# ---- Maladies confuses ciblées ----
# Sélectionnées parce qu'elles partagent beaucoup de symptômes (Jaccard élevé)
# et sont donc les plus susceptibles de bénéficier de la calibration.
CONFUSING_DISEASES = [
    'Hepatitis D',       # Jaccard 0.69 avec Hepatitis E, 0.60 avec Chronic Cholestasis
    'Hepatitis E',       # Jaccard 0.69 avec Hepatitis D, 0.50 avec Hepatitis A
    'Pneumonia',         # Jaccard 0.50 avec Tuberculosis
    'Dengue',            # Jaccard 0.39 avec Chicken Pox, 0.375 avec Malaria
]

# ---- Couleurs ----
COLOR_PERFECT   = '#1a1a2e'
COLOR_RAW       = '#e85d04'
COLOR_CAL       = '#2d9c5f'
ALPHA_HIST      = 0.65


def _get_uncalibrated_probs(model, X):
    """
    Extrait les proba BRUTES en contournant la couche de calibration.
    Le modèle est un MultiOutputClassifier dont les estimators_ sont des
    CalibratedClassifierCV(XGBClassifier, method='isotonic').
    """
    raw_probs_list = []
    for est_cal in model.estimators_:
        base = getattr(est_cal, 'estimator', None) or getattr(est_cal, 'base_estimator', None)
        if base is None:
            raw_probs_list.append(est_cal.predict_proba(X))
        else:
            raw_probs_list.append(base.predict_proba(X))
    return raw_probs_list


def _compute_ece(y_true, y_prob, n_bins=10):
    """Expected Calibration Error."""
    try:
        frac_pos, mean_pred = calibration_curve(y_true, y_prob, n_bins=n_bins, strategy='quantile')
        # Pondérer par la proportion d'échantillons dans chaque bin
        bin_counts = np.histogram(y_prob, bins=n_bins, range=(0, 1))[0]
        total = bin_counts.sum()
        if total == 0:
            return 0.0
        # ECE simplifié (non pondéré car calibration_curve gère déjà le binning)
        return float(np.mean(np.abs(frac_pos - mean_pred)))
    except Exception:
        return float('nan')


def _auc_score(y_true, probs):
    """Calcule le score ROC AUC (binaire)."""
    try:
        return float(roc_auc_score(y_true, probs))
    except Exception:
        return 0.5


def _plot_panel(ax_curve, ax_hist, probs_raw, probs_cal, y_bin,
                disease_name, n_bins=10):
    """
    Dessine un panneau : courbe de calibration + histogramme de distribution.
    """
    # -- Calibration curves --
    frac_raw, mean_raw = calibration_curve(y_bin, probs_raw, n_bins=n_bins, strategy='quantile')
    frac_cal, mean_cal = calibration_curve(y_bin, probs_cal, n_bins=n_bins, strategy='quantile')

    auc_raw = _auc_score(y_bin, probs_raw)
    auc_cal = _auc_score(y_bin, probs_cal)
    ece_raw = _compute_ece(y_bin, probs_raw, n_bins)
    ece_cal = _compute_ece(y_bin, probs_cal, n_bins)
    brier_raw = brier_score_loss(y_bin, probs_raw)
    brier_cal = brier_score_loss(y_bin, probs_cal)

    # Diagonale parfaite
    ax_curve.plot([0, 1], [0, 1], 'k-', lw=1.2, label='Calibration parfaite', alpha=0.6)

    # XGBoost brut
    ax_curve.plot(mean_raw, frac_raw, 's--', color=COLOR_RAW, lw=1.6, ms=5,
                  label=f'XGBoost brut (ECE={ece_raw:.3f})')

    # Isotonique calibré
    ax_curve.plot(mean_cal, frac_cal, 'D-', color=COLOR_CAL, lw=2.0, ms=5,
                  label=f'+ isotonique (ECE={ece_cal:.3f})')

    ax_curve.set_xlim(-0.02, 1.02)
    ax_curve.set_ylim(-0.02, 1.10)
    ax_curve.set_xlabel('Probabilité prédite', fontsize=7.5)
    ax_curve.set_ylabel('Probabilité empirique', fontsize=7.5)

    # Compter les positifs
    n_pos = y_bin.sum()
    n_total = len(y_bin)
    ax_curve.set_title(
        f'{disease_name} -- OvR\n'
        f'(n+={n_pos}/{n_total}, AUC brut={auc_raw:.3f}, '
        f'Brier: {brier_raw:.4f} -> {brier_cal:.4f})',
        fontsize=7.5, pad=4
    )
    ax_curve.tick_params(labelsize=6.5)
    ax_curve.legend(fontsize=6.5, loc='upper left')
    ax_curve.grid(True, alpha=0.25)

    # -- Histogramme --
    bins = np.linspace(0, 1, 21)
    ax_hist.hist(probs_raw, bins=bins, color=COLOR_RAW, alpha=ALPHA_HIST,
                 label='XGBoost brut', density=True)
    ax_hist.hist(probs_cal, bins=bins, color=COLOR_CAL, alpha=ALPHA_HIST,
                 label='+ isotonique', density=True)
    ax_hist.set_xlabel('Probabilité prédite', fontsize=6.5)
    ax_hist.set_ylabel('Densité', fontsize=6.5)
    ax_hist.tick_params(labelsize=6)
    ax_hist.legend(fontsize=6)
    ax_hist.grid(True, alpha=0.2)

    return {
        'disease': disease_name,
        'auc_raw': auc_raw, 'auc_cal': auc_cal,
        'ece_raw': ece_raw, 'ece_cal': ece_cal,
        'brier_raw': brier_raw, 'brier_cal': brier_cal,
        'n_pos': n_pos, 'n_total': n_total,
    }


def main():
    print("=" * 65)
    print("  Calibration ciblée sur maladies CONFUSES (symptômes partagés)")
    print("=" * 65)
    print()

    config = ModelConfig(BASE_DIR)
    text_utils = TextUtils()
    profile_gen = ProfileGenerator()

    # ---- 1. Chargement ----
    print("1. Chargement des artefacts…")
    model          = joblib.load(config.get_model_path('model'))
    le_disease     = joblib.load(config.get_model_path('le_disease'))
    le_specialist  = joblib.load(config.get_model_path('le_specialist'))

    print(f"   Classes maladie connues: {list(le_disease.classes_)}")

    # ---- 2. Données ----
    print("\n2. Préparation des données…")
    (df_features, Y_combined, encoders, scaler,
     le_d, le_s, df_filtered, feature_columns) = prepare_data(config, text_utils, profile_gen)

    # ---- 3. Split ----
    print("\n3. Split 80/20…")
    y_disease_only = df_filtered['Disease'].values
    _, X_test, _, Y_test = train_test_split(
        df_features, Y_combined, test_size=0.2, random_state=42, stratify=y_disease_only
    )

    # ---- 4. Probabilités ----
    print("\n4. Prédictions calibrées + brutes…")
    proba_list_cal = model.predict_proba(X_test)
    probs_disease_cal = proba_list_cal[0]

    raw_list = _get_uncalibrated_probs(model, X_test)
    probs_disease_raw = raw_list[0]

    # ---- 5. Trouver les indices des maladies cibles ----
    print("\n5. Recherche des maladies cibles…")
    available_classes = list(le_disease.classes_)
    targets = []

    for disease_name in CONFUSING_DISEASES:
        # Chercher la maladie (insensible à la casse, en .title())
        disease_title = disease_name.strip().title()
        if disease_title in available_classes:
            cls_idx = available_classes.index(disease_title)
            y_bin = (Y_test[:, 0] == cls_idx).astype(int)
            if y_bin.sum() >= 5:  # Au moins 5 positifs
                targets.append((disease_title, cls_idx))
                print(f"   [OK] {disease_title} (idx={cls_idx}, n+={y_bin.sum()})")
            else:
                print(f"   [--] {disease_title} -- pas assez d'exemples positifs ({y_bin.sum()})")
        else:
            print(f"   [--] {disease_name} -- NON TROUVEE parmi: {available_classes[:5]}...")

    if len(targets) < 2:
        print("\nERREUR : pas assez de maladies cibles trouvées. Fallback sur ECE worst.")
        # Fallback : prendre les 4 maladies avec le pire ECE
        ece_list = []
        for cls_idx in range(probs_disease_raw.shape[1]):
            y_bin = (Y_test[:, 0] == cls_idx).astype(int)
            if y_bin.sum() < 5:
                continue
            ece = _compute_ece(y_bin, probs_disease_raw[:, cls_idx])
            ece_list.append((available_classes[cls_idx], cls_idx, ece))
        ece_list.sort(key=lambda x: x[2], reverse=True)
        targets = [(name, idx) for name, idx, _ in ece_list[:4]]

    n_panels = len(targets)
    if n_panels > 4:
        targets = targets[:4]
        n_panels = 4

    # ---- 6. Figure ----
    print(f"\n6. Tracé de la figure ({n_panels} maladies)…")

    # Layout: 2 lignes par maladie (courbe + histogramme), 2 colonnes
    n_cols = 2
    n_rows = (n_panels + 1) // 2  # Nombre de paires

    fig = plt.figure(figsize=(14, 5 * n_rows + 2), facecolor='white')
    fig.suptitle(
        'DiagnoCare -- Calibration sur maladies a symptomes partages',
        fontsize=14, fontweight='bold', y=0.98
    )

    subtitle = (
        "Maladies selectionnees pour leur forte similarite symptomatique (Jaccard >= 0.37)\n"
        "XGBoost multi-output (n_estimators=400, max_depth=8) ; "
        "calibration isotonique sur 35% ; tracé = hold-out 20%"
    )
    fig.text(0.5, 0.955, subtitle, ha='center', fontsize=8.5, color='#444444', style='italic')

    # Créer une grille : pour chaque maladie, 2 sous-axes (courbe, histogramme)
    gs = gridspec.GridSpec(
        n_rows * 2, n_cols,
        figure=fig,
        top=0.92, bottom=0.08,
        left=0.07, right=0.97,
        hspace=0.55, wspace=0.30,
        height_ratios=[3, 1.5] * n_rows
    )

    results = []
    for i, (disease_name, cls_idx) in enumerate(targets):
        col = i % n_cols
        row_pair = i // n_cols

        ax_curve = fig.add_subplot(gs[row_pair * 2, col])
        ax_hist  = fig.add_subplot(gs[row_pair * 2 + 1, col])

        y_bin = (Y_test[:, 0] == cls_idx).astype(int)

        res = _plot_panel(
            ax_curve, ax_hist,
            probs_disease_raw[:, cls_idx],
            probs_disease_cal[:, cls_idx],
            y_bin,
            disease_name
        )
        results.append(res)

    # ---- 7. Tableau récapitulatif ----
    print("\n7. Résultats de calibration :")
    print(f"{'Maladie':<22} {'ECE brut':>10} {'ECE cal.':>10} {'dECE':>10} "
          f"{'Brier brut':>12} {'Brier cal.':>12} {'dBrier':>10} "
          f"{'AUC brut':>10} {'AUC cal.':>10}")
    print("-" * 120)
    for r in results:
        delta_ece = r['ece_raw'] - r['ece_cal']
        delta_brier = r['brier_raw'] - r['brier_cal']
        print(f"{r['disease']:<22} {r['ece_raw']:>10.4f} {r['ece_cal']:>10.4f} {delta_ece:>+10.4f} "
              f"{r['brier_raw']:>12.6f} {r['brier_cal']:>12.6f} {delta_brier:>+10.6f} "
              f"{r['auc_raw']:>10.4f} {r['auc_cal']:>10.4f}")

    # Note de bas de page
    note = (
        "ECE = Expected Calibration Error (plus bas = mieux) ; "
        "Brier score = erreur quadratique des probabilités (plus bas = mieux) ; "
        "Maladies choisies pour leur chevauchement symptomatique élevé (Jaccard > 0.37)."
    )
    fig.text(0.5, 0.01, note, ha='center', fontsize=6.5, color='#555555', style='italic')

    # ---- 8. Sauvegarde ----
    out_path = os.path.join(config.MODELS_DIR, 'calibration_confusing_diseases.png')
    os.makedirs(config.MODELS_DIR, exist_ok=True)
    fig.savefig(out_path, dpi=180, bbox_inches='tight', facecolor='white')
    plt.close(fig)

    print(f"\n[OK] Image sauvegardee : {out_path}")
    print("   -> Utilisez cette figure dans votre memoire pour illustrer l'apport de la calibration.")


if __name__ == '__main__':
    main()
