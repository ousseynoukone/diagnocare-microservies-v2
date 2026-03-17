# Data sources and references

This document lists the datasets and literature used for the ML prediction model (symptom–disease and specialist recommendation).

---

## Datasets (Kaggle)

| Description | URL |
|-------------|-----|
| Disease and symptoms dataset | https://www.kaggle.com/datasets/choongqianzheng/disease-and-symptoms-dataset |
| Doctor specialist recommendation system | https://www.kaggle.com/datasets/ebrahimelgazar/doctor-specialist-recommendation-system |
| Disease, symptoms and patient profile dataset | https://www.kaggle.com/datasets/uom190346a/disease-symptoms-and-patient-profile-dataset |

---

## Code references (Kaggle)

| Description | URL |
|-------------|-----|
| Disease type prediction using symptoms | https://www.kaggle.com/code/naga26/disease-type-prediction-using-symptoms |

---

## Literature

| Source | URL |
|--------|-----|
| PMC (PubMed Central) | https://pmc.ncbi.nlm.nih.gov/articles/PMC8354353/ |
| PubMed | https://pubmed.ncbi.nlm.nih.gov/37949111/ |

---

## Usage in this service

- **Symptom-disease data** and **patient profile**-style features are derived from the datasets above.
- **Disease -> specialist** mapping comes from the doctor specialist recommendation dataset (e.g. `Doctor_Versus_Disease.csv`).
- **Synthetic patient profiles** (age, BP, cholesterol, etc.) are generated in `training/profile_generator.py` for training; they are consistent with typical demographics per disease type.
- The literature sources support the methodological and medical context of symptom-based prediction and specialist recommendation.

---

## ML techniques used

| Technique | Purpose | Library |
|-----------|---------|---------|
| TF-IDF | Weight rare symptoms higher than common ones | scikit-learn `TfidfVectorizer` |
| XGBoost | Gradient boosting that handles sparse/missing data natively | `xgboost` |
| Feature interactions | Cross profile signals (age, gender, smoking, BP) with key symptoms | Manual feature engineering |
| Isotonic calibration | Align predicted probabilities with observed accuracy | scikit-learn `CalibratedClassifierCV` |
| Data augmentation | Simulate incomplete symptom input (light/heavy drop) | Custom (`data_augmentation.py`) |
| Label cleaning | Normalise specialist labels (typos, casing, duplicates) | Custom (`data_cleaning.py`) |

For training instructions, see [BUILD.md](BUILD.md).
For full model documentation, see `docs/technical-documentation/06-ml-prediction-service.md`.
