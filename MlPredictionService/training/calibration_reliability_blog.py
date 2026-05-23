"""
Courbes de calibration type article / sklearn (probabilité prédite vs empirique).

Référence pédagogique : https://blog.octo.com/calibration-de-probabilite
— bins uniformes sur [0, 1], `sklearn.calibration.calibration_curve`,
comparaison RF brut vs **régression isotonique** ajustée sur un sous-jeu calibrage.

Pour un problème multi-classes : vue **one-vs-rest** sur une classe de référence
(prévalence la plus proche de 50 % sur le train pour des courbes lisibles).
RF entraîné sur une partie du train ; isotonique appris sur **le reste** du train
(``cv='prefit'``) ; courbes tracées sur le **hold-out** — évite l'illusion d'une
diagonale « parfaite » due à un calibrage appris sur les mêmes données que le RF.
Le modèle multi-sortie sauvegardé en prod n'est pas modifié.
"""
from __future__ import annotations

import os
from typing import Any

import matplotlib.pyplot as plt
import numpy as np
from sklearn.calibration import calibration_curve
from sklearn.ensemble import RandomForestClassifier
from sklearn.isotonic import IsotonicRegression
from sklearn.metrics import roc_auc_score
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder

# Bins uniformes [0,1] en 10 tranches → largeur 0,1 (aligné sur la lecture pédagogique).
N_BINS_UNIFORM_01 = 10


def _pick_reference_class_index(y_train: np.ndarray) -> int:
    """Classe présente dont la fréquence sur le train est la plus proche de 50 % (OvR équilibré)."""
    y_train = np.asarray(y_train).ravel()
    counts = np.bincount(y_train)
    present = np.where(counts > 0)[0]
    if present.size == 0:
        return 0
    props = counts[present].astype(np.float64) / max(len(y_train), 1)
    return int(present[np.argmin(np.abs(props - 0.5))])


