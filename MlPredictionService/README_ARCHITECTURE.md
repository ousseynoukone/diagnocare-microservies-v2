# Architecture du Service ML Prediction

## Vue d'ensemble

Ce projet utilise une architecture modulaire pour améliorer la maintenabilité, la testabilité et la simplicité du code.

## Structure Modulaire

```
MlPredictionService/
├── config/              # Configuration de l'application
│   ├── app_config.py    # Configuration Flask et Eureka
│   └── model_config.py  # Configuration des modèles ML
├── models/              # Modèles de données (DTOs)
│   ├── prediction_request.py
│   ├── prediction_response.py
│   ├── symptom_extraction_request.py
│   └── translation_request.py
├── repositories/        # Couche d'accès aux données
│   ├── model_repository.py      # Chargement des modèles ML
│   └── translation_repository.py # Chargement des traductions
├── services/            # Logique métier
│   ├── prediction_service.py    # Service de prédiction
│   ├── translation_service.py   # Service de traduction
│   └── nlp_service.py           # Service NLP
├── controllers/         # Contrôleurs API (endpoints)
│   ├── health_controller.py
│   ├── metadata_controller.py
│   └── prediction_controller.py
├── utils/               # Utilitaires
│   └── text_utils.py    # Utilitaires de traitement de texte
└── app.py               # Point d'entrée de l'application
```

## Organisation des Modules

Chaque module a une responsabilité claire :
- **Config**: Gestion de la configuration uniquement
- **Models**: Représentation des données uniquement
- **Repositories**: Accès aux données uniquement
- **Services**: Logique métier uniquement
- **Controllers**: Gestion des endpoints uniquement
- **Utils**: Fonctions utilitaires uniquement

L'architecture permet :
- L'extension des services sans modification du code existant
- Le remplacement des repositories par d'autres implémentations
- L'injection de dépendances pour faciliter les tests

## Flux de Données

```
Request → Controller → Service → Repository → Model/Data
                ↓
         Response ← Service ← Repository
```

## Exemple d'Utilisation

### Prédiction
```python
# Le contrôleur reçoit la requête
prediction_controller.predict()

# Le service traite la logique métier
prediction_service.predict(request)

# Les repositories fournissent les données
model_repository.get_model('model')
translation_repository.get_translations()
```

## Avantages de cette Architecture

1. **Maintenabilité**: Code organisé en modules clairs
2. **Testabilité**: Chaque composant peut être testé indépendamment
3. **Extensibilité**: Facile d'ajouter de nouvelles fonctionnalités
4. **Réutilisabilité**: Services et repositories réutilisables
5. **Séparation des préoccupations**: Chaque couche a un rôle précis

## Comment Ajouter une Nouvelle Fonctionnalité

1. **Nouveau endpoint**: Créer un nouveau contrôleur ou ajouter une méthode dans un contrôleur existant
2. **Nouvelle logique métier**: Créer un nouveau service ou étendre un service existant
3. **Nouvelle source de données**: Créer un nouveau repository
4. **Nouveau modèle de données**: Créer une nouvelle classe dans `models/`

## Commentaires en Français

Tous les commentaires et docstrings sont en français pour faciliter la compréhension du code par l'équipe francophone.