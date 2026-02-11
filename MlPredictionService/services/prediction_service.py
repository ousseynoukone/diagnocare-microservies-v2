"""
Service de prédiction ML pour les maladies et spécialistes
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


class PredictionService:
    """
    Service pour effectuer des prédictions de maladies et spécialistes
    """
    
    def __init__(
        self,
        model_repository: ModelRepository,
        translation_service: TranslationService,
        nlp_service: NLPService
    ):
        """
        Initialise le service de prédiction
        Args:
            model_repository: Repository pour accéder aux modèles ML
            translation_service: Service pour les traductions
            nlp_service: Service NLP pour les explications
        """
        self.model_repository = model_repository
        self.translation_service = translation_service
        self.nlp_service = nlp_service
        self.text_utils = TextUtils()
        self.logger = logging.getLogger(__name__)
    
    def predict(self, request: PredictionRequest) -> PredictionResponse:
        """
        Effectue une prédiction basée sur les symptômes et le profil patient
        Args:
            request: Requête de prédiction
        Returns:
            PredictionResponse: Réponse avec les prédictions
        """
        # Validation
        is_valid, error_msg = request.validate()
        if not is_valid:
            raise ValueError(error_msg)
        
        # Nettoyage et validation des symptômes
        cleaned_symptoms = [
            self.text_utils.clean_text(s) 
            for s in request.symptoms
        ]
        mlb = self.model_repository.mlb
        cleaned_symptoms = [
            s for s in cleaned_symptoms 
            if s in mlb.classes_
        ]
        
        if not cleaned_symptoms:
            raise ValueError("No valid symptoms provided")
        
        # Encodage des symptômes
        symptoms_encoded = mlb.transform([cleaned_symptoms])
        df_symptoms = pd.DataFrame(symptoms_encoded, columns=mlb.classes_)
        
        # Préparation du profil patient avec valeurs par défaut
        profile_data = self._prepare_profile_data(request)
        
        # Normalisation des features numériques
        df_numerical = self._prepare_numerical_features(profile_data)
        
        # Encodage des features catégorielles
        df_categorical = self._prepare_categorical_features(profile_data)
        
        # Combinaison de toutes les features
        df_final = self._combine_features(df_symptoms, df_numerical, df_categorical)
        
        # Prédiction
        model = self.model_repository.model
        probs = model.predict_proba(df_final)
        disease_probs = probs[0][0]
        specialist_probs = probs[1][0]
        
        # Top 5 maladies
        top_diseases_indices = disease_probs.argsort()[-5:][::-1]
        results = []
        
        le_disease = self.model_repository.le_disease
        le_specialist = self.model_repository.le_specialist
        
        for i, idx in enumerate(top_diseases_indices):
            disease_name = le_disease.inverse_transform([idx])[0]
            probability = disease_probs[idx]
            
            # Meilleur spécialiste pour ce rang de maladie
            specialist_idx = specialist_probs.argsort()[-5:][::-1][i]
            specialist_name = le_specialist.inverse_transform([specialist_idx])[0]
            specialist_prob = specialist_probs[specialist_idx]
            
            # Traduction selon la langue demandée
            disease_name_translated = self.translation_service.translate_disease(
                disease_name, target_lang=request.language
            )
            specialist_name_translated = self.translation_service.translate_specialist(
                specialist_name, target_lang=request.language
            )
            
            # Génération de l'explication
            explanation = self.nlp_service.generate_prediction_explanation(
                disease_name_translated,
                float(probability * 100),
                specialist_name_translated,
                cleaned_symptoms,
                self.translation_service,
                language=request.language
            )
            
            # Construction du résultat
            result = PredictionResult(
                rank=i + 1,
                disease=disease_name,
                probability=float(probability * 100),
                specialist=specialist_name,
                specialist_probability=float(specialist_prob * 100),
                description=explanation
            )
            
            # Ajout des traductions selon la langue
            if request.language == 'fr':
                result.disease_fr = disease_name_translated
                result.specialist_fr = specialist_name_translated
            else:
                result.disease_en = disease_name_translated
                result.specialist_en = specialist_name_translated
            
            results.append(result)
        
        # Métadonnées
        metadata = {
            "symptoms_count": len(cleaned_symptoms),
            "profile_used": profile_data
        }
        
        return PredictionResponse(
            predictions=results,
            language=request.language,
            metadata=metadata
        )
    
    def _prepare_profile_data(self, request: PredictionRequest) -> Dict:
        """
        Prépare les données du profil patient avec valeurs par défaut
        Args:
            request: Requête de prédiction
        Returns:
            Dict: Données du profil patient
        """
        # Calcul du BMI si non fourni
        bmi = request.bmi
        if bmi is None:
            height = request.height or 170  # cm
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
        """
        Prépare et normalise les features numériques
        Args:
            profile_data: Données du profil patient
        Returns:
            pd.DataFrame: Features numériques normalisées
        """
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
        """
        Prépare les features catégorielles en one-hot encoding
        Args:
            profile_data: Données du profil patient
        Returns:
            pd.DataFrame: Features catégorielles encodées
        """
        categorical_cols = [
            'Gender', 'Blood Pressure', 'Cholesterol Level', 'Outcome Variable',
            'Smoking', 'Alcohol', 'Sedentarite', 'Family_History'
        ]
        
        feature_columns = self.model_repository.feature_columns
        
        # Initialisation de toutes les features one-hot à False
        one_hot_features = {}
        for col in feature_columns:
            for cat_col in categorical_cols:
                if col.startswith(f"{cat_col}_"):
                    one_hot_features[col] = False
        
        # Mise à True des features correspondantes
        for cat_col in categorical_cols:
            value = profile_data[cat_col]
            key = f"{cat_col}_{value}"
            if key in one_hot_features:
                one_hot_features[key] = True
        
        df_categorical = pd.DataFrame([one_hot_features])
        return df_categorical
    
    def _combine_features(
        self,
        df_symptoms: pd.DataFrame,
        df_numerical: pd.DataFrame,
        df_categorical: pd.DataFrame
    ) -> pd.DataFrame:
        """
        Combine toutes les features et s'assure de l'ordre correct des colonnes
        Args:
            df_symptoms: Features des symptômes
            df_numerical: Features numériques normalisées
            df_categorical: Features catégorielles encodées
        Returns:
            pd.DataFrame: DataFrame final avec toutes les features dans le bon ordre
        """
        df_combined = pd.concat([df_symptoms, df_numerical, df_categorical], axis=1)
        feature_columns = self.model_repository.feature_columns
        df_final = df_combined.reindex(columns=feature_columns, fill_value=0)
        return df_final