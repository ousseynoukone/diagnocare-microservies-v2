"""
Data augmentation pour rendre le modele robuste aux saisies incompletes.

En situation reelle, un utilisateur ne mentionne pas tous ses symptomes.
On simule ca avec deux niveaux de "suppression" :

  - Light : retirer 1-2 symptomes (oublis courants)
  - Heavy : ne garder que 2-3 symptomes (utilisateur qui donne juste
    sa plainte principale, ex: "j'ai de la fievre et mal a la tete")

Le heavy drop est le plus utile : il force le modele a distinguer
des maladies qui partagent des symptomes courants, et a exprimer
son incertitude quand l'information est insuffisante.
"""
import numpy as np
import pandas as pd
from typing import List


def augment_symptom_lists(
    symptom_lists: List[List[str]],
    diseases: pd.Series,
    n_light_copies: int = 2,
    max_light_drop: int = 2,
    n_heavy_copies: int = 3,
    keep_min: int = 2,
    keep_max: int = 3,
    random_seed: int = 42,
) -> tuple:
    """
    Augmentation en 2 passes :
      - Light : retire 1-2 symptomes (cas classique)
      - Heavy : ne garde que 2-3 symptomes (cas realiste)

    Returns:
        (augmented_symptom_lists, augmented_diseases) incluant les originaux
    """
    rng = np.random.RandomState(random_seed)

    augmented_symptoms = list(symptom_lists)
    augmented_diseases = list(diseases)

    for idx, symptoms in enumerate(symptom_lists):
        if len(symptoms) <= 1:
            continue

        for _ in range(n_light_copies):
            n_drop = rng.randint(1, min(max_light_drop, len(symptoms) - 1) + 1)
            indices_to_drop = rng.choice(len(symptoms), size=n_drop, replace=False)
            reduced = [s for i, s in enumerate(symptoms) if i not in indices_to_drop]
            augmented_symptoms.append(reduced)
            augmented_diseases.append(diseases.iloc[idx])

        if len(symptoms) > keep_max:
            for _ in range(n_heavy_copies):
                n_keep = rng.randint(keep_min, keep_max + 1)
                indices_to_keep = rng.choice(len(symptoms), size=n_keep, replace=False)
                reduced = [symptoms[i] for i in sorted(indices_to_keep)]
                augmented_symptoms.append(reduced)
                augmented_diseases.append(diseases.iloc[idx])

    return augmented_symptoms, pd.Series(augmented_diseases).reset_index(drop=True)
