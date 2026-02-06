from flask import Flask, request, jsonify
from flasgger import Swagger
import joblib
import pandas as pd
import numpy as np
import os
import re
import logging
import socket
try:
    from dotenv import load_dotenv
except Exception:
    load_dotenv = None
from flask_eureka import Eureka
from flask_eureka.eureka import eureka_bp
from flask_eureka.eurekaclient import EurekaClient
from nlp_service import (
    load_translations, 
    extract_symptoms_from_text,
    translate_disease,
    translate_specialist,
    generate_prediction_explanation,
    translate_symptom
)

if load_dotenv:
    load_dotenv()

logging.basicConfig(level=logging.INFO)

def _patch_eureka_client():
    """
    flask-eureka-client hardcodes securePort enabled=true and relies on ifconfig for ipAddr.
    Patch the instance data to disable secure port and use the configured hostname as ipAddr.
    """
    original_get_instance_data = EurekaClient.get_instance_data

    def _get_instance_data(self):
        data = original_get_instance_data(self)
        instance = data.get('instance', {})
        if self.host_name:
            instance['ipAddr'] = self.host_name
        secure_port = instance.get('securePort', {})
        secure_port['@enabled'] = 'false'
        secure_port['$'] = 443
        instance['securePort'] = secure_port
        data['instance'] = instance
        return data

    EurekaClient.get_instance_data = _get_instance_data

def _detect_local_ip():
    try:
        with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as s:
            s.connect(("8.8.8.8", 80))
            return s.getsockname()[0]
    except Exception:
        return "localhost"

SERVICE_NAME = os.getenv("SERVICE_NAME", "ml-prediction-service")
EUREKA_SERVICE_URL = os.getenv("EUREKA_SERVICE_URL", "http://localhost:8761")
EUREKA_INSTANCE_HOSTNAME = os.getenv("EUREKA_INSTANCE_HOSTNAME", _detect_local_ip())
EUREKA_INSTANCE_PORT = int(os.getenv("EUREKA_INSTANCE_PORT", "5000"))

app = Flask(__name__)
app.config["SERVICE_NAME"] = SERVICE_NAME
app.config["EUREKA_SERVICE_URL"] = EUREKA_SERVICE_URL
app.config["EUREKA_INSTANCE_HOSTNAME"] = EUREKA_INSTANCE_HOSTNAME
app.config["EUREKA_INSTANCE_PORT"] = EUREKA_INSTANCE_PORT
app.config["EUREKA_INSTANCE_IP_ADDRESS"] = EUREKA_INSTANCE_HOSTNAME
app.config["EUREKA_INSTANCE_SECURE_PORT_ENABLED"] = False
app.config["EUREKA_INSTANCE_NON_SECURE_PORT_ENABLED"] = True
app.config["SWAGGER"] = {
    "title": "DiagnoCare ML API",
    "uiversion": 3,
    "openapi": "3.0.2",
}
swagger_template = {
    "openapi": "3.0.2",
    "info": {
        "title": "DiagnoCare ML API",
        "version": "1.0"
    },
    "servers": [
        {"url": "http://localhost:5000", "description": "Local ML Service"},
        {"url": "http://localhost:8765", "description": "Gateway"}
    ]
}
Swagger(app, template=swagger_template)

app.logger.info("Initializing Eureka client...")
_patch_eureka_client()
eureka = Eureka(app)
app.register_blueprint(eureka_bp)
eureka.register_service(
    name=app.config["SERVICE_NAME"],
    vip_address=EUREKA_INSTANCE_HOSTNAME,
    secure_vip_address=EUREKA_INSTANCE_HOSTNAME,
)
app.logger.info("Eureka client initialized.")


# --- CONFIGURATION ---
MODELS_DIR = 'models'
TRANSLATIONS_FILE = 'data/translations.json'

# --- LOAD ARTIFACTS ---
try:
    print("Loading models...")
    model = joblib.load(os.path.join(MODELS_DIR, 'model.joblib'))
    mlb = joblib.load(os.path.join(MODELS_DIR, 'mlb.joblib'))
    scaler = joblib.load(os.path.join(MODELS_DIR, 'scaler.joblib'))
    le_disease = joblib.load(os.path.join(MODELS_DIR, 'le_disease.joblib'))
    le_specialist = joblib.load(os.path.join(MODELS_DIR, 'le_specialist.joblib'))
    feature_columns = joblib.load(os.path.join(MODELS_DIR, 'feature_columns.joblib'))
    print("Models loaded successfully.")
