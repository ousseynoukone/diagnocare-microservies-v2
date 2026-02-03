"""
NLP Service for symptom extraction, translation, and explanation generation
"""
import json
import os
import re
from typing import List, Dict, Optional, Tuple
from fuzzywuzzy import fuzz

# Try to import spacy, fallback to basic processing if not available
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
            print("Warning: spaCy models not found. Using basic NLP processing.")
except Exception as exc:
    # spaCy is not compatible with Python 3.14+ at the moment.
    # Any import/setup failure should fallback to basic processing.
    nlp_spacy = None
    print(f"Warning: spaCy unavailable ({exc}). Using basic NLP processing.")


def load_translations(translations_file: str = "data/translations.json") -> Dict:
    """Load translations from JSON file"""
    try:
        with open(translations_file, 'r', encoding='utf-8') as f:
            return json.load(f)
    except FileNotFoundError:
        print(f"Warning: Translation file {translations_file} not found. Using empty translations.")
        return {"diseases": {}, "specialists": {}}
    except json.JSONDecodeError as e:
        print(f"Error loading translations: {e}")
        return {"diseases": {}, "specialists": {}}


def normalize_symptom_name(symptom_name: str) -> str:
    """Normalize symptom name to match ML model format"""
    if not symptom_name:
        return ""
    # Convert to lowercase and replace spaces with underscores
    normalized = re.sub(r'[^\w\s]', '', str(symptom_name).strip().lower())
    normalized = "_".join(normalized.split())
    return normalized


