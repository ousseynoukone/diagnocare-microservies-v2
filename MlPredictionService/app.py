from flask import Flask, request, jsonify
import joblib
import pandas as pd
import numpy as np
import os
import re
from nlp_service import (
    load_translations, 
    extract_symptoms_from_text,
    translate_disease,
    translate_specialist,
    generate_prediction_explanation
)

app = Flask(__name__)

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
    translations = {"diseases": {}, "specialists": {}}

# --- HELPER FUNCTIONS ---
def clean_text(text):
    if pd.isna(text): return ""
    return "_".join(re.sub(r'[^\w\s]', '', str(text).strip().lower()).split())

@app.route('/health', methods=['GET'])
def health_check():
    return jsonify({"status": "healthy", "service": "DiagnoCare ML API"}), 200

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
    try:
        data = request.json
        
        # Get language parameter (default: 'fr')
        language = data.get('language', 'fr').lower()
        if language not in ['fr', 'en']:
            language = 'fr'  # Default to French if invalid
        
        # 1. Parse Symptoms
        symptoms = data.get('symptoms', [])
        
        # If raw_description is provided, extract symptoms automatically
        raw_description = data.get('raw_description')
        if raw_description and (not symptoms or len(symptoms) == 0):
            available_symptoms = list(mlb.classes_)
            extracted = extract_symptoms_from_text(raw_description, available_symptoms, language=language)
            symptoms = extracted
        
        cleaned_symptoms = [clean_text(s) for s in symptoms]
        
        # Create symptom features
        symptoms_encoded = mlb.transform([cleaned_symptoms])
        df_symptoms = pd.DataFrame(symptoms_encoded, columns=mlb.classes_)
        
        # 2. Parse Profile Data (with default fallbacks if missing)
        # Defaults based on "General" profile from training script
        age = data.get('age', 35)
        weight = data.get('weight', 75)
        height = data.get('height', 170) # cm
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
            'Outcome Variable': data.get('outcome_variable', 'Negative'),
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
