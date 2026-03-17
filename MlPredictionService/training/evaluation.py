"""
Evaluation du modele apres entrainement :
  - accuracy top-1 et top-k
  - rapports de classification par classe (precision, recall, F1)
  - matrices de confusion (texte + PNG)
  - feature importances (pour detecter si le modele se base trop sur l'age vs les symptomes)
"""
import os
import sys
import numpy as np
from sklearn.metrics import accuracy_score, confusion_matrix, classification_report

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from config.model_config import ModelConfig

PROFILE_PREFIXES = (
    'Gender_', 'Blood Pressure_', 'Cholesterol Level_',
    'Outcome Variable_', 'Smoking_', 'Alcohol_', 'Sedentarite_', 'Family_History_'
)


def _is_profile_feature(col_name: str) -> bool:
    return '_normalized' in col_name or col_name.startswith(PROFILE_PREFIXES)


def _is_interaction_feature(col_name: str) -> bool:
    return col_name.startswith('IX_')


def evaluate_and_report(
    model,
    X_test,
    Y_test: np.ndarray,
    le_disease,
    le_specialist,
    config: ModelConfig,
    feature_columns: list = None,
    precomputed_importances: np.ndarray = None,
) -> None:
    Y_pred = model.predict(X_test)
    n_test = Y_test.shape[0]

    # --- Accuracy top-1 ---
    acc_disease = accuracy_score(Y_test[:, 0], Y_pred[:, 0])
    acc_specialist = accuracy_score(Y_test[:, 1], Y_pred[:, 1])
    print(f"   - Precision Maladie (top-1):     {acc_disease*100:.2f}%")
    print(f"   - Precision Specialiste (top-1): {acc_specialist*100:.2f}%")

    # --- Top-k ---
    proba_list = model.predict_proba(X_test)
    probs_disease = proba_list[0]
    for k in [3, 5]:
        top_k_correct = sum(
            1 for i in range(n_test)
            if Y_test[i, 0] in np.argsort(probs_disease[i])[-k:][::-1]
        )
        print(f"   - Precision Maladie (top-{k}):     {100*top_k_correct/n_test:.2f}%")

    # --- Confiance moyenne ---
    max_probs = np.max(probs_disease, axis=1)
    mean_confidence = np.mean(max_probs) * 100
    low_confidence_pct = np.mean(max_probs < 0.30) * 100
    print(f"   - Confiance moyenne (proba max):  {mean_confidence:.1f}%")
    print(f"   - Predictions a faible confiance (<30%): {low_confidence_pct:.1f}%")

    # --- Rapport par maladie ---
    y_disease_true = Y_test[:, 0]
    y_disease_pred = Y_pred[:, 0]
    cm_disease = confusion_matrix(y_disease_true, y_disease_pred)
    disease_labels = le_disease.classes_
    print("\n   --- Matrice de confusion (Maladies) ---")
    print(f"   Forme: {cm_disease.shape[0]} classes x {cm_disease.shape[1]} classes")
    print(classification_report(y_disease_true, y_disease_pred, target_names=disease_labels, zero_division=0))

    # --- Rapport par specialiste ---
    y_specialist_true = Y_test[:, 1]
    y_specialist_pred = Y_pred[:, 1]
    cm_specialist = confusion_matrix(y_specialist_true, y_specialist_pred)
    specialist_labels = le_specialist.classes_
    print("\n   --- Matrice de confusion (Specialistes) ---")
    print(f"   Forme: {cm_specialist.shape[0]} classes x {cm_specialist.shape[1]} classes")
    print(classification_report(y_specialist_true, y_specialist_pred, target_names=specialist_labels, zero_division=0))

    # --- Feature importances ---
    if feature_columns is not None:
        importances = precomputed_importances
        if importances is None:
            importances = _try_extract_importances(model, len(feature_columns))
        if importances is not None:
            _report_feature_importances(importances, feature_columns, config)
        else:
            print("   - Feature importances non disponibles pour ce type de modele")

    # --- Matrices PNG ---
    _save_confusion_matrix_images(
        config, cm_disease, disease_labels, cm_specialist, specialist_labels
    )


def _try_extract_importances(model, n_features: int):
    """Tente d'extraire les importances selon le type de modele."""
    # RandomForest / XGBoost direct
    if hasattr(model, 'feature_importances_'):
        return model.feature_importances_
    # MultiOutputClassifier wrapping des estimateurs avec feature_importances_
    if hasattr(model, 'estimators_'):
        importances = np.zeros(n_features)
        count = 0
        for est in model.estimators_:
            if hasattr(est, 'feature_importances_'):
                importances += est.feature_importances_
                count += 1
        if count > 0:
            return importances / count
    return None


