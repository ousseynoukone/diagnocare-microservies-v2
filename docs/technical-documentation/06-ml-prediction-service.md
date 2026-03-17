# ML Prediction Service - Complete Documentation

## Overview

**ML Prediction Service** is a Flask-based Python service that provides machine learning disease predictions based on symptoms and patient profiles.

**Technology**: Flask (Python), scikit-learn  
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
│   ├── prediction_service.py       # Prediction logic
│   ├── translation_service.py     # Multi-language support
│   └── nlp_service.py              # NLP symptom extraction
├── repositories/
│   ├── model_repository.py         # Model loading
│   └── translation_repository.py   # Translation data
├── models/
│   ├── prediction_request.py       # Request DTOs
│   └── prediction_response.py     # Response DTOs
├── utils/
│   └── text_utils.py               # Text utilities
├── training/                       # Training scripts (run outside container)
│   ├── train_model.py              # Entry point: python training/train_model.py
│   ├── model_trainer.py            # Orchestrates data, train, evaluate, save
│   ├── profile_generator.py        # Synthetic patient profiles per disease
│   ├── data_preparation.py         # Load dataset, build features
│   └── evaluation.py               # Metrics, confusion matrices, PNG export
├── models/                         # Saved artefacts (after training)
│   ├── *.joblib                    # model, mlb, scaler, label encoders, feature_columns
│   ├── confusion_matrix_disease.png
│   └── confusion_matrix_specialist.png
└── data/
    └── translations.json           # Generated before build
```

---

## API Endpoints

### POST `/predict`
**Purpose**: Predict diseases from symptoms

**Request**:
```json
{
  "symptoms": ["fever", "cough", "fatigue"],
  "age": 45,
  "weight": 75.0,
  "height": 170.0,
  "tension_moyenne": 120.0,
  "cholesterole_moyen": 190.0,
  "gender": "Male",
  "blood_pressure": "Normal",
  "cholesterol_level": "Normal",
  "smoking": "No",
  "alcohol": "None",
  "sedentarite": "Moderate",
  "family_history": "No",
  "language": "en"
}
```

**Response**:
```json
{
  "predictions": [
    {
      "rank": 1,
      "disease": "Fungal infection",
      "disease_fr": "Infection fongique",
      "probability": 45.23,
      "specialist": "Dermatologist",
      "specialist_fr": "Dermatologue",
      "specialist_probability": 42.15,
      "description": "..."
    }
  ],
  "metadata": {
    "symptoms_count": 3,
    "profile_used": true
  }
}
```

### GET `/health`
**Purpose**: Health check endpoint

**Response**: `200 OK`

### GET `/metadata`
**Purpose**: Get model metadata

---

## Prediction Process

1. **Load Model**: Load trained scikit-learn model from disk
2. **Prepare Features**: Convert symptoms and profile to feature vector
3. **Predict**: Run model inference
4. **Translate**: Localize disease and specialist names
5. **Rank**: Sort predictions by probability
6. **Return**: Top 5 predictions

---

## Model Configuration

- **Model Format**: `.joblib` (scikit-learn)
- **Model Location**: `/app/models/`
- **Translation Data**: `/app/data/translations.json`
- **Training**: The Docker image does **not** train the model; artefacts (models, translations) must be produced before building the image. See `BUILD.md` in the service root and the **Training** section below.

---

## Training (outside container)

Training is run on the host (or CI), not inside the container.

1. **Entry point**: `python training/train_model.py` (from `MlPredictionService/`).
2. **Steps**: Load dataset → clean symptoms → generate synthetic patient profiles → map disease → specialist → build features → stratified train/test split → train RandomForest → evaluate → save `.joblib` and optional confusion matrix PNGs.
3. **Outputs**: `models/model.joblib`, `models/mlb.joblib`, `models/scaler.joblib`, `models/le_disease.joblib`, `models/le_specialist.joblib`, `models/feature_columns.joblib`. If matplotlib is installed: `models/confusion_matrix_disease.png`, `models/confusion_matrix_specialist.png`.
4. **Evaluation**: Top-1 and top-k accuracy for disease and specialist; classification reports; confusion matrices (text + optional PNG). See `training/evaluation.py`.

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

The service uses a combination of symptom–disease data, disease–specialist mapping, and (where applicable) synthetic patient profiles derived from these sources. See `MlPredictionService/DATA_SOURCES.md` for the same references in the repository.

---

## See Also
- [DiagnoCare Service](04-diagnocare-service.md) - ML integration
- [Architecture Overview](01-architecture-overview.md)
- **In-repo**: `MlPredictionService/BUILD.md` for build and training instructions
- **In-repo**: `MlPredictionService/DATA_SOURCES.md` for data sources and references
