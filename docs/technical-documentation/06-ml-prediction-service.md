# ML Prediction Service - Complete Documentation

## Overview

**ML Prediction Service** is a Flask-based Python service that provides machine learning disease predictions based on symptoms and patient profiles.

**Technology**: Flask (Python), XGBoost, scikit-learn, TF-IDF  
**Port**: 5000  
**Architecture**: Modular (controllers, services, repositories)

---

## Architecture

### Package Structure
```
MlPredictionService/
├── app.py                          # Flask application entry
├── config/
│   ├── app_config.py               # Flask & Eureka config
│   └── model_config.py             # ML model config
├── controllers/
│   ├── prediction_controller.py    # /predict endpoint
│   ├── health_controller.py        # /health endpoint
│   └── metadata_controller.py      # /metadata endpoint
├── services/
│   ├── prediction_service.py       # Prediction logic (TF-IDF + interactions)
│   ├── translation_service.py     # Multi-language support
│   └── nlp_service.py              # NLP symptom extraction
├── repositories/
│   ├── model_repository.py         # Model loading (includes TfidfVectorizer)
│   └── translation_repository.py   # Translation data
├── models/
│   ├── prediction_request.py       # Request DTOs
│   └── prediction_response.py     # Response DTOs (confidence_level, confidence_note)
├── utils/
│   └── text_utils.py               # Text utilities
├── training/                       # Training scripts (run outside container)
│   ├── train_model.py              # Entry point: python training/train_model.py
│   ├── model_trainer.py            # Orchestrates data, train XGBoost, calibrate, save
│   ├── profile_generator.py        # Synthetic patient profiles per disease
│   ├── data_preparation.py         # Load dataset, TF-IDF encoding, feature interactions
│   ├── data_augmentation.py        # Light/heavy symptom drop simulation
│   ├── data_cleaning.py            # Specialist label normalisation
│   └── evaluation.py               # Metrics, confusion matrices, PNG export
├── models/                         # Saved artefacts (after training)
│   ├── model.joblib                # CalibratedClassifierCV(MultiOutputClassifier(XGBClassifier))
│   ├── mlb.joblib                  # MultiLabelBinarizer (symptom validation)
│   ├── tfidf.joblib                # TfidfVectorizer (symptom encoding)
│   ├── scaler.joblib               # StandardScaler (numeric profile features)
│   ├── le_disease.joblib           # LabelEncoder (disease)
│   ├── le_specialist.joblib        # LabelEncoder (specialist)
│   ├── feature_columns.joblib      # Column order for inference
│   ├── confusion_matrix_disease.png
│   ├── confusion_matrix_specialist.png
│   └── feature_importances.png
└── data/
    └── translations.json           # Generated before build
```

---

## ML Model Architecture

### Why this stack

The model uses **XGBoost** with **TF-IDF** symptom encoding, **feature interactions**, and **isotonic calibration**. Each component solves a specific problem:

### 1. TF-IDF Symptom Encoding (replaces binary 0/1)

**Problem**: With binary MultiLabelBinarizer, a common symptom like `fatigue` (present in 15+ diseases) had the same weight (1.0) as a rare, diagnostic symptom like `silver_like_dusting` (exclusive to Psoriasis).

**Solution**: TF-IDF (Term Frequency-Inverse Document Frequency) automatically weights symptoms by rarity:
- `fatigue`: IDF ≈ 1.0 (appears in many diseases → low signal)
- `silver_like_dusting`: IDF ≈ 3.7 (appears in 1 disease → high signal)

The `sublinear_tf=True` and `norm='l2'` parameters in the TfidfVectorizer ensure smooth weighting and normalised feature vectors. The MultiLabelBinarizer (`mlb.joblib`) is still saved for symptom **validation** at inference time — it checks that user-provided symptom labels exist in the known vocabulary.

### 2. XGBoost (replaces RandomForest)

**Problem**: When a user selects 4 symptoms out of 131, RandomForest treats the 127 zeros as "patient does NOT have this symptom". In reality, those are unknown — the patient wasn't asked about them.

**Solution**: XGBoost with `tree_method='hist'` natively handles sparse/missing data. It learns to route "0" values to the optimal branch rather than treating them as definitive negatives. Key hyperparameters:

| Parameter | Value | Rationale |
|-----------|-------|-----------|
| `n_estimators` | 400 | Enough trees for 41 disease classes |
| `max_depth` | 8 | Deep enough for symptom combinations |
| `learning_rate` | 0.1 | Standard for this dataset size |
| `subsample` | 0.8 | Row sampling to reduce overfitting |
| `colsample_bytree` | 0.8 | Feature sampling per tree |
| `min_child_weight` | 3 | Prevents splits on very rare patterns |
| `reg_alpha` | 0.1 | L1 regularisation |
| `reg_lambda` | 1.0 | L2 regularisation |

### 3. Feature Interactions (profile × key symptoms)