except Exception as e:
    print(f"Error loading models: {e}")
    print("Please run train_model.py first!")

# --- LOAD TRANSLATIONS ---
try:
    print("Loading translations...")
    translations = load_translations(TRANSLATIONS_FILE)
    print("Translations loaded successfully.")
except Exception as e:
    print(f"Error loading translations: {e}")
    translations = {"diseases": {}, "specialists": {}, "symptoms": {}}

# --- HELPER FUNCTIONS ---
def clean_text(text):
    if pd.isna(text):
        return ""
    normalized = str(text).strip().lower()
    normalized = normalized.replace("-", " ").replace("/", " ")
    return "_".join(re.sub(r'[^\w\s]', '', normalized).split())

@app.route('/health', methods=['GET'])
def health_check():
    """
    Health check
    ---
    tags:
      - Health
    responses:
      200:
        description: Service is healthy
    """
    return jsonify({"status": "healthy", "service": "DiagnoCare ML API"}), 200

@app.route('/features-metadata', methods=['GET'])
def features_metadata():
    """
    Get symptoms and feature metadata
    ---
    tags:
      - Metadata
    responses:
      200:
        description: Symptoms and feature definitions in EN/FR
    """
    symptoms = sorted([str(s) for s in mlb.classes_])
  
    symptoms_en = [
        {"id": s, "label": translate_symptom(s, translations, target_lang="en")}
        for s in symptoms
    ]
    symptoms_fr = [
        {"id": s, "label": translate_symptom(s, translations, target_lang="fr")}
        for s in symptoms
    ]

    numeric_features = [
        {"key": "age", "name_en": "Age", "name_fr": "Âge", "unit_en": "years", "unit_fr": "ans", "range": "10-80"},
        {"key": "weight", "name_en": "Weight", "name_fr": "Poids", "unit_en": "kg", "unit_fr": "kg", "range": "40-150"},
        {"key": "bmi", "name_en": "BMI", "name_fr": "IMC", "unit_en": "kg/m²", "unit_fr": "kg/m²", "range": "12-50"},
        {"key": "tension_moyenne", "name_en": "Mean blood pressure", "name_fr": "Tension moyenne", "unit_en": "mmHg", "unit_fr": "mmHg", "range": "80-180"},
        {"key": "cholesterole_moyen", "name_en": "Mean cholesterol", "name_fr": "Cholestérol moyen", "unit_en": "mg/dL", "unit_fr": "mg/dL", "range": "100-300"}
    ]

    categorical_features = [
        {"key": "gender", "name_en": "Gender", "name_fr": "Genre",
         "values_en": ["Male", "Female"], "values_fr": ["Homme", "Femme"]},
        {"key": "blood_pressure", "name_en": "Blood Pressure", "name_fr": "Pression artérielle",
         "values_en": ["Low", "Normal", "High"], "values_fr": ["Faible", "Normale", "Élevée"]},
        {"key": "cholesterol_level", "name_en": "Cholesterol Level", "name_fr": "Niveau de cholestérol",
         "values_en": ["Low", "Normal", "High"], "values_fr": ["Faible", "Normal", "Élevé"]},
        {"key": "outcome_variable", "name_en": "Outcome Variable", "name_fr": "Variable de résultat",
         "values_en": ["Negative", "Positive"], "values_fr": ["Négatif", "Positif"]},
        {"key": "smoking", "name_en": "Smoking", "name_fr": "Tabagisme",
         "values_en": ["No", "Yes"], "values_fr": ["Non", "Oui"]},
        {"key": "alcohol", "name_en": "Alcohol", "name_fr": "Alcool",
         "values_en": ["None", "Moderate", "Heavy"], "values_fr": ["Aucun", "Modéré", "Élevé"]},
        {"key": "sedentarite", "name_en": "Sedentariness", "name_fr": "Sédentarité",
         "values_en": ["Low", "Moderate", "High"], "values_fr": ["Faible", "Modérée", "Élevée"]},
        {"key": "family_history", "name_en": "Family History", "name_fr": "Antécédents familiaux",
         "values_en": ["No", "Yes"], "values_fr": ["Non", "Oui"]}
    ]

    return jsonify({
        "symptoms": {
            "count": len(symptoms),
            "en": symptoms_en,
            "fr": symptoms_fr
        },
        "features": {
            "numeric": numeric_features,
            "categorical": categorical_features
        },
        "languages": ["en", "fr"]
    }), 200