def _report_feature_importances(importances: np.ndarray, feature_columns: list, config: ModelConfig) -> None:
    sorted_idx = np.argsort(importances)[::-1]
    top_n = 20

    print(f"\n   --- Top-{top_n} Feature Importances ---")

    symptom_weight = 0.0
    profile_weight = 0.0
    interaction_weight = 0.0
    for i, col in enumerate(feature_columns):
        if _is_interaction_feature(col):
            interaction_weight += importances[i]
        elif _is_profile_feature(col):
            profile_weight += importances[i]
        else:
            symptom_weight += importances[i]

    print(f"   Poids total symptomes (TF-IDF):   {symptom_weight*100:.1f}%")
    print(f"   Poids total profil:               {profile_weight*100:.1f}%")
    print(f"   Poids total interactions:          {interaction_weight*100:.1f}%")
    if profile_weight > 0.5:
        print("   /!\\ ATTENTION : le profil pese plus de 50%, risque de biais")

    for rank in range(min(top_n, len(feature_columns))):
        idx = sorted_idx[rank]
        tag = ""
        if _is_interaction_feature(feature_columns[idx]):
            tag = " [IX]"
        elif _is_profile_feature(feature_columns[idx]):
            tag = " [profil]"
        print(f"   {rank+1:2d}. {feature_columns[idx]:45s} {importances[idx]*100:.2f}%{tag}")

    try:
        import matplotlib.pyplot as plt
        fig, ax = plt.subplots(figsize=(10, 6))
        top_indices = sorted_idx[:top_n]
        top_names = [feature_columns[i] for i in top_indices]
        top_values = [importances[i] * 100 for i in top_indices]

        def _color(name):
            if _is_interaction_feature(name):
                return '#4CAF50'
            elif _is_profile_feature(name):
                return '#FF9800'
            return '#2196F3'

        colors = [_color(n) for n in top_names]
        ax.barh(range(top_n), top_values[::-1], color=colors[::-1])
        ax.set_yticks(range(top_n))
        ax.set_yticklabels(top_names[::-1], fontsize=8)
        ax.set_xlabel('Importance (%)')
        ax.set_title('Top-20 Feature Importances (bleu=symptome, orange=profil, vert=interaction)')
        plt.tight_layout()
        path = os.path.join(config.MODELS_DIR, 'feature_importances.png')
        plt.savefig(path, dpi=150, bbox_inches='tight')
        plt.close()
        print(f"   - Feature importances sauvegardees: {path}")
    except ImportError:
        pass


def _save_confusion_matrix_images(
    config: ModelConfig,
    cm_disease,
    disease_labels,
    cm_specialist,
    specialist_labels,
) -> None:
    try:
        from sklearn.metrics import ConfusionMatrixDisplay
        import matplotlib.pyplot as plt
    except ImportError:
        print("   - (Optionnel) pip install matplotlib pour les matrices en image")
        return

    os.makedirs(config.MODELS_DIR, exist_ok=True)

    fig, ax = plt.subplots(figsize=(max(12, len(disease_labels) * 0.4), max(10, len(disease_labels) * 0.35)))
    disp = ConfusionMatrixDisplay(confusion_matrix=cm_disease, display_labels=disease_labels)
    disp.plot(ax=ax, cmap='Blues', xticks_rotation=45, values_format='d')
    plt.title('Matrice de confusion - Maladies')
    plt.tight_layout()
    path_disease = os.path.join(config.MODELS_DIR, 'confusion_matrix_disease.png')
    plt.savefig(path_disease, dpi=150, bbox_inches='tight')
    plt.close()
    print(f"   - Matrice de confusion (maladies): {path_disease}")

    fig, ax = plt.subplots(figsize=(max(12, len(specialist_labels) * 0.4), max(10, len(specialist_labels) * 0.35)))
    disp = ConfusionMatrixDisplay(confusion_matrix=cm_specialist, display_labels=specialist_labels)
    disp.plot(ax=ax, cmap='Greens', xticks_rotation=45, values_format='d')
    plt.title('Matrice de confusion - Specialistes')
    plt.tight_layout()
    path_specialist = os.path.join(config.MODELS_DIR, 'confusion_matrix_specialist.png')
    plt.savefig(path_specialist, dpi=150, bbox_inches='tight')
    plt.close()
    print(f"   - Matrice de confusion (specialistes): {path_specialist}")
