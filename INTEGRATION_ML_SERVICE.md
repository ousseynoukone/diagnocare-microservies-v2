# Intégration du Service ML - Documentation

## Résumé des Changements

Cette documentation décrit l'intégration complète du service ML (Flask) avec le service DiagnoCare (Spring Boot).

## 1. Service ML Flask dans Docker

### Dockerfile créé
- **Fichier**: `MlPredictionService/Dockerfile`
- **Description**: Dockerfile pour containeriser le service Flask ML
- **Port**: 5000
- **Health Check**: Intégré

### Docker Compose
- **Service ajouté**: `ml-prediction-service`
- **Configuration**: 
  - Build depuis `./MlPredictionService/.`
  - Port exposé: `5000:5000`
  - Volumes pour modèles et données
  - Health check configuré

## 2. Client HTTP pour le Service ML

### Configuration
- **Fichier**: `core/config/MLServiceConfig.java`
- **URL par défaut**: `http://ml-prediction-service:5000`
- **Configurable via**: `ml.service.url` dans `application.properties`

### Client ML
- **Fichier**: `service/MLPredictionClient.java`
- **Fonctionnalités**:
  - Appel au endpoint `/predict` du service ML
  - Health check du service ML
  - Gestion des erreurs et timeouts
  - Logging des appels

### DTOs
- **MLPredictionRequestDTO**: Requête vers le service ML
  - Symptômes (List<String>)
  - Profil patient (âge, poids, tension, cholestérol, etc.)
  
- **MLPredictionResponseDTO**: Réponse du service ML
  - Liste de prédictions (top 5)
  - Probabilités de maladie et spécialiste
  - Métadonnées

## 3. Service NLP pour Extraction de Symptômes

### Interface
- **Fichier**: `service/SymptomNLPService.java`
- **Méthodes**:
  - `extractSymptoms(String rawDescription)`: Extrait les symptômes depuis une description en langage naturel
  - `normalizeSymptom(String symptomName)`: Normalise un nom de symptôme

### Implémentation
- **Fichier**: `service/impl/SymptomNLPServiceImpl.java`
- **Fonctionnalités**:
  - Extraction par matching de mots-clés
  - Support de synonymes (français/anglais)
  - Normalisation vers format attendu par le ML (underscore, lowercase)
  - Recherche dans la base de données des symptômes existants

## 4. Intégration Complète dans PredictionController

### Endpoint `/predictions` (POST)
- **Input**: `SessionSymptomRequestDTO`
- **Processus**:
  1. Création de la session de symptômes
  2. Extraction des symptômes via NLP
  3. Récupération du profil médical patient
  4. Construction de la requête ML
  5. Appel au service ML
  6. Détection des alertes "Red Flag"
  7. Calcul du score global
  8. Création de l'entité Prediction
  9. Création des PathologyResult pour les top 3 prédictions
  10. Création/find des entités Pathology et Doctor

### Détection Red Flag
- Probabilité > 90% pour la top prédiction
- Maladies critiques (heart, cardiac, stroke, severe)

### Calcul Score Global
- Moyenne des probabilités des top 3 prédictions

## 5. Améliorations des Services

### DoctorService
- **Méthode ajoutée**: `getDoctorBySpecialistLabel(String specialistLabel)`
- Permet de trouver un médecin par son label de spécialité

### PathologyService
- **Méthode existante utilisée**: `getPathologyByName(String name)`
- Création automatique si la pathologie n'existe pas

## 6. Configuration

### application.properties
- **Nouvelle propriété**: `ml.service.url=http://ml-prediction-service:5000`
- Configurable via variables d'environnement

## Workflow Complet

```
1. Client → POST /api/v1/diagnocare/predictions
   Body: SessionSymptomRequestDTO (userId, rawDescription, symptomIds)

2. PredictionController.makePrediction()
   ├─ Création SessionSymptom
   ├─ Extraction symptômes (NLP)
   ├─ Récupération profil patient
   └─ Construction MLPredictionRequestDTO

3. MLPredictionClient.predict()
   └─ POST http://ml-prediction-service:5000/predict
      └─ Retourne MLPredictionResponseDTO (top 5 prédictions)

4. Traitement des résultats
   ├─ Détection Red Flag
   ├─ Calcul score global
   ├─ Création Prediction
   └─ Création PathologyResult (top 3)

5. Retour au client
   └─ PredictionDTO avec toutes les informations
```

## Format de Requête ML

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
  "outcome_variable": "Negative",
  "smoking": "No",
  "alcohol": "None",
  "sedentarite": "Moderate",
  "family_history": "No"
}
```

## Format de Réponse ML

```json
{
  "predictions": [
    {
      "rank": 1,
      "disease": "Fungal infection",
      "probability": 45.23,
      "specialist": "Dermatologist",
      "specialist_probability": 42.15
    },
    ...
  ],
  "metadata": {
    "symptoms_count": 3,
    "profile_used": {...}
  }
}
```

## Tests

### Health Check ML Service
```bash
curl http://localhost:5000/health
```

### Test Prédiction
```bash
curl -X POST http://localhost:8765/api/v1/diagnocare/predictions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "userId": 1,
    "rawDescription": "J'ai de la fièvre, une toux et je me sens fatigué",
    "symptomIds": []
  }'
```

## Notes Importantes

1. **Service ML doit être démarré** avant le service DiagnoCare
2. **Modèles ML** doivent être présents dans `MlPredictionService/models/`
3. **NLP basique**: L'implémentation actuelle est basique (matching de mots-clés). Pour une production, considérer une solution NLP plus avancée (spaCy, NLTK, etc.)
4. **Gestion d'erreurs**: Le service gère les erreurs de communication avec le ML service
5. **Création automatique**: Les entités Pathology et Doctor sont créées automatiquement si elles n'existent pas

## Prochaines Étapes Suggérées

1. Améliorer le service NLP avec une bibliothèque dédiée
2. Ajouter un cache pour les appels ML (si mêmes symptômes)
3. Implémenter un circuit breaker pour la résilience
4. Ajouter des métriques et monitoring
5. Tests unitaires et d'intégration
