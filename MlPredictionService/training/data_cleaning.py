"""
Nettoyage des données : correction de casse, typos, doublons dans les labels
de spécialistes et de maladies avant l'entraînement.

Les datasets Kaggle contiennent des incohérences connues :
  - "hepatologist" vs "Hepatologist" (casse)
  - "Gastroenterologist " (espace en fin de ligne)
  - "Internal Medcine" (typo : devrait être "Internal Medicine")
  - "Tuberculosis" utilisé à la fois comme maladie ET comme spécialiste

Ce module les corrige pour éviter que le LabelEncoder crée des classes en double.
"""


# Les spécialistes tels qu'on les veut dans le modèle final.
# clé = forme brute dans le CSV, valeur = forme corrigée
SPECIALIST_FIXES = {
    "hepatologist":           "Hepatologist",
    "Hepatologist":           "Hepatologist",
    "Gastroenterologist ":    "Gastroenterologist",   # espace en fin
    "Gastroenterologist":     "Gastroenterologist",
    "Internal Medcine":       "Internal Medicine",
    "Tuberculosis":           "Pulmonologist",         # TB relève du pneumologue
}


def clean_specialist_label(raw: str) -> str:
    """
    Normalise un label de spécialiste :
      1. strip les espaces
      2. applique les corrections connues
      3. en dernier recours, capitalise la première lettre
    """
    stripped = raw.strip()
    if stripped in SPECIALIST_FIXES:
        return SPECIALIST_FIXES[stripped]
    # Sécurité : au moins mettre la première lettre en majuscule
    return stripped[0].upper() + stripped[1:] if stripped else stripped