def extract_symptoms_from_text(raw_description: str, available_symptoms: List[str],
                               language: str = 'fr') -> List[str]:
    """
    Extract symptoms from raw text description using NLP techniques
    
    Args:
        raw_description: Raw text description from user
        available_symptoms: List of available symptom names (normalized format)
        language: Language of the description ('fr' or 'en')
    
    Returns:
        List of normalized symptom names that match the description
    """
    if not raw_description or not raw_description.strip():
        return []
    
    # Normalize input text
    text_lower = raw_description.lower().strip()
    
    # Remove punctuation and normalize spaces
    text_clean = re.sub(r'[^\w\s]', ' ', text_lower)
    text_clean = re.sub(r'\s+', ' ', text_clean).strip()
    
    matched_symptoms = []
    confidence_scores = []
    
    # Create a mapping of normalized symptoms to original for matching
    symptom_map = {normalize_symptom_name(s): s for s in available_symptoms}
    normalized_symptoms = list(symptom_map.keys())
    
    # French symptom synonyms/phrases mapping -> canonical English symptom
    fr_synonyms = {
        "fièvre": "fever",
        "fievre": "fever",
        "fiévreux": "fever",
        "fiévreuse": "fever",
        "fébrile": "fever",
        "frissons": "chills",
        "toux": "cough",
        "toux sèche": "cough",
        "toux seche": "cough",
        "fatigue": "fatigue",
        "épuisement": "fatigue",
        "maux de tête": "headache",
        "mal de tête": "headache",
        "mal de tete": "headache",
        "céphalée": "headache",
        "coup de barre": "fatigue",
        "douleur": "pain",
        "douleurs": "pain",
        "douleurs musculaires": "muscle_pain",
        "courbatures": "muscle_pain",
        "nausée": "nausea",
        "nausee": "nausea",
        "vomissement": "vomiting",
        "vomissements": "vomiting",
        "diarrhée": "diarrhea",
        "diarrhées": "diarrhea",
        "constipation": "constipation",
        "démangeaison": "itching",
        "démangeaisons": "itching",
        "éruption": "skin_rash",
        "éruption cutanée": "skin_rash",
        "éruptions": "skin_rash",
        "rougeur": "skin_rash",
        "rougeurs": "skin_rash",
        "gorge irritée": "sore_throat",
        "gorge irritee": "sore_throat",
        "mal de gorge": "sore_throat",
        "nez bouché": "congestion",
        "nez bouche": "congestion",
        "congestion": "congestion",
        "vertige": "dizziness",
        "vertiges": "dizziness",
        "faiblesse": "weakness",
        "malaise": "malaise",
        "perte d'appétit": "loss_of_appetite",
        "perte d appetit": "loss_of_appetite",
        "oppression thoracique": "chest_tightness",
        "poitrine serrée": "chest_tightness",
        "poitrine serree": "chest_tightness"
    }

    # English symptom synonyms/phrases mapping -> canonical English symptom
    en_synonyms = {
        "fever": "fever",
        "feverish": "fever",
        "chills": "chills",
        "cough": "cough",
        "dry cough": "cough",
        "fatigue": "fatigue",
        "tiredness": "fatigue",
        "exhaustion": "fatigue",
        "headache": "headache",
        "head pain": "headache",
        "dizziness": "dizziness",
        "vertigo": "dizziness",
        "sore throat": "sore_throat",
        "throat pain": "sore_throat",
        "runny nose": "congestion",
        "stuffy nose": "congestion",
        "nasal congestion": "congestion",
        "shortness of breath": "breathlessness",
        "chest tightness": "chest_tightness",
        "muscle pain": "muscle_pain",
        "body aches": "muscle_pain",
        "aches": "pain",
        "nausea": "nausea",
        "vomiting": "vomiting",
        "diarrhea": "diarrhea",
        "constipation": "constipation",
        "rash": "skin_rash",
        "skin rash": "skin_rash",
        "itching": "itching",
        "loss of appetite": "loss_of_appetite",
        "weakness": "weakness",
        "malaise": "malaise",
        "sleep disturbance": "sleep_disturbance"
    }
    
    # If spaCy is available, use it for better phrase matching + lemmatization
    if nlp_spacy is not None:
        doc = nlp_spacy(text_clean)

        # Phrase matcher for multi-word expressions
        matcher = PhraseMatcher(nlp_spacy.vocab, attr="LOWER")
        phrase_map = fr_synonyms if language == 'fr' else en_synonyms
        phrase_patterns = [nlp_spacy.make_doc(p) for p in phrase_map.keys()]
        if phrase_patterns:
            matcher.add("SYMPTOM_PHRASES", phrase_patterns)
            matches = matcher(doc)
            for _, start, end in matches:
                span_text = doc[start:end].text.lower()
                if span_text in phrase_map:
                    normalized_en = normalize_symptom_name(phrase_map[span_text])
                    if normalized_en in normalized_symptoms and normalized_en not in matched_symptoms:
                        matched_symptoms.append(normalized_en)
                        confidence_scores.append(0.95)

        # Lemma-based matching for single tokens
        lemmas = [t.lemma_.lower() for t in doc if t.is_alpha]
        for lemma in lemmas:
            normalized_lemma = normalize_symptom_name(lemma)
            if normalized_lemma in normalized_symptoms and normalized_lemma not in matched_symptoms:
                matched_symptoms.append(normalized_lemma)
                confidence_scores.append(0.9)

        # Also check synonyms with substring match in the raw text
        for term, en_symptom in phrase_map.items():
            if term in text_clean:
                normalized_en = normalize_symptom_name(en_symptom)
                if normalized_en in normalized_symptoms and normalized_en not in matched_symptoms:
                    matched_symptoms.append(normalized_en)
                    confidence_scores.append(0.9)
    else:
        # Basic matching if spaCy is not available
        words = text_clean.split()
        for word in words:
            normalized_word = normalize_symptom_name(word)
            if normalized_word in normalized_symptoms and normalized_word not in matched_symptoms:
                matched_symptoms.append(normalized_word)
                confidence_scores.append(0.85)

        for i in range(len(words) - 1):
            bigram = " ".join(words[i:i+2])
            normalized_bigram = normalize_symptom_name(bigram)
            if normalized_bigram in normalized_symptoms and normalized_bigram not in matched_symptoms:
                matched_symptoms.append(normalized_bigram)
                confidence_scores.append(0.85)

        phrase_map = fr_synonyms if language == 'fr' else en_synonyms
        for term, en_symptom in phrase_map.items():
            if term in text_clean:
                normalized_en = normalize_symptom_name(en_symptom)
                if normalized_en in normalized_symptoms and normalized_en not in matched_symptoms:
                    matched_symptoms.append(normalized_en)
                    confidence_scores.append(0.9)
    
    # Fuzzy matching for similar symptoms
    for symptom in normalized_symptoms:
        if symptom in matched_symptoms:
            continue
        
        # Calculate similarity scores
        ratio = fuzz.ratio(text_clean, symptom)
        partial_ratio = fuzz.partial_ratio(text_clean, symptom)
        token_sort_ratio = fuzz.token_sort_ratio(text_clean, symptom)
        
        # Use the best score
        best_score = max(ratio, partial_ratio, token_sort_ratio) / 100.0
        
        # Threshold for matching (adjust as needed)
        if best_score > 0.75:
            matched_symptoms.append(symptom)
            confidence_scores.append(best_score)
    
    # Sort by confidence and return top matches
    if matched_symptoms:
        sorted_pairs = sorted(zip(matched_symptoms, confidence_scores), 
                            key=lambda x: x[1], reverse=True)
        matched_symptoms = [symptom for symptom, _ in sorted_pairs]
    
    return matched_symptoms


