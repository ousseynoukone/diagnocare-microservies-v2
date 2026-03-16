# Building the ML Prediction Service image

The container **does not train the model or generate translations**. You must produce these artifacts **before** building the image.

## 1. Produce artifacts (outside the container)

From the project root (e.g. on your machine or in CI):

```bash
cd MlPredictionService

# Install dependencies (optional, if you run training locally)
pip install -r requirements.txt

# Train the model → writes models/*.joblib
python train.py

# Generate translations → writes data/translations.json (and optionally data/symptoms_template.json)
python generate_symptom_translations.py
# or: python translate_symptoms_fr.py  (if you use that script)
```

Ensure these exist before building:

- `models/model.joblib`
- `models/mlb.joblib`
- `models/scaler.joblib`
- `models/le_disease.joblib`
- `models/le_specialist.joblib`
- `models/feature_columns.joblib`
- `data/translations.json`

## 2. Build the image

```bash
docker build -t ml-prediction-service .
```

The Dockerfile and `.dockerignore` are set up so that:

- **Included:** app code, `models/` (Python package + your `.joblib` files), `data/translations.json` (and `data/symptoms_template.json` if present). `dataset.csv` is ignored.
- **Excluded:** `training/`, `train.py`, `translate_symptoms_fr.py`, `generate_symptom_translations.py`, and `data/dataset.csv`.

If any required `.joblib` or `data/translations.json` is missing, the app will fail at **startup** with a clear error, not at build time.
