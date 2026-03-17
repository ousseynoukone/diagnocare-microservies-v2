# Building the ML Prediction Service image

The container **does not train the model or generate translations**. You must produce these artifacts **before** building the image.

## 1. Produce artifacts (outside the container)

From the project root (e.g. on your machine or in CI):

```bash
cd MlPredictionService

# Install dependencies (including xgboost)
pip install -r requirements.txt

# Train the model → writes models/*.joblib
python training/train_model.py

# Generate translations → writes data/translations.json
python generate_symptom_translations.py
```

### Required artifacts

Ensure these exist before building:

| File | Description |
|------|-------------|
| `models/model.joblib` | Calibrated XGBoost multi-output model |
| `models/mlb.joblib` | MultiLabelBinarizer (symptom validation) |
| `models/tfidf.joblib` | TfidfVectorizer (symptom TF-IDF encoding) |
| `models/scaler.joblib` | StandardScaler (numeric profile features) |
| `models/le_disease.joblib` | LabelEncoder (41 diseases) |
| `models/le_specialist.joblib` | LabelEncoder (15 specialists) |
| `models/feature_columns.joblib` | Column order (204 feature names) |
| `data/translations.json` | Disease/specialist translations (en/fr) |

### Optional outputs (if matplotlib is installed)

- `models/confusion_matrix_disease.png` — 41×41 confusion matrix
- `models/confusion_matrix_specialist.png` — 15×15 confusion matrix
- `models/feature_importances.png` — Top-20 feature importances

## 2. Build the image

```bash
docker build -t ml-prediction-service .
```

The Dockerfile and `.dockerignore` are set up so that:

- **Included:** app code, `models/` (Python package + your `.joblib` files), `data/translations.json` (and `data/symptoms_template.json` if present). `dataset.csv` is ignored.
- **Excluded:** `training/`, `train.py`, `translate_symptoms_fr.py`, `generate_symptom_translations.py`, and `data/dataset.csv`.

If any required `.joblib` or `data/translations.json` is missing, the app will fail at **startup** with a clear error, not at build time.

## 3. Key dependencies

The container installs from `requirements.txt`, which includes:
- `xgboost>=2.0.0` — XGBoost gradient boosting (model runtime)
- `scikit-learn>=1.3.2` — TfidfVectorizer, CalibratedClassifierCV, MultiOutputClassifier
- `pandas`, `numpy`, `joblib` — Data handling and model serialization

## 4. Model architecture summary

The trained model pipeline:
1. **TF-IDF** encodes symptoms (weights rare symptoms higher than common ones)
2. **Feature interactions** cross key symptoms with profile signals (age, gender, smoking, BP)
3. **XGBoost** (multi-output, 400 trees, max_depth=8) handles sparse input and missing data
4. **Isotonic calibration** aligns predicted probabilities with observed accuracy

See `docs/technical-documentation/06-ml-prediction-service.md` for full details.
