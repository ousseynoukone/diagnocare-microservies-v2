"""
Service de prediction ML pour les maladies et specialistes.

Utilise TF-IDF pour encoder les symptomes (au lieu du binaire 0/1),
XGBoost calibre pour la prediction, et des feature interactions
(profil x symptomes cles) pour que le profil patient pese davantage.
"""
import pandas as pd
import numpy as np
import logging
from typing import Dict, List, Optional
from repositories.model_repository import ModelRepository
from services.translation_service import TranslationService
from services.nlp_service import NLPService
from models.prediction_request import PredictionRequest
from models.prediction_response import PredictionResponse, PredictionResult
from utils.text_utils import TextUtils

KEY_SYMPTOMS_FOR_INTERACTIONS = [
    'chest_pain', 'breathlessness', 'high_fever', 'fatigue',
    'weight_loss', 'weight_gain', 'headache', 'joint_pain',
    'skin_rash', 'vomiting', 'dizziness', 'sweating',
]


class PredictionService:

    def __init__(
        self,
        model_repository: ModelRepository,
        translation_service: TranslationService,
        nlp_service: NLPService
    ):
        self.model_repository = model_repository
        self.translation_service = translation_service
        self.nlp_service = nlp_service
        self.text_utils = TextUtils()
        self.logger = logging.getLogger(__name__)

    def predict(self, request: PredictionRequest) -> PredictionResponse:
        is_valid, error_msg = request.validate()
        if not is_valid:
            raise ValueError(error_msg)

        mlb = self.model_repository.mlb
        tfidf = self.model_repository.tfidf
        model = self.model_repository.model
        if mlb is None or model is None:
            raise ValueError("ML models are not loaded. Please ensure models are trained and available.")

        # Nettoyage et validation des symptomes
        cleaned_symptoms = [
            self.text_utils.clean_text(s)
            for s in request.symptoms
        ]
        cleaned_symptoms = [
            s for s in cleaned_symptoms
            if s in mlb.classes_
        ]

        if not cleaned_symptoms:
            raise ValueError("No valid symptoms provided")

        # TF-IDF encoding des symptomes
        if tfidf is not None:
            symptom_doc = ' '.join(cleaned_symptoms)
            X_tfidf = tfidf.transform([symptom_doc])
            tfidf_features = tfidf.get_feature_names_out().tolist()
            df_symptoms = pd.DataFrame(X_tfidf.toarray(), columns=tfidf_features)
        else:
            # Fallback binaire si pas de tfidf (ancien modele)
            symptoms_encoded = mlb.transform([cleaned_symptoms])
            df_symptoms = pd.DataFrame(symptoms_encoded, columns=mlb.classes_)

        # Profil patient
        profile_data = self._prepare_profile_data(request)
        df_numerical = self._prepare_numerical_features(profile_data)
        df_categorical = self._prepare_categorical_features(profile_data)

        # Feature interactions
        df_interactions = self._build_interactions(df_symptoms, profile_data)

        # Combinaison de toutes les features
        df_final = self._combine_features(df_symptoms, df_numerical, df_categorical, df_interactions)

        # Prediction
        probs = model.predict_proba(df_final)
        disease_probs = probs[0][0]
        specialist_probs = probs[1][0]

        # Top 5 maladies
        top_diseases_indices = disease_probs.argsort()[-5:][::-1]
        results = []

        le_disease = self.model_repository.le_disease
        le_specialist = self.model_repository.le_specialist

        disease_specialist_map = self.model_repository.disease_specialist_map

        for i, idx in enumerate(top_diseases_indices):
            disease_name = le_disease.inverse_transform([idx])[0]
            probability = disease_probs[idx]

            # Résoudre le spécialiste via le mapping maladie->spécialiste plutôt que
            # par rang indépendant (évite la désynchronisation des deux sorties du modèle).
            disease_clean = self.text_utils.clean_text(disease_name)
            mapped_specialist = disease_specialist_map.get(disease_clean)

            if mapped_specialist is not None:
                try:
                    specialist_idx = le_specialist.transform([mapped_specialist])[0]
                    specialist_name = mapped_specialist
                    specialist_prob = specialist_probs[specialist_idx]
                except ValueError:
                    self.logger.warning(
                        f"Spécialiste '{mapped_specialist}' absent du LabelEncoder, "
                        f"fallback sur la proba maximale."
                    )
                    specialist_idx = int(specialist_probs.argmax())
                    specialist_name = le_specialist.inverse_transform([specialist_idx])[0]
                    specialist_prob = specialist_probs[specialist_idx]
            else:
                # Fallback : spécialiste avec la probabilité la plus élevée
                self.logger.warning(
                    f"Aucun mapping pour la maladie '{disease_name}' ({disease_clean}), "
                    f"fallback sur la proba maximale."
                )
                specialist_idx = int(specialist_probs.argmax())
                specialist_name = le_specialist.inverse_transform([specialist_idx])[0]
                specialist_prob = specialist_probs[specialist_idx]

            disease_name_translated = self.translation_service.translate_disease(
                disease_name, target_lang=request.language
            )
            specialist_name_translated = self.translation_service.translate_specialist(
                specialist_name, target_lang=request.language
            )

            explanation = self.nlp_service.generate_prediction_explanation(
                disease_name_translated,
                float(probability * 100),
                specialist_name_translated,
                cleaned_symptoms,
                self.translation_service,
                language=request.language
            )

            result = PredictionResult(
                rank=i + 1,
                disease=disease_name_translated if request.language == 'fr' else disease_name,
                probability=float(probability * 100),
                specialist=specialist_name_translated if request.language == 'fr' else specialist_name,
                specialist_probability=float(specialist_prob * 100),
                description=explanation
            )

            result.disease_en = disease_name
            result.specialist_en = specialist_name
            result.disease_fr = disease_name_translated
            result.specialist_fr = specialist_name_translated

            results.append(result)

        metadata = {
            "symptoms_count": len(cleaned_symptoms),
            "profile_used": profile_data
        }

        top_prob = disease_probs[top_diseases_indices[0]] * 100
        if top_prob < 40:
            confidence_level = "low"
            confidence_note = (
                "La confiance du modele est faible. Veuillez preciser vos symptomes "
                "ou consulter un medecin pour un diagnostic fiable."
            )
        elif top_prob < 70:
            confidence_level = "moderate"
            confidence_note = (
                "Plusieurs pathologies sont possibles avec des probabilites proches. "
                "Les resultats ci-dessous sont indicatifs."
            )
        else:
            confidence_level = "high"
            confidence_note = None

        return PredictionResponse(
            predictions=results,
            language=request.language,
            metadata=metadata,
            confidence_level=confidence_level,
            confidence_note=confidence_note,
        )

    def _prepare_profile_data(self, request: PredictionRequest) -> Dict:
        bmi = request.bmi
        if bmi is None:
            height = request.height or 170
            weight = request.weight or 75
            bmi = weight / ((height / 100) ** 2)

        return {
            'Age': request.age or 35,
            'Weight': request.weight or 75,
            'BMI': bmi,
            'Tension_Moyenne': request.tension_moyenne or 120,
            'Cholesterole_Moyen': request.cholesterole_moyen or 190,
            'Gender': request.gender or 'Male',
            'Blood Pressure': request.blood_pressure or 'Normal',
            'Cholesterol Level': request.cholesterol_level or 'Normal',
            'Outcome Variable': request.outcome_variable or 'Negative',
            'Smoking': request.smoking or 'No',
            'Alcohol': request.alcohol or 'None',
            'Sedentarite': request.sedentarite or 'Moderate',
            'Family_History': request.family_history or 'No'
        }

    def _prepare_numerical_features(self, profile_data: Dict) -> pd.DataFrame:
        df_profile_num = pd.DataFrame([{
            'Age': profile_data['Age'],
            'Weight': profile_data['Weight'],
            'BMI': profile_data['BMI'],
            'Tension_Moyenne': profile_data['Tension_Moyenne'],
            'Cholesterole_Moyen': profile_data['Cholesterole_Moyen']
        }])

        scaler = self.model_repository.scaler
        numerical_normalized = scaler.transform(df_profile_num)
        df_numerical = pd.DataFrame(
            numerical_normalized,
            columns=[f'{col}_normalized' for col in df_profile_num.columns]
        )

        return df_numerical

    def _prepare_categorical_features(self, profile_data: Dict) -> pd.DataFrame:
        categorical_cols = [
            'Gender', 'Blood Pressure', 'Cholesterol Level', 'Outcome Variable',
            'Smoking', 'Alcohol', 'Sedentarite', 'Family_History'
        ]

        feature_columns = self.model_repository.feature_columns

        one_hot_features = {}
        for col in feature_columns:
            for cat_col in categorical_cols:
                if col.startswith(f"{cat_col}_"):
                    one_hot_features[col] = False

        for cat_col in categorical_cols:
            value = profile_data[cat_col]
            key = f"{cat_col}_{value}"
            if key in one_hot_features:
                one_hot_features[key] = True

        df_categorical = pd.DataFrame([one_hot_features])
        return df_categorical

    def _build_interactions(self, df_symptoms: pd.DataFrame, profile_data: Dict) -> pd.DataFrame:
        """Memes interactions que pendant l'entrainement."""
        age_norm = (profile_data['Age'] - 38) / 16.0  # approx mean/std from training
        is_male = 1.0 if profile_data['Gender'] == 'Male' else 0.0
        is_smoker = 1.0 if profile_data['Smoking'] == 'Yes' else 0.0
        bp_high = 1.0 if profile_data['Blood Pressure'] == 'High' else 0.0

        profile_signals = {
            'age': age_norm,
            'male': is_male,
            'smoker': is_smoker,
            'bp_high': bp_high,
        }

        interactions = {}
        for symptom in KEY_SYMPTOMS_FOR_INTERACTIONS:
            if symptom in df_symptoms.columns:
                s_val = df_symptoms[symptom].values[0]
            else:
                s_val = 0.0
            for pname, pval in profile_signals.items():
                interactions[f'IX_{symptom}_x_{pname}'] = s_val * pval

        return pd.DataFrame([interactions])

    def _combine_features(
        self,
        df_symptoms: pd.DataFrame,
        df_numerical: pd.DataFrame,
        df_categorical: pd.DataFrame,
        df_interactions: pd.DataFrame,
    ) -> pd.DataFrame:
        df_combined = pd.concat([df_symptoms, df_numerical, df_categorical, df_interactions], axis=1)
        feature_columns = self.model_repository.feature_columns
        df_final = df_combined.reindex(columns=feature_columns, fill_value=0)
        return df_final