@app.route('/extract-symptoms', methods=['POST'])
def extract_symptoms():
    """Extract symptoms from raw text description"""
    try:
        data = request.json
        raw_description = data.get('raw_description', '')
        language = data.get('language', 'fr').lower()
        
        if not raw_description:
            return jsonify({"error": "raw_description is required"}), 400
        
        if language not in ['fr', 'en']:
            language = 'fr'  # Default to French
        
        # Get available symptoms from ML model
        available_symptoms = list(mlb.classes_)
        
        # Extract symptoms using NLP
        extracted_symptoms = extract_symptoms_from_text(
            raw_description, 
            available_symptoms,
            language=language
        )
        
        return jsonify({
            "symptoms": extracted_symptoms,
            "language": language
        }), 200
        
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/predict', methods=['POST'])
def predict():
    """
    Predict diseases and specialists
    ---
    tags:
      - Prediction
    requestBody:
      required: true
      content:
        application/json:
          schema:
            type: object
            required:
              - symptoms
            properties:
              symptoms:
                type: array
                items:
                  type: string
              language:
                type: string
                enum: [fr, en]
              age:
                type: integer
              weight:
                type: number
              bmi:
                type: number
              tension_moyenne:
                type: number
              cholesterole_moyen:
                type: number
              gender:
                type: string
              blood_pressure:
                type: string
              cholesterol_level:
                type: string
              outcome_variable:
                type: string
              smoking:
                type: string
              alcohol:
                type: string
              sedentarite:
                type: string
              family_history:
                type: string
    responses:
      200:
        description: Predictions returned
      400:
        description: Invalid input
    """
    try:
        data = request.json
        
        # Get language parameter (default: 'fr')
        language = data.get('language', 'fr').lower()
        if language not in ['fr', 'en']:
            language = 'fr'  # Default to French if invalid
        
        # 1. Parse Symptoms (English only)
        symptoms = data.get('symptoms', [])
        if isinstance(symptoms, str):
            return jsonify({"error": "symptoms must be an array of strings"}), 400
        if not symptoms:
            return jsonify({"error": "symptoms is required and must be in English"}), 400
        
        cleaned_symptoms = [clean_text(s) for s in symptoms]
        cleaned_symptoms = [s for s in cleaned_symptoms if s in mlb.classes_]
        if not cleaned_symptoms:
            return jsonify({"error": "No valid symptoms provided"}), 400
        
        # Create symptom features
        symptoms_encoded = mlb.transform([cleaned_symptoms])
        df_symptoms = pd.DataFrame(symptoms_encoded, columns=mlb.classes_)
        
        # 2. Parse Profile Data (with default fallbacks if missing)
        # Defaults based on "General" profile from training script
        age = data.get('age', 35)
        weight = data.get('weight', 75)
        bmi = data.get('bmi')
        if bmi is None:
            bmi = data.get('IMC')
        if bmi is None:
            height = data.get('height', 170) # cm (legacy input)
            bmi = weight / ((height/100)**2)
        
        # Approximate vitals if not provided
        tension_moyenne = data.get('tension_moyenne', 120)
        cholesterole_moyen = data.get('cholesterole_moyen', 190)
        
        # Create DataFrame for numerical features to scale
        # Columns must match training: ['Age', 'Weight', 'BMI', 'Tension_Moyenne', 'Cholesterole_Moyen']
        df_profile_num = pd.DataFrame([{
            'Age': age,
            'Weight': weight,
            'BMI': bmi,
            'Tension_Moyenne': tension_moyenne,
            'Cholesterole_Moyen': cholesterole_moyen
        }])
        
        numerical_normalized = scaler.transform(df_profile_num)
        df_numerical = pd.DataFrame(numerical_normalized, columns=[f'{col}_normalized' for col in df_profile_num.columns])
        
        # 3. Categorical Features
        # We need to construct the exact dataframe shape expected by the model
        # The training script used get_dummies. We need to manually recreate the 0/1 columns.
        
        categorical_cols = ['Gender', 'Blood Pressure', 'Cholesterol Level', 'Outcome Variable', 
                            'Smoking', 'Alcohol', 'Sedentarite', 'Family_History']
        
        inputs = {
            'Gender': data.get('gender', 'Male'),
            'Blood Pressure': data.get('blood_pressure', 'Normal'),
            'Cholesterol Level': data.get('cholesterol_level', 'Normal'),
            'Outcome Variable': data.get('outcome_variable', 'Negative') or 'Negative',
            'Smoking': data.get('smoking', 'No'),
            'Alcohol': data.get('alcohol', 'None'),
            'Sedentarite': data.get('sedentarite', 'Moderate'),
            'Family_History': data.get('family_history', 'No')
        }
        
        # Create a single-row dict with all possible one-hot keys initialized to 0/False
        # We can find these keys by looking at feature_columns that start with the prefix
        one_hot_features = {}
        for col in feature_columns:
            for cat_col in categorical_cols:
                print(cat_col)
                if col.startswith(f"{cat_col}_"):
                    one_hot_features[col] = False
        
        # Set the matching keys to True
        for cat_col, value in inputs.items():
            key = f"{cat_col}_{value}"
            if key in one_hot_features:
                one_hot_features[key] = True
            
        df_categorical = pd.DataFrame([one_hot_features])
        
        # 4. Combine all features
        # We concat and then reindex to ensure exact column order and presence
        df_combined = pd.concat([df_symptoms, df_numerical, df_categorical], axis=1)
        
        # Reindex to match training feature columns exactly, filling missing interactions with 0 if any
        df_final = df_combined.reindex(columns=feature_columns, fill_value=0)
        
        # 5. Predict
        probs = model.predict_proba(df_final)
        disease_probs = probs[0][0]
        specialist_probs = probs[1][0]
        
        # Top 5 Diseases
        top_diseases_indices = disease_probs.argsort()[-5:][::-1]
        results = []
        
        for i, idx in enumerate(top_diseases_indices):
            disease_name = le_disease.inverse_transform([idx])[0]
            probability = disease_probs[idx]
            
            # Find best specialist for this disease rank (simplified approach matches training script logic)
            # In training, we saw specialist matches disease rank. Here we can pick top specialist too.
            specialist_idx = specialist_probs.argsort()[-5:][::-1][i] 
            specialist_name = le_specialist.inverse_transform([specialist_idx])[0]
            specialist_prob = specialist_probs[specialist_idx]
            
            # Translate according to requested language
            disease_name_translated = translate_disease(disease_name, translations, target_lang=language)
            specialist_name_translated = translate_specialist(specialist_name, translations, target_lang=language)
            
            # Generate explanation/description in requested language
            explanation = generate_prediction_explanation(
                disease_name_translated, 
                float(probability * 100), 
                specialist_name_translated, 
                cleaned_symptoms,
                translations,
                language=language
            )
            
            # Build result with both original (EN) and translated names
            result = {
                'rank': i + 1,
                'disease': disease_name,  # EN (original, always present)
                'probability': float(probability * 100),
                'specialist': specialist_name,  # EN (original, always present)
                'specialist_probability': float(specialist_prob * 100),
                'description': explanation  # Explanation in requested language
            }
            
            # Add translated names according to language
            if language == 'fr':
                result['disease_fr'] = disease_name_translated
                result['specialist_fr'] = specialist_name_translated
            else:  # English
                result['disease_en'] = disease_name_translated  # Same as disease
                result['specialist_en'] = specialist_name_translated  # Same as specialist
            
            results.append(result)
            
        return jsonify({
            "predictions": results,
            "language": language,
            "metadata": {
                "symptoms_count": len(cleaned_symptoms),
                "profile_used": inputs
            }
        })

    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
