import pandas as pd
import numpy as np
import re
import joblib
import os
from sklearn.preprocessing import MultiLabelBinarizer, LabelEncoder, StandardScaler
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import accuracy_score

# --- CONFIGURATION ---
DATASET_PATH = 'data/dataset.csv'
MAPPING_PATH = 'data/Doctor_Versus_Disease.csv'
MODELS_DIR = 'models'

# Ensure models directory exists
os.makedirs(MODELS_DIR, exist_ok=True)

# --- HELPER FUNCTIONS ---
def clean_text(text):
    if pd.isna(text): return ""
    return "_".join(re.sub(r'[^\w\s]', '', str(text).strip().lower()).split())

def generate_synthetic_profile(disease_name):
    """Génère un profil patient synthétique basé sur le type de maladie"""
    disease_lower = disease_name.lower()
    
    # Âge selon le type de maladie
    if any(word in disease_lower for word in ['heart', 'cardiac', 'hypertension', 'myocardial']):
        age = int(np.random.normal(55, 10))
        age = max(40, min(80, age))
        bp = np.random.choice(['Low', 'Normal', 'High'], p=[0.1, 0.2, 0.7])
        chol = np.random.choice(['Low', 'Normal', 'High'], p=[0.1, 0.3, 0.6])
        smoking = np.random.choice(['No', 'Yes'], p=[0.3, 0.7])
        sedentarite = np.random.choice(['Low', 'Moderate', 'High'], p=[0.2, 0.3, 0.5])
    elif any(word in disease_lower for word in ['acne', 'chicken pox', 'rubella']):
        age = int(np.random.normal(20, 8))
        age = max(10, min(35, age))
        bp = np.random.choice(['Low', 'Normal', 'High'], p=[0.3, 0.5, 0.2])
        chol = np.random.choice(['Low', 'Normal', 'High'], p=[0.4, 0.5, 0.1])
        smoking = np.random.choice(['No', 'Yes'], p=[0.7, 0.3])
        sedentarite = np.random.choice(['Low', 'Moderate', 'High'], p=[0.5, 0.3, 0.2])
    elif any(word in disease_lower for word in ['diabetes', 'thyroid']):
        age = int(np.random.normal(45, 12))
        age = max(25, min(70, age))
        bp = np.random.choice(['Low', 'Normal', 'High'], p=[0.2, 0.4, 0.4])
        chol = np.random.choice(['Low', 'Normal', 'High'], p=[0.2, 0.4, 0.4])
        smoking = np.random.choice(['No', 'Yes'], p=[0.5, 0.5])
        sedentarite = np.random.choice(['Low', 'Moderate', 'High'], p=[0.2, 0.4, 0.4])
    else:
        age = int(np.random.normal(35, 15))
        age = max(18, min(75, age))
        bp = np.random.choice(['Low', 'Normal', 'High'], p=[0.25, 0.5, 0.25])
        chol = np.random.choice(['Low', 'Normal', 'High'], p=[0.25, 0.5, 0.25])
        smoking = np.random.choice(['No', 'Yes'], p=[0.6, 0.4])
        sedentarite = np.random.choice(['Low', 'Moderate', 'High'], p=[0.3, 0.4, 0.3])
    
    # Genre
    if 'urinary tract' in disease_lower or 'uti' in disease_lower:
        gender = np.random.choice(['Male', 'Female'], p=[0.3, 0.7])
    else:
        gender = np.random.choice(['Male', 'Female'], p=[0.5, 0.5])
    
    # Poids & BMI
    if gender == 'Male':
        base_weight = 75 + (age - 35) * 0.3
        avg_height = 175
    else:
        base_weight = 65 + (age - 35) * 0.2
        avg_height = 162
    
    if any(word in disease_lower for word in ['diabetes', 'heart', 'cardiac', 'hypertension']):
        weight = int(np.random.normal(base_weight + 10, 12))
    elif any(word in disease_lower for word in ['acne', 'chicken pox', 'rubella']):
        weight = int(np.random.normal(base_weight - 5, 8))
    else:
        weight = int(np.random.normal(base_weight, 10))
    
    weight = max(40, min(150, weight))
    bmi = weight / ((avg_height / 100) ** 2)
    
    # Tension Moyenne
    if bp == 'High':
        tension_moyenne = np.random.normal(145, 10)
    elif bp == 'Low':
        tension_moyenne = np.random.normal(100, 8)
    else:
        tension_moyenne = np.random.normal(120, 8)
    tension_moyenne = max(80, min(180, int(tension_moyenne)))
    
    # Cholestérol Moyen
    if chol == 'High':
        cholesterole_moyen = np.random.normal(240, 20)
    elif chol == 'Low':
        cholesterole_moyen = np.random.normal(150, 15)
    else:
        cholesterole_moyen = np.random.normal(190, 15)
    cholesterole_moyen = max(100, min(300, int(cholesterole_moyen)))
    
    # Alcool
    if any(word in disease_lower for word in ['heart', 'cardiac', 'liver']):
        alcohol = np.random.choice(['None', 'Moderate', 'Heavy'], p=[0.4, 0.4, 0.2])
    else:
        alcohol = np.random.choice(['None', 'Moderate', 'Heavy'], p=[0.5, 0.4, 0.1])
    
    # Antécédents familiaux
    if any(word in disease_lower for word in ['diabetes', 'heart', 'cardiac', 'cancer']):
        family_history = np.random.choice(['No', 'Yes'], p=[0.3, 0.7])
    else:
        family_history = np.random.choice(['No', 'Yes'], p=[0.6, 0.4])
    
    # Outcome
    if any(word in disease_lower for word in ['cancer', 'stroke', 'heart attack']):
        outcome = np.random.choice(['Negative', 'Positive'], p=[0.2, 0.8])
    else:
        outcome = np.random.choice(['Negative', 'Positive'], p=[0.5, 0.5])
    
    return {
        'Age': age,
        'Gender': gender,
        'Blood Pressure': bp,
        'Cholesterol Level': chol,
        'Outcome Variable': outcome,
        'Smoking': smoking,
        'Weight': weight,
        'BMI': round(bmi, 1),
        'Tension_Moyenne': tension_moyenne,
        'Cholesterole_Moyen': cholesterole_moyen,
        'Alcohol': alcohol,
        'Sedentarite': sedentarite,
        'Family_History': family_history
    }

