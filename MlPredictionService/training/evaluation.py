"""
Évaluation du modèle après entraînement :
  - accuracy top-1 et top-k
  - rapports de classification par classe (precision, recall, F1)
  - matrices de confusion (texte + PNG)
  - feature importances (pour détecter si le modèle se base trop sur l'âge vs les symptômes)
"""
import os
import sys
import numpy as np
from sklearn.metrics import accuracy_score, confusion_matrix, classification_report

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from config.model_config import ModelConfig


def evaluate_and_report(
    model,
    X_test,
    Y_test: np.ndarray,
    le_disease,
    le_specialist,
    config: ModelConfig,
    feature_columns: list = None,
) -> None:
    """
    Affiche les métriques, rapports, matrices de confusion et feature importances.
    """
    Y_pred = model.predict(X_test)
    n_test = Y_test.shape[0]

    # --- Accuracy top-1 ---
    acc_disease = accuracy_score(Y_test[:, 0], Y_pred[:, 0])
    acc_specialist = accuracy_score(Y_test[:, 1], Y_pred[:, 1])
    print(f"   - Précision Maladie (top-1):     {acc_disease*100:.2f}%")
    print(f"   - Précision Spécialiste (top-1): {acc_specialist*100:.2f}%")

    # --- Top-k : la vraie maladie est-elle dans le top k des probas ? ---
    proba_list = model.predict_proba(X_test)
    probs_disease = proba_list[0]
    for k in [3, 5]:
        top_k_correct = sum(
            1 for i in range(n_test)
            if Y_test[i, 0] in np.argsort(probs_disease[i])[-k:][::-1]
        )
        print(f"   - Précision Maladie (top-{k}):     {100*top_k_correct/n_test:.2f}%")

    # --- Confiance moyenne : si la proba max est < 30%, la prédiction est incertaine ---
    max_probs = np.max(probs_disease, axis=1)
    mean_confidence = np.mean(max_probs) * 100
    low_confidence_pct = np.mean(max_probs < 0.30) * 100
    print(f"   - Confiance moyenne (proba max):  {mean_confidence:.1f}%")
    print(f"   - Prédictions à faible confiance (<30%): {low_confidence_pct:.1f}%")

    # --- Rapport par maladie ---
    y_disease_true = Y_test[:, 0]
    y_disease_pred = Y_pred[:, 0]
    cm_disease = confusion_matrix(y_disease_true, y_disease_pred)
    disease_labels = le_disease.classes_
    print("\n   --- Matrice de confusion (Maladies) ---")
    print(f"   Forme: {cm_disease.shape[0]} classes x {cm_disease.shape[1]} classes")
    print(classification_report(y_disease_true, y_disease_pred, target_names=disease_labels, zero_division=0))

    # --- Rapport par spécialiste ---
    y_specialist_true = Y_test[:, 1]
    y_specialist_pred = Y_pred[:, 1]
    cm_specialist = confusion_matrix(y_specialist_true, y_specialist_pred)
    specialist_labels = le_specialist.classes_
    print("\n   --- Matrice de confusion (Spécialistes) ---")
    print(f"   Forme: {cm_specialist.shape[0]} classes x {cm_specialist.shape[1]} classes")
    print(classification_report(y_specialist_true, y_specialist_pred, target_names=specialist_labels, zero_division=0))

    # --- Feature importances (pour vérifier que le modèle ne s'appuie pas trop sur l'âge) ---
    if feature_columns is not None:
        _report_feature_importances(model, feature_columns, config)

    # --- Matrices PNG ---
    _save_confusion_matrix_images(
        config, cm_disease, disease_labels, cm_specialist, specialist_labels
    )


