# ML/NLP Integration Status and Issues

## What Was Implemented

### Flask ML service
- Added symptom extraction endpoint: `POST /extract-symptoms`
  - Accepts `raw_description` and `language` (`fr` or `en`)
  - Returns a list of normalized symptoms used by the ML model
- Enhanced prediction endpoint: `POST /predict`
  - Accepts either `symptoms` or `raw_description`
  - Handles `language` parameter for FR/EN output
  - Generates a human‑readable description for each prediction
  - Returns translated disease/specialist names when `language="fr"`
- Added translation dictionary in `data/translations.json`
  - EN → FR mappings for diseases and specialists
- Added `nlp_service.py`
  - Extracts symptoms from free text
  - Uses spaCy when available (FR/EN models)
  - Falls back to rule‑based + fuzzy matching when spaCy is unavailable
  - Generates explanations in the requested language

### Java service (DiagnoCareService)
- Added DTOs for ML requests/responses with language support
  - `MLPredictionRequestDTO` includes `language`
  - `MLPredictionResponseDTO` includes `description`, `disease_fr`, `specialist_fr`, etc.
- Added ML symptom extraction call
  - `MLPredictionClient.extractSymptoms(raw_description, language)`
- Updated prediction flow to use Flask NLP output
  - `PredictionController` now calls Flask for extraction
  - PathologyResult description uses the explanation returned by Flask
- Deprecated local Java NLP service (no longer used)

## Current Problem (Detailed)

### Symptom extraction is too sparse
Even with a long, rich symptom description, the extractor often returns only 3–5 symptoms.
This is the main cause of weak predictions.

Example:
Input text contains symptoms like:
- fièvre / frissons
- gorge irritée
- nez bouché
- douleurs musculaires / courbatures
- vertiges
- fatigue / faiblesse

But the extraction may return only:
`fatigue, cough, headache, nausea`

### Why sparse extraction hurts
The ML model was trained on **many symptoms per sample**.
When you provide only a few, the model has low signal and returns:
- low confidence scores
- unstable ranking of diseases

### Specialist mismatch confusion
The specialist prediction is currently independent from the disease prediction.
That means you can get a disease and a specialist that do not logically match.
This is expected given the model setup, but it can look wrong to users.

## Root Causes Summary
- Sparse symptom extraction from free text
- Model trained on richer symptom vectors than real input
- Disease and specialist predictions are independent
- Default profile values reduce personalization

## Recommended Fixes (Priority Order)

1. Improve symptom extraction (highest impact)
   - Add more FR/EN phrases and synonyms
   - Ensure multi‑word expressions are captured (e.g. "nez bouché")
   - Use spaCy in production (Python 3.11/3.12) for best accuracy

2. Add follow‑up questions (increase symptom count)
   - Ask 2–4 clarifying questions when too few symptoms are detected
   - Example: fever? sore throat? body aches? congestion?

3. Add a minimum symptom threshold
   - If extracted symptoms < N, return “insufficient data”
   - Avoid low‑confidence predictions

4. Retrain or fine‑tune for sparse inputs
   - Simulate shorter symptom lists during training
   - Improves robustness when users provide few symptoms

5. Clarify UI output
   - Separate disease list and specialist list (if no mapping)
   - Show a disclaimer when specialist is predicted independently

## Notes
- spaCy is not compatible with Python 3.14 yet.
  Use Python 3.11/3.12 for full NLP features, or run via Docker.