**Problem**: Patient profile features (age, gender, smoking, blood pressure) contributed only ~2% to predictions. The model was essentially a symptom lookup table.

**Solution**: 48 interaction features cross key symptoms with profile signals:
- `IX_chest_pain_x_age` — chest pain in a 60-year-old has more cardiac weight
- `IX_breathlessness_x_smoker` — breathlessness in a smoker suggests pulmonary disease
- `IX_fatigue_x_bp_high` — fatigue with hypertension points toward cardiac/vascular

Profile signals used: `age` (normalised), `male` (binary), `smoker` (binary), `bp_high` (binary).

Key symptoms for interactions:
```
chest_pain, breathlessness, high_fever, fatigue, weight_loss, weight_gain,
headache, joint_pain, skin_rash, vomiting, dizziness, sweating
```

### 4. Isotonic Calibration (CalibratedClassifierCV)

**Problem**: Tree-based models output "confidence scores" based on vote ratios, not true probabilities. An XGBoost saying "90%" doesn't mean the prediction is correct 90% of the time.

**Solution**: After training the raw XGBoost, each sub-estimator (disease classifier, specialist classifier) is individually calibrated using `CalibratedClassifierCV(cv=3, method='isotonic')` on the training data. This maps raw scores to calibrated probabilities where "90% confidence ≈ 90% actual accuracy".

### 5. Data Augmentation

**Light drop**: For each sample, 2 copies are created with 1-2 random symptoms removed. This simulates users who forget to mention some symptoms.

**Heavy drop**: 1 copy per sample reduced to only 2-3 symptoms. This simulates minimal user input and forces the model to make reasonable predictions with very little data.

### 6. Data Cleaning

Specialist labels from `Doctor_Versus_Disease.csv` are normalised: duplicates like `Gastroenterologist` (with trailing space), case mismatches (`hepatologist` vs `Hepatologist`), and typos (`Internal Medcine` → `Internal Medicine`) are cleaned in `data_cleaning.py`.

---

## Training Pipeline

Training is run on the host (or CI), **not** inside the container.

### Entry point
```bash
cd MlPredictionService
python training/train_model.py
```

### Pipeline steps

| Step | Module | Description |
|------|--------|-------------|
| 1 | `data_preparation.py` | Load `dataset.csv` (4920 rows, 17 symptom columns) |
| 2 | `data_preparation.py` | Clean symptom labels |
| 3 | `data_augmentation.py` | Light drop (×2) + heavy drop (×1) → ~19k samples |
| 4 | `data_preparation.py` | TF-IDF encoding (131 symptom features, weighted) |
| 5 | `profile_generator.py` | Generate synthetic patient profiles per disease |
| 6 | `data_preparation.py` | Map disease → specialist (with label cleaning) |
| 7 | `data_preparation.py` | Create 48 feature interactions (profile × key symptoms) |
| 8 | `data_preparation.py` | Build final feature matrix (204 columns) |
| 9 | `model_trainer.py` | Stratified train/test split (80/20) |
| 10 | `model_trainer.py` | Train `MultiOutputClassifier(XGBClassifier)` |
| 11 | `model_trainer.py` | Calibrate each sub-estimator (isotonic, cv=3) |
| 12 | `evaluation.py` | Evaluate: top-1/3/5 accuracy, confusion matrices, feature importances |
| 13 | `model_trainer.py` | Save 7 `.joblib` artefacts + optional PNG plots |

### Feature matrix composition (204 columns)

| Category | Count | Description |
|----------|-------|-------------|
| TF-IDF symptoms | 131 | Weighted symptom features |
| Numerical (normalised) | 5 | Age, Weight, BMI, Tension, Cholesterol |
| Categorical (one-hot) | 20 | Gender, BP, Cholesterol Level, Smoking, etc. |
| Interactions | 48 | 12 key symptoms × 4 profile signals |
| **Total** | **204** | |

### Evaluation metrics (latest training)

| Metric | Score |
|--------|-------|
| Disease accuracy (top-1) | **96.15%** |
| Disease accuracy (top-3) | **98.99%** |
| Disease accuracy (top-5) | **99.66%** |
| Specialist accuracy (top-1) | **97.60%** |
| Mean confidence (calibrated) | **96.1%** |
| Low confidence predictions (<30%) | **0.3%** |

### Outputs

- `models/model.joblib` — Calibrated multi-output XGBoost model
- `models/mlb.joblib` — MultiLabelBinarizer (symptom validation)
- `models/tfidf.joblib` — TfidfVectorizer (symptom encoding)
- `models/scaler.joblib` — StandardScaler (numeric profile normalisation)
- `models/le_disease.joblib` — LabelEncoder (41 diseases)
- `models/le_specialist.joblib` — LabelEncoder (15 specialists)
- `models/feature_columns.joblib` — Column order (204 feature names)
- `models/confusion_matrix_disease.png` — 41×41 confusion matrix
- `models/confusion_matrix_specialist.png` — 15×15 confusion matrix
- `models/feature_importances.png` — Top-20 features (blue=symptom, orange=profile, green=interaction)