def _report_feature_importances(model, feature_columns: list, config: ModelConfig) -> None:
    """
    Affiche le top-20 des features les plus importantes et sauvegarde
    un barplot si matplotlib est dispo. Permet de détecter les biais :
    si 'Age_normalized' pèse 40% du modèle, c'est un signe de surapprentissage
    sur les profils synthétiques plutôt que sur les symptômes.
    """
    # Pour un MultiOutput RandomForest, chaque estimateur a ses propres importances.
    # On prend la moyenne des deux (maladie + spécialiste).
    importances = np.zeros(len(feature_columns))
    for est in model.estimators_:
        importances += est.feature_importances_
    importances /= len(model.estimators_)

    # Trier par importance décroissante
    sorted_idx = np.argsort(importances)[::-1]
    top_n = 20

    print(f"\n   --- Top-{top_n} Feature Importances ---")
    # Regrouper par catégorie pour le résumé
    symptom_weight = 0.0
    profile_weight = 0.0
    for i in range(len(feature_columns)):
        col = feature_columns[i]
        w = importances[i]
        if '_normalized' in col or col.startswith(('Gender_', 'Blood Pressure_', 'Cholesterol Level_',
                'Outcome Variable_', 'Smoking_', 'Alcohol_', 'Sedentarite_', 'Family_History_')):
            profile_weight += w
        else:
            symptom_weight += w

    print(f"   Poids total symptômes:  {symptom_weight*100:.1f}%")
    print(f"   Poids total profil:     {profile_weight*100:.1f}%")
    if profile_weight > 0.5:
        print("   ⚠ ATTENTION : le profil patient pèse plus de 50%, risque de biais sur les données synthétiques")

    for rank in range(min(top_n, len(feature_columns))):
        idx = sorted_idx[rank]
        print(f"   {rank+1:2d}. {feature_columns[idx]:40s} {importances[idx]*100:.2f}%")

    # Barplot optionnel
    try:
        import matplotlib.pyplot as plt
        fig, ax = plt.subplots(figsize=(10, 6))
        top_indices = sorted_idx[:top_n]
        top_names = [feature_columns[i] for i in top_indices]
        top_values = [importances[i] * 100 for i in top_indices]
        colors = ['#2196F3' if '_normalized' not in n and not any(
            n.startswith(p) for p in ('Gender_', 'Blood Pressure_', 'Cholesterol Level_',
                'Outcome Variable_', 'Smoking_', 'Alcohol_', 'Sedentarite_', 'Family_History_'))
            else '#FF9800' for n in top_names]
        ax.barh(range(top_n), top_values[::-1], color=colors[::-1])
        ax.set_yticks(range(top_n))
        ax.set_yticklabels(top_names[::-1], fontsize=8)
        ax.set_xlabel('Importance (%)')
        ax.set_title('Top-20 Feature Importances (bleu = symptôme, orange = profil)')
        plt.tight_layout()
        path = os.path.join(config.MODELS_DIR, 'feature_importances.png')
        plt.savefig(path, dpi=150, bbox_inches='tight')
        plt.close()
        print(f"   - Feature importances sauvegardées: {path}")
    except ImportError:
        pass


def _save_confusion_matrix_images(
    config: ModelConfig,
    cm_disease,
    disease_labels,
    cm_specialist,
    specialist_labels,
) -> None:
    """Génère les deux PNG dans config.MODELS_DIR si matplotlib est dispo."""
    try:
        from sklearn.metrics import ConfusionMatrixDisplay
        import matplotlib.pyplot as plt
    except ImportError:
        print("   - (Optionnel) Installez matplotlib pour sauvegarder les matrices en image: pip install matplotlib")
        return

    os.makedirs(config.MODELS_DIR, exist_ok=True)

    # Maladies (bleu)
    fig, ax = plt.subplots(figsize=(max(12, len(disease_labels) * 0.4), max(10, len(disease_labels) * 0.35)))
    disp = ConfusionMatrixDisplay(confusion_matrix=cm_disease, display_labels=disease_labels)
    disp.plot(ax=ax, cmap='Blues', xticks_rotation=45, values_format='d')
    plt.title('Matrice de confusion - Prédiction des maladies')
    plt.tight_layout()
    path_disease = os.path.join(config.MODELS_DIR, 'confusion_matrix_disease.png')
    plt.savefig(path_disease, dpi=150, bbox_inches='tight')
    plt.close()
    print(f"   - Matrice de confusion (maladies) sauvegardée: {path_disease}")

    # Spécialistes (vert)
    fig, ax = plt.subplots(figsize=(max(12, len(specialist_labels) * 0.4), max(10, len(specialist_labels) * 0.35)))
    disp = ConfusionMatrixDisplay(confusion_matrix=cm_specialist, display_labels=specialist_labels)
    disp.plot(ax=ax, cmap='Greens', xticks_rotation=45, values_format='d')
    plt.title('Matrice de confusion - Prédiction des spécialistes')
    plt.tight_layout()
    path_specialist = os.path.join(config.MODELS_DIR, 'confusion_matrix_specialist.png')
    plt.savefig(path_specialist, dpi=150, bbox_inches='tight')
    plt.close()
    print(f"   - Matrice de confusion (spécialistes) sauvegardée: {path_specialist}")