def main():
    print("1. Loading dataset...")
    df = pd.read_csv(DATASET_PATH)
    
    # Fix dataset if it has leading/trailing spaces in column names
    df.columns = df.columns.str.strip()
    
    # Identify symptom columns (ignoring 'Disease' and any other non-symptom cols if present)
    symptom_cols = [c for c in df.columns if 'Symptom' in c]
    print(f"   - Found {len(symptom_cols)} symptom columns")

    print("\n2. Processing symptoms...")
    symptom_lists = []
    for _, row in df.iterrows():
        cleaned = [clean_text(row[col]) for col in symptom_cols if pd.notna(row[col])]
        symptom_lists.append([s for s in cleaned if s])

    mlb = MultiLabelBinarizer()
    X_symptoms = mlb.fit_transform(symptom_lists)
    df_symptoms = pd.DataFrame(X_symptoms, columns=mlb.classes_)
    print(f"   - Encoded {len(mlb.classes_)} unique symptoms")

    print("\n3. Generating synthetic patient profiles...")
    np.random.seed(42)
    profiles = [generate_synthetic_profile(row['Disease']) for _, row in df.iterrows()]
    df_profiles = pd.DataFrame(profiles)
    
    print("\n4. Mapping Disease -> Specialist...")
    df_map = pd.read_csv(MAPPING_PATH, header=None, names=['Disease', 'Specialist'], encoding='cp1252')
    df_map['Disease_clean'] = df_map['Disease'].apply(clean_text)
    mapping_dict = dict(zip(df_map['Disease_clean'], df_map['Specialist']))

    df['Disease_clean'] = df['Disease'].apply(clean_text)
    df['Target_Specialist'] = df['Disease_clean'].map(mapping_dict)
    
    # Drop rows without specialist mapping
    df = df[df['Target_Specialist'].notna()]
    print(f"   - {len(df)} samples matched with specialists")

    print("\n5. Combining features...")
    # Normalize numerical features
    numerical_cols = ['Age', 'Weight', 'BMI', 'Tension_Moyenne', 'Cholesterole_Moyen']
    scaler = StandardScaler()
    # Fit only on the subset that matches specialists
    df_profiles_subset = df_profiles.iloc[df.index].reset_index(drop=True)
    
    numerical_normalized = scaler.fit_transform(df_profiles_subset[numerical_cols])
    df_numerical = pd.DataFrame(numerical_normalized, columns=[f'{col}_normalized' for col in numerical_cols])

    # One-hot encode categorical features
    categorical_cols = ['Gender', 'Blood Pressure', 'Cholesterol Level', 'Outcome Variable', 
                        'Smoking', 'Alcohol', 'Sedentarite', 'Family_History']
    df_categorical = pd.get_dummies(df_profiles_subset[categorical_cols], prefix=categorical_cols)

    # Combine
    # df has been filtered. df_symptoms needs to be filtered too.
    df_symptoms_subset = df_symptoms.iloc[df.index].reset_index(drop=True)
    
    df_features = pd.concat([df_symptoms_subset, df_numerical, df_categorical], axis=1)
    
    # Targets
    le_disease = LabelEncoder()
    le_specialist = LabelEncoder()
    
    y_disease = le_disease.fit_transform(df['Disease'])
    y_specialist = le_specialist.fit_transform(df['Target_Specialist'])
    Y_combined = np.column_stack((y_disease, y_specialist))

    print(f"   - Final feature shape: {df_features.shape}")

    print("\n6. Training Model...")
    X_train, X_test, Y_train, Y_test = train_test_split(
        df_features, Y_combined, test_size=0.2, random_state=42
    )

    model = RandomForestClassifier(
        n_estimators=200,
        max_depth=15,
        min_samples_split=10,
        min_samples_leaf=5,
        random_state=42
    )
    model.fit(X_train, Y_train)
    
    # Quick eval
    Y_pred = model.predict(X_test)
    acc_disease = accuracy_score(Y_test[:, 0], Y_pred[:, 0])
    acc_specialist = accuracy_score(Y_test[:, 1], Y_pred[:, 1])
    print(f"   - Disease Accuracy: {acc_disease*100:.2f}%")
    print(f"   - Specialist Accuracy: {acc_specialist*100:.2f}%")

    print("\n7. Saving Artifacts...")
    joblib.dump(model, os.path.join(MODELS_DIR, 'model.joblib'))
    joblib.dump(mlb, os.path.join(MODELS_DIR, 'mlb.joblib'))
    joblib.dump(scaler, os.path.join(MODELS_DIR, 'scaler.joblib'))
    joblib.dump(le_disease, os.path.join(MODELS_DIR, 'le_disease.joblib'))
    joblib.dump(le_specialist, os.path.join(MODELS_DIR, 'le_specialist.joblib'))
    
    # Save column names to ensure consistent order during inference
    feature_columns = df_features.columns.tolist()
    joblib.dump(feature_columns, os.path.join(MODELS_DIR, 'feature_columns.joblib'))
    
    print(f"   - Saved all artifacts to {MODELS_DIR}/")

if __name__ == "__main__":
    main()
