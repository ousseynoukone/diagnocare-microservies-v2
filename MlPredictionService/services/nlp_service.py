"""
Service NLP pour l'extraction de symptômes depuis du texte
"""
import re
import logging
from typing import List, Dict
from fuzzywuzzy import fuzz

# Tentative d'import de spacy avec fallback
try:
    import spacy
    from spacy.matcher import PhraseMatcher
    nlp_spacy = None
    try:
        nlp_spacy = spacy.load("fr_core_news_sm")
    except OSError:
        try:
            nlp_spacy = spacy.load("en_core_web_sm")
        except OSError:
            logging.warning("Modèles spaCy non trouvés. Utilisation du traitement NLP de base.")
except Exception as exc:
    nlp_spacy = None
    logging.warning(f"spaCy indisponible ({exc}). Utilisation du traitement NLP de base.")

from utils.text_utils import TextUtils


class NLPService:
    """
    Service pour l'extraction de symptômes depuis du texte naturel
    """
    
    def __init__(self):
        """Initialise le service NLP"""
        self.text_utils = TextUtils()
        self.logger = logging.getLogger(__name__)
        self._init_synonyms()
    
    def _init_synonyms(self):
        """Initialise les synonymes pour le matching"""
        # Synonymes français -> symptôme canonique anglais
        self.fr_synonyms = {
            "fièvre": "fever", "fievre": "fever", "fiévreux": "fever", "fiévreuse": "fever",
            "fébrile": "fever", "frissons": "chills", "toux": "cough", "toux sèche": "cough",
            "toux seche": "cough", "fatigue": "fatigue", "épuisement": "fatigue",
            "maux de tête": "headache", "mal de tête": "headache", "mal de tete": "headache",
            "céphalée": "headache", "coup de barre": "fatigue", "douleur": "pain",
            "douleurs": "pain", "douleurs musculaires": "muscle_pain", "courbatures": "muscle_pain",
            "nausée": "nausea", "nausee": "nausea", "vomissement": "vomiting",
            "vomissements": "vomiting", "diarrhée": "diarrhea", "diarrhées": "diarrhea",
            "constipation": "constipation", "démangeaison": "itching", "démangeaisons": "itching",
            "éruption": "skin_rash", "éruption cutanée": "skin_rash", "éruptions": "skin_rash",
            "rougeur": "skin_rash", "rougeurs": "skin_rash", "gorge irritée": "sore_throat",
            "gorge irritee": "sore_throat", "mal de gorge": "sore_throat", "nez bouché": "congestion",
            "nez bouche": "congestion", "congestion": "congestion", "vertige": "dizziness",
            "vertiges": "dizziness", "faiblesse": "weakness", "malaise": "malaise",
            "perte d'appétit": "loss_of_appetite", "perte d appetit": "loss_of_appetite",
            "oppression thoracique": "chest_tightness", "poitrine serrée": "chest_tightness",
            "poitrine serree": "chest_tightness"
        }
        
        # Synonymes anglais -> symptôme canonique anglais
        self.en_synonyms = {
            "fever": "fever", "feverish": "fever", "chills": "chills", "cough": "cough",
            "dry cough": "cough", "fatigue": "fatigue", "tiredness": "fatigue",
            "exhaustion": "fatigue", "headache": "headache", "head pain": "headache",
            "dizziness": "dizziness", "vertigo": "dizziness", "sore throat": "sore_throat",
            "throat pain": "sore_throat", "runny nose": "congestion", "stuffy nose": "congestion",
            "nasal congestion": "congestion", "shortness of breath": "breathlessness",
            "chest tightness": "chest_tightness", "muscle pain": "muscle_pain",
            "body aches": "muscle_pain", "aches": "pain", "nausea": "nausea",
            "vomiting": "vomiting", "diarrhea": "diarrhea", "constipation": "constipation",
            "rash": "skin_rash", "skin rash": "skin_rash", "itching": "itching",
            "loss of appetite": "loss_of_appetite", "weakness": "weakness", "malaise": "malaise",
            "sleep disturbance": "sleep_disturbance"
        }
    
    def extract_symptoms_from_text(
        self, 
        raw_description: str, 
        available_symptoms: List[str],
        language: str = 'fr'
    ) -> List[str]:
        """
        Extrait les symptômes depuis un texte en utilisant des techniques NLP
        Args:
            raw_description: Texte brut de la description utilisateur
            available_symptoms: Liste des noms de symptômes disponibles (format normalisé)
            language: Langue de la description ('fr' ou 'en')
        Returns:
            List[str]: Liste des noms de symptômes normalisés correspondant à la description
        """
        if not raw_description or not raw_description.strip():
            return []
        
        # Normalisation du texte d'entrée
        text_lower = raw_description.lower().strip()
        text_clean = re.sub(r'[^\w\s]', ' ', text_lower)
        text_clean = re.sub(r'\s+', ' ', text_clean).strip()
        
        matched_symptoms = []
        confidence_scores = []
        
        # Création d'un mapping des symptômes normalisés
        symptom_map = {
            self.text_utils.normalize_symptom_name(s): s 
            for s in available_symptoms
        }
        normalized_symptoms = list(symptom_map.keys())
        
        # Utilisation de spaCy si disponible
        if nlp_spacy is not None:
            matched_symptoms, confidence_scores = self._extract_with_spacy(
                text_clean, normalized_symptoms, language
            )
        else:
            matched_symptoms, confidence_scores = self._extract_basic(
                text_clean, normalized_symptoms, language
            )
        
        # Matching flou pour les symptômes similaires
        matched_symptoms, confidence_scores = self._fuzzy_match(
            text_clean, normalized_symptoms, matched_symptoms, confidence_scores
        )
        
        # Tri par confiance et retour des meilleurs matches
        if matched_symptoms:
            sorted_pairs = sorted(
                zip(matched_symptoms, confidence_scores),
                key=lambda x: x[1], 
                reverse=True
            )
            matched_symptoms = [symptom for symptom, _ in sorted_pairs]
        
        return matched_symptoms
    
    def _extract_with_spacy(
        self, 
        text_clean: str, 
        normalized_symptoms: List[str],
        language: str
    ) -> tuple[List[str], List[float]]:
        """
        Extraction avec spaCy (méthode avancée)
        Args:
            text_clean: Texte nettoyé
            normalized_symptoms: Liste des symptômes normalisés
            language: Langue du texte
        Returns:
            tuple: (symptômes_matchés, scores_confiance)
        """
        matched_symptoms = []
        confidence_scores = []
        
        doc = nlp_spacy(text_clean)
        phrase_map = self.fr_synonyms if language == 'fr' else self.en_synonyms
        
        # Phrase matcher pour les expressions multi-mots
        matcher = PhraseMatcher(nlp_spacy.vocab, attr="LOWER")
        phrase_patterns = [nlp_spacy.make_doc(p) for p in phrase_map.keys()]
        if phrase_patterns:
            matcher.add("SYMPTOM_PHRASES", phrase_patterns)
            matches = matcher(doc)
            for _, start, end in matches:
                span_text = doc[start:end].text.lower()
                if span_text in phrase_map:
                    normalized_en = self.text_utils.normalize_symptom_name(phrase_map[span_text])
                    if normalized_en in normalized_symptoms and normalized_en not in matched_symptoms:
                        matched_symptoms.append(normalized_en)
                        confidence_scores.append(0.95)
        
        # Matching basé sur les lemmes pour les tokens individuels
        lemmas = [t.lemma_.lower() for t in doc if t.is_alpha]
        for lemma in lemmas:
            normalized_lemma = self.text_utils.normalize_symptom_name(lemma)
            if normalized_lemma in normalized_symptoms and normalized_lemma not in matched_symptoms:
                matched_symptoms.append(normalized_lemma)
                confidence_scores.append(0.9)
        
        # Vérification des synonymes avec correspondance de sous-chaîne
        for term, en_symptom in phrase_map.items():
            if term in text_clean:
                normalized_en = self.text_utils.normalize_symptom_name(en_symptom)
                if normalized_en in normalized_symptoms and normalized_en not in matched_symptoms:
                    matched_symptoms.append(normalized_en)
                    confidence_scores.append(0.9)
        
        return matched_symptoms, confidence_scores
    
    def _extract_basic(
        self, 
        text_clean: str, 
        normalized_symptoms: List[str],
        language: str
    ) -> tuple[List[str], List[float]]:
        """
        Extraction basique sans spaCy
        Args:
            text_clean: Texte nettoyé
            normalized_symptoms: Liste des symptômes normalisés
            language: Langue du texte
        Returns:
            tuple: (symptômes_matchés, scores_confiance)
        """
        matched_symptoms = []
        confidence_scores = []
        words = text_clean.split()
        
        # Matching par mots individuels
        for word in words:
            normalized_word = self.text_utils.normalize_symptom_name(word)
            if normalized_word in normalized_symptoms and normalized_word not in matched_symptoms:
                matched_symptoms.append(normalized_word)
                confidence_scores.append(0.85)
        
        # Matching par bigrammes
        for i in range(len(words) - 1):
            bigram = " ".join(words[i:i+2])
            normalized_bigram = self.text_utils.normalize_symptom_name(bigram)
            if normalized_bigram in normalized_symptoms and normalized_bigram not in matched_symptoms:
                matched_symptoms.append(normalized_bigram)
                confidence_scores.append(0.85)
        
        # Vérification des synonymes
        phrase_map = self.fr_synonyms if language == 'fr' else self.en_synonyms
        for term, en_symptom in phrase_map.items():
            if term in text_clean:
                normalized_en = self.text_utils.normalize_symptom_name(en_symptom)
                if normalized_en in normalized_symptoms and normalized_en not in matched_symptoms:
                    matched_symptoms.append(normalized_en)
                    confidence_scores.append(0.9)
        
        return matched_symptoms, confidence_scores
    
    def _fuzzy_match(
        self,
        text_clean: str,
        normalized_symptoms: List[str],
        matched_symptoms: List[str],
        confidence_scores: List[float]
    ) -> tuple[List[str], List[float]]:
        """
        Matching flou pour trouver des symptômes similaires
        Args:
            text_clean: Texte nettoyé
            normalized_symptoms: Liste des symptômes normalisés
            matched_symptoms: Symptômes déjà matchés
            confidence_scores: Scores de confiance existants
        Returns:
            tuple: (symptômes_matchés, scores_confiance)
        """
        for symptom in normalized_symptoms:
            if symptom in matched_symptoms:
                continue
            
            # Calcul des scores de similarité
            ratio = fuzz.ratio(text_clean, symptom)
            partial_ratio = fuzz.partial_ratio(text_clean, symptom)
            token_sort_ratio = fuzz.token_sort_ratio(text_clean, symptom)
            
            # Utilisation du meilleur score
            best_score = max(ratio, partial_ratio, token_sort_ratio) / 100.0
            
            # Seuil pour le matching (ajustable)
            if best_score > 0.75:
                matched_symptoms.append(symptom)
                confidence_scores.append(best_score)
        
        return matched_symptoms, confidence_scores
    
    def generate_prediction_explanation(
        self,
        disease_name: str,
        probability: float,
        specialist_name: str,
        symptoms: List[str],
        translation_service: 'TranslationService',
        language: str = 'fr'
    ) -> str:
        """
        Génère une explication/description pour une prédiction dans la langue demandée
        Args:
            disease_name: Nom de la maladie prédite (déjà traduite)
            probability: Score de probabilité (0-100)
            specialist_name: Nom du spécialiste recommandé (déjà traduit)
            symptoms: Liste des symptômes qui ont mené à cette prédiction
            translation_service: Service de traduction pour traduire les symptômes
            language: Langue pour l'explication ('fr' ou 'en')
        Returns:
            str: Texte d'explication dans la langue demandée
        """
        translated_symptoms = translation_service.translate_symptoms(symptoms, target_lang=language)
        
        if language == 'fr':
            # Formatage de la liste des symptômes
            if translated_symptoms:
                symptoms_text = ", ".join(translated_symptoms[:5])  # Limite aux 5 premiers
                if len(translated_symptoms) > 5:
                    symptoms_text += f" et {len(translated_symptoms) - 5} autre(s)"
            else:
                symptoms_text = "les symptômes déclarés"
            
            explanation = (
                f"Cette prédiction de '{disease_name}' ({probability:.2f}%) est basée sur "
                f"la présence des symptômes suivants: {symptoms_text}, qui sont "
                f"caractéristiques de cette pathologie. Le spécialiste recommandé est "
                f"un(e) {specialist_name}."
            )
        else:  # Anglais
            # Formatage de la liste des symptômes
            if translated_symptoms:
                symptoms_text = ", ".join(translated_symptoms[:5])  # Limite aux 5 premiers
                if len(translated_symptoms) > 5:
                    symptoms_text += f" and {len(translated_symptoms) - 5} other(s)"
            else:
                symptoms_text = "the declared symptoms"
            
            explanation = (
                f"This prediction of '{disease_name}' ({probability:.2f}%) is based on "
                f"the presence of the following symptoms: {symptoms_text}, which are "
                f"characteristic of this pathology. The recommended specialist is "
                f"a {specialist_name}."
            )
        
        return explanation