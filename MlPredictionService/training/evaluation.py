"""
Évaluation du modèle après entraînement : accuracy, top-k, rapports par classe,
et matrices de confusion (texte + images si matplotlib est dispo).
"""
import os
import sys
import numpy as np
from sklearn.metrics import accuracy_score, confusion_matrix, classification_report

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from config.model_config import ModelConfig


def evaluate_and_report(
    model,
    X_test: np.ndarray,
    Y_test: np.ndarray,
    le_disease,
    le_specialist,
    config: ModelConfig,
) -> None:
    """
    Affiche les métriques (top-1, top-k), les rapports de classification et les matrices
    de confusion. Sauvegarde les deux matrices en PNG dans config.MODELS_DIR si
    matplotlib est disponible.
    """
    Y_pred = model.predict(X_test)
    n_test = Y_test.shape[0]

    # --- Précision top-1 (la prédiction la plus probable doit être la bonne) ---
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

    # --- Maladies : matrice de confusion + rapport détaillé (precision, recall, F1) ---
    y_disease_true = Y_test[:, 0]
    y_disease_pred = Y_pred[:, 0]
    cm_disease = confusion_matrix(y_disease_true, y_disease_pred)
    disease_labels = le_disease.classes_
    print("\n   --- Matrice de confusion (Maladies) ---")
    print(f"   Forme: {cm_disease.shape[0]} classes x {cm_disease.shape[1]} classes")
    print(classification_report(y_disease_true, y_disease_pred, target_names=disease_labels, zero_division=0))

    # --- Spécialistes : idem ---
    y_specialist_true = Y_test[:, 1]
    y_specialist_pred = Y_pred[:, 1]
    cm_specialist = confusion_matrix(y_specialist_true, y_specialist_pred)
    specialist_labels = le_specialist.classes_
    print("\n   --- Matrice de confusion (Spécialistes) ---")
    print(f"   Forme: {cm_specialist.shape[0]} classes x {cm_specialist.shape[1]} classes")
    print(classification_report(y_specialist_true, y_specialist_pred, target_names=specialist_labels, zero_division=0))

    # --- Sauvegarde des matrices en image (optionnel) ---
    _save_confusion_matrix_images(
        config, cm_disease, disease_labels, cm_specialist, specialist_labels
    )


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
