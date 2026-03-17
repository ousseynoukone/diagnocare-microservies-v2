"""
Data augmentation pour rendre le modèle robuste aux saisies incomplètes.

Le problème : les datasets Kaggle sont "trop propres" — chaque maladie a
exactement ses N symptômes. En situation réelle, un utilisateur en oublie 1 ou 2.
Le modèle ne reconnaît alors plus la maladie.

La solution : pour chaque ligne du dataset, on crée K copies où on retire
aléatoirement 1 à max_drop symptômes. Le RandomForest apprend ainsi que
la maladie X reste probable même avec un tableau clinique incomplet.
"""
import numpy as np
import pandas as pd
from typing import List


def augment_symptom_lists(
    symptom_lists: List[List[str]],
    diseases: pd.Series,
    n_augmented_copies: int = 3,
    max_drop: int = 2,
    random_seed: int = 42,
) -> tuple:
    """
    Duplique les lignes en retirant des symptômes au hasard.

    Args:
        symptom_lists: liste originale (1 entrée = 1 liste de symptômes nettoyés)
        diseases: Series des maladies correspondantes (même index)
        n_augmented_copies: combien de copies « bruitées » par ligne originale
        max_drop: nombre max de symptômes retirés par copie
        random_seed: graine pour la reproductibilité

    Returns:
        (augmented_symptom_lists, augmented_diseases) incluant les originaux
    """
    rng = np.random.RandomState(random_seed)

    augmented_symptoms = list(symptom_lists)  # on garde les originaux
    augmented_diseases = list(diseases)

    for idx, symptoms in enumerate(symptom_lists):
        if len(symptoms) <= 1:
            # Pas assez de symptômes pour en retirer
            continue

        for _ in range(n_augmented_copies):
            n_drop = rng.randint(1, min(max_drop, len(symptoms) - 1) + 1)
            indices_to_drop = rng.choice(len(symptoms), size=n_drop, replace=False)
            reduced = [s for i, s in enumerate(symptoms) if i not in indices_to_drop]
            augmented_symptoms.append(reduced)
            augmented_diseases.append(diseases.iloc[idx])

    return augmented_symptoms, pd.Series(augmented_diseases).reset_index(drop=True)
