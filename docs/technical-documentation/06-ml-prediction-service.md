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
│   ├── prediction_controller.py   # /predict endpoint
│   ├── health_controller.py         # /health endpoint
│   └── metadata_controller.py      # /metadata endpoint
├── services/
│   ├── prediction_service.py       # Prediction logic
│   ├── translation_service.py      # Multi-language support
│   └── nlp_service.py              # NLP symptom extraction
├── repositories/
│   ├── model_repository.py          # Model loading
│   └── translation_repository.py  # Translation data
├── models/
│   ├── prediction_request.py       # Request DTOs
│   └── prediction_response.py      # Response DTOs
└── utils/
    └── text_utils.py               # Text utilities
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

---

## Multi-Language Support

- **Languages**: French (fr), English (en)
- **Translation**: Disease names and specialist labels
- **Default**: English

---

## See Also
- [DiagnoCare Service](04-diagnocare-service.md) - ML integration
- [Architecture Overview](01-architecture-overview.md)