def _plot_one_head(
    ax_rel: Any,
    ax_hist: Any,
    X_train: Any,
    y_train: np.ndarray,
    X_test: Any,
    y_test: np.ndarray,
    rf_params: dict,
    le: LabelEncoder,
    head_name: str,
    *,
    calib_fraction: float = 0.35,
    n_bins_curve: int = N_BINS_UNIFORM_01,
) -> None:
    y_train = np.asarray(y_train).ravel()
    y_test = np.asarray(y_test).ravel()
    k = _pick_reference_class_index(y_train)
    y_bin = (y_test == k).astype(int)
    class_name = str(le.inverse_transform([k])[0])

    cnt = np.bincount(y_train)
    u = np.unique(y_train)
    strat = y_train if u.size > 1 and all(int(cnt[int(i)]) >= 2 for i in u) else None
    X_tf, X_cal, y_tf, y_cal = train_test_split(
        X_train,
        y_train,
        test_size=calib_fraction,
        random_state=42,
        stratify=strat,
    )

    rf = RandomForestClassifier(**rf_params)
    rf.fit(X_tf, y_tf)

    p_raw = rf.predict_proba(X_test)[:, k]
    p_cal_fold = rf.predict_proba(X_cal)[:, k]
    y_bin_cal = (y_cal == k).astype(np.float64)
    if float(y_bin_cal.min()) == float(y_bin_cal.max()):
        p_cal = p_raw.copy()
    else:
        ir = IsotonicRegression(out_of_bounds="clip")
        ir.fit(p_cal_fold, y_bin_cal)
        p_cal = np.clip(ir.predict(rf.predict_proba(X_test)[:, k]), 0.0, 1.0)

    prob_true_raw, mean_pred_raw = calibration_curve(
        y_bin, p_raw, n_bins=n_bins_curve, strategy="uniform"
    )
    prob_true_cal, mean_pred_cal = calibration_curve(
        y_bin, p_cal, n_bins=n_bins_curve, strategy="uniform"
    )
    try:
        auc_raw = float(roc_auc_score(y_bin, p_raw))
        auc_cal = float(roc_auc_score(y_bin, p_cal))
    except ValueError:
        auc_raw = float("nan")
        auc_cal = float("nan")

    ax_rel.plot(
        [0, 1],
        [0, 1],
        linestyle="--",
        color="#C9A227",
        linewidth=1.4,
        label="Calibration parfaite (honnêteté = diagonale)",
        zorder=1,
    )
    ax_rel.plot(
        mean_pred_raw,
        prob_true_raw,
        "s-",
        color="tab:blue",
        linewidth=1.5,
        markersize=5,
        label=f"Forêt aléatoire brut (AUC = {auc_raw:.2f})",
    )
    ax_rel.plot(
        mean_pred_cal,
        prob_true_cal,
        "o-",
        color="tab:green",
        linewidth=1.5,
        markersize=5,
        label=f"+ isotonique (AUC = {auc_cal:.2f})",
    )
    ax_rel.set_xlabel("Confiance moyenne déclarée (probabilité prédite, par bin de 0,1)")
    ax_rel.set_ylabel("Proportion empirique (fraction de positifs OvR dans la tranche)")
    ax_rel.set_title(
        f"{head_name} — vue OvR : « {class_name} »\n"
        f"(RF sur {1 - calib_fraction:.0%} du train ; isotonique sur {calib_fraction:.0%} ; tracé = hold-out)"
    )
    ax_rel.legend(loc="lower right", fontsize=8)
    ax_rel.grid(True, alpha=0.3)
    ax_rel.set_xlim(0, 1)
    ax_rel.set_ylim(0, 1)

    ax_hist.hist(
        p_raw,
        bins=N_BINS_UNIFORM_01,
        range=(0, 1),
        alpha=0.55,
        color="tab:blue",
        label="RF brut",
        density=True,
    )
    ax_hist.hist(
        p_cal,
        bins=N_BINS_UNIFORM_01,
        range=(0, 1),
        alpha=0.55,
        color="tab:green",
        label="+ isotonique (cal)",
        density=True,
    )
    ax_hist.set_xlabel("Probabilité prédite (classe de référence)")
    ax_hist.set_ylabel("Densité")
    ax_hist.legend(loc="upper right", fontsize=8)
    ax_hist.set_xlim(0, 1)


def save_blog_style_calibration_figure(
    X_train: Any,
    Y_train: np.ndarray,
    X_test: Any,
    Y_test: np.ndarray,
    le_disease: LabelEncoder,
    le_specialist: LabelEncoder,
    rf_params: dict,
    output_path: str,
    title: str = "DiagnoCare — Courbes de calibration (probabilités)",
) -> None:
    """
    Figure 2×2 : pour Maladie et Spécialiste, courbe de fiabilité + histogramme.

    Le modèle produ reste celui entraîné dans ``train_model`` (multi-sortie) ;
    cette figure entraîne des RF mono-sortie **uniquement** pour la comparaison
    brut vs isotonique sur la vue OvR décrite dans le module.
    """
    os.makedirs(os.path.dirname(output_path) or ".", exist_ok=True)

    fig, axes = plt.subplots(2, 2, figsize=(12, 9))
    fig.suptitle(title, fontsize=12, fontweight="bold", y=0.98)

    _plot_one_head(
        axes[0, 0],
        axes[1, 0],
        X_train,
        Y_train[:, 0],
        X_test,
        Y_test[:, 0],
        rf_params,
        le_disease,
        "Maladie",
    )
    _plot_one_head(
        axes[0, 1],
        axes[1, 1],
        X_train,
        Y_train[:, 1],
        X_test,
        Y_test[:, 1],
        rf_params,
        le_specialist,
        "Spécialiste",
    )
    fig.tight_layout(rect=[0, 0.02, 1, 0.96])
    fig.savefig(output_path, dpi=150, bbox_inches="tight")
    plt.close(fig)