---

## API Endpoints

### POST `/predict`
**Purpose**: Predict diseases from symptoms

**Request**:
```json
{
  "symptoms": ["chest_pain", "breathlessness", "sweating", "vomiting"],
  "age": 62,
  "weight": 92,
  "bmi": 29.5,
  "tension_moyenne": 165,
  "cholesterole_moyen": 260,
  "gender": "Male",
  "blood_pressure": "High",
  "cholesterol_level": "High",
  "smoking": "Yes",
  "alcohol": "Moderate",
  "sedentarite": "High",
  "family_history": "Yes",
  "outcome_variable": "Positive",
  "language": "en"
}
```

**Response**:
```json
{
  "predictions": [
    {
      "rank": 1,
      "disease": "Heart attack",
      "disease_en": "Heart attack",
      "disease_fr": "Crise cardiaque",
      "probability": 87.3,
      "specialist": "Cardiologist",
      "specialist_en": "Cardiologist",
      "specialist_fr": "Cardiologue",
      "specialist_probability": 84.1,
      "description": "..."
    }
  ],
  "language": "en",
  "confidence_level": "high",
  "confidence_note": null,
  "metadata": {
    "symptoms_count": 4,
    "profile_used": { "Age": 62, "Gender": "Male", "..." : "..." }
  }
}
```

**Confidence levels**:
| Level | Condition | Note |
|-------|-----------|------|
| `high` | top probability > 70% | No note |
| `moderate` | 40% ≤ top probability ≤ 70% | "Plusieurs pathologies possibles..." |
| `low` | top probability < 40% | "Veuillez preciser vos symptomes..." |

### GET `/health`
**Purpose**: Health check endpoint  
**Response**: `200 OK`

### GET `/metadata`
**Purpose**: Get model metadata (symptom labels, feature info, translations)

---

## Prediction Process (inference)

1. **Validate symptoms**: Check against `mlb.classes_` (known symptom vocabulary)
2. **TF-IDF encode**: Transform symptom list into weighted feature vector via saved `TfidfVectorizer`
3. **Prepare profile**: Extract numeric and categorical features from request (with defaults)
4. **Build interactions**: Cross key symptoms with profile signals (same 48 features as training)
5. **Combine features**: Concatenate TF-IDF + numeric + categorical + interactions, reindex to 204 columns
6. **Predict**: Run calibrated XGBoost multi-output model → disease probabilities + specialist probabilities
7. **Rank**: Sort top 5 diseases by probability
8. **Translate**: Localise disease and specialist names (en/fr)
9. **Confidence**: Assign confidence level based on calibrated top probability
10. **Return**: Top 5 predictions with metadata

---

## Model Configuration

- **Model Format**: `.joblib` (scikit-learn / XGBoost)
- **Model Location**: `/app/models/`
- **Translation Data**: `/app/data/translations.json`
- **Training**: The Docker image does **not** train the model; artefacts must be produced before building the image. See `BUILD.md`.
- **Dependencies**: `xgboost>=2.0.0`, `scikit-learn>=1.3.2`, `pandas`, `numpy`, `joblib`

---

## Multi-Language Support

- **Languages**: French (fr), English (en)
- **Translation**: Disease names and specialist labels
- **Default**: English

---

## Data sources and references

The training pipeline and model are based on the following datasets and literature:

### Datasets (Kaggle)

- **Disease and symptoms dataset**  
  https://www.kaggle.com/datasets/choongqianzheng/disease-and-symptoms-dataset  

- **Doctor specialist recommendation system**  
  https://www.kaggle.com/datasets/ebrahimelgazar/doctor-specialist-recommendation-system  

- **Disease, symptoms and patient profile dataset**  
  https://www.kaggle.com/datasets/uom190346a/disease-symptoms-and-patient-profile-dataset  

### Code references (Kaggle)

- **Disease type prediction using symptoms**  
  https://www.kaggle.com/code/naga26/disease-type-prediction-using-symptoms  

### Literature

- **PMC (PubMed Central)**  
  https://pmc.ncbi.nlm.nih.gov/articles/PMC8354353/  

- **PubMed**  
  https://pubmed.ncbi.nlm.nih.gov/37949111/  

The service uses a combination of symptom-disease data, disease-specialist mapping, and synthetic patient profiles derived from these sources. See `MlPredictionService/DATA_SOURCES.md` for the same references in the repository.

---

## See Also
- [DiagnoCare Service](04-diagnocare-service.md) - ML integration
- [Architecture Overview](01-architecture-overview.md)
- **In-repo**: `MlPredictionService/BUILD.md` for build and training instructions
- **In-repo**: `MlPredictionService/DATA_SOURCES.md` for data sources and references