def translate_disease(disease_name_en: str, translations_dict: Dict, 
                     target_lang: str = 'fr') -> str:
    """Translate disease name from English to target language"""
    if target_lang == 'en':
        return disease_name_en
    
    diseases = translations_dict.get('diseases', {})
    translated = diseases.get(disease_name_en, disease_name_en)
    return translated


def translate_specialist(specialist_name_en: str, translations_dict: Dict, 
                        target_lang: str = 'fr') -> str:
    """Translate specialist name from English to target language"""
    if target_lang == 'en':
        return specialist_name_en
    
    specialists = translations_dict.get('specialists', {})
    translated = specialists.get(specialist_name_en, specialist_name_en)
    return translated


def generate_prediction_explanation(disease_name: str, probability: float, 
                                   specialist_name: str, symptoms: List[str],
                                   language: str = 'fr') -> str:
    """
    Generate explanation/description for a prediction in the requested language
    
    Args:
        disease_name: Name of the predicted disease (already translated)
        probability: Probability score (0-100)
        specialist_name: Name of recommended specialist (already translated)
        symptoms: List of symptoms that led to this prediction
        language: Language for the explanation ('fr' or 'en')
    
    Returns:
        Explanation text in the requested language
    """
    if language == 'fr':
        # Format symptoms list
        if symptoms:
            symptoms_text = ", ".join(symptoms[:5])  # Limit to first 5 symptoms
            if len(symptoms) > 5:
                symptoms_text += f" et {len(symptoms) - 5} autre(s)"
        else:
            symptoms_text = "les symptômes déclarés"
        
        explanation = (
            f"Cette prédiction de '{disease_name}' ({probability:.2f}%) est basée sur "
            f"la présence des symptômes suivants: {symptoms_text}, qui sont "
            f"caractéristiques de cette pathologie. Le spécialiste recommandé est "
            f"un(e) {specialist_name}."
        )
    else:  # English
        # Format symptoms list
        if symptoms:
            symptoms_text = ", ".join(symptoms[:5])  # Limit to first 5 symptoms
            if len(symptoms) > 5:
                symptoms_text += f" and {len(symptoms) - 5} other(s)"
        else:
            symptoms_text = "the declared symptoms"
        
        explanation = (
            f"This prediction of '{disease_name}' ({probability:.2f}%) is based on "
            f"the presence of the following symptoms: {symptoms_text}, which are "
            f"characteristic of this pathology. The recommended specialist is "
            f"a {specialist_name}."
        )
    
    return explanation
