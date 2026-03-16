"""
Application Flask principale
"""
from flask import Flask
from flasgger import Swagger
from flask_eureka import Eureka
from flask_eureka.eureka import eureka_bp
from flask_eureka.eurekaclient import EurekaClient
import logging

# Configuration
from config.app_config import AppConfig
from config.model_config import ModelConfig

# Repositories
from repositories.model_repository import ModelRepository
from repositories.translation_repository import TranslationRepository

# Services
from services.translation_service import TranslationService
from services.nlp_service import NLPService
from services.prediction_service import PredictionService

# Controllers
from controllers.health_controller import HealthController
from controllers.metadata_controller import MetadataController
from controllers.prediction_controller import PredictionController


def _patch_eureka_client():
    """
    Patch pour flask-eureka-client qui force securePort enabled=true
    et utilise ifconfig pour ipAddr.
    Désactive le port sécurisé et utilise le hostname configuré comme ipAddr.
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


def create_app() -> Flask:
    """
    Factory function pour créer l'application Flask
    Returns:
        Flask: Application Flask configurée
    """
    # Configuration
    app_config = AppConfig()
    model_config = ModelConfig()
    
    # Initialisation de Flask
    app = Flask(__name__)
    app.config.update(app_config.get_flask_config())
    
    # Configuration du logging
    app.logger.setLevel(logging.INFO)
    
    # Configuration Swagger
    Swagger(app, template=app_config.SWAGGER_TEMPLATE)
    
    # Configuration Eureka
    app.logger.info("Initialisation du client Eureka...")
    _patch_eureka_client()
    eureka = Eureka(app)
    app.register_blueprint(eureka_bp)
    eureka.register_service(
        name=app.config["SERVICE_NAME"],
        vip_address=app_config.EUREKA_INSTANCE_HOSTNAME,
        secure_vip_address=app_config.EUREKA_INSTANCE_HOSTNAME,
    )
    app.logger.info("Client Eureka initialisé.")
    
    # Initialisation des repositories
    model_repository = ModelRepository(model_config)
    translation_repository = TranslationRepository(model_config)
    
    # Chargement des modèles et traductions
    if not model_repository.load_all():
        app.logger.error("Erreur lors du chargement des modèles. Veuillez exécuter train_model.py d'abord!")
    
    if not translation_repository.load():
        app.logger.warning("Erreur lors du chargement des traductions. Utilisation de traductions vides.")
    
    # Initialisation des services
    translation_service = TranslationService(translation_repository)
    nlp_service = NLPService()
    prediction_service = PredictionService(
        model_repository,
        translation_service,
        nlp_service
    )
    
    # Initialisation des controllers
    health_controller = HealthController()
    metadata_controller = MetadataController(model_repository, translation_service)
    prediction_controller = PredictionController(
        prediction_service,
        nlp_service,
        translation_service,
        model_repository
    )
    
    # Enregistrement des routes
    _register_routes(app, health_controller, metadata_controller, prediction_controller)
    
    return app


def _register_routes(
    app: Flask,
    health_controller: HealthController,
    metadata_controller: MetadataController,
    prediction_controller: PredictionController
):
    """
    Enregistre toutes les routes de l'application
    Args:
        app: Application Flask
        health_controller: Contrôleur de santé
        metadata_controller: Contrôleur de métadonnées
        prediction_controller: Contrôleur de prédiction
    """
    # Route de santé
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
        return health_controller.health_check()
    
    # Route de métadonnées
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
        return metadata_controller.get_features_metadata()
    
    # Route d'extraction de symptômes
    @app.route('/extract-symptoms', methods=['POST'])
    def extract_symptoms():
        """
        Extract symptoms from raw text description
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
                  - raw_description
                properties:
                  raw_description:
                    type: string
                  language:
                    type: string
                    enum: [fr, en]
        responses:
          200:
            description: Extracted symptoms
          400:
            description: Invalid input
        """
        return prediction_controller.extract_symptoms()
    
    # Route de traduction
    @app.route('/translate', methods=['POST'])
    def translate():
        """
        Translate symptoms, diseases, and specialists
        ---
        tags:
          - Translation
        requestBody:
          required: true
          content:
            application/json:
              schema:
                type: object
                properties:
                  language:
                    type: string
                    enum: [fr, en]
                  symptoms:
                    type: array
                    items:
                      type: string
                  diseases:
                    type: array
                    items:
                      type: string
                  specialists:
                    type: array
                    items:
                      type: string
        responses:
          200:
            description: Translated items
          400:
            description: Invalid input
        """
        return prediction_controller.translate()
    
    # Route de prédiction
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
        return prediction_controller.predict()


# Point d'entrée de l'application
if __name__ == '__main__':
    app = create_app()
    app_config = AppConfig()
    app.run(
        host=app_config.FLASK_HOST,
        port=app_config.FLASK_PORT,
        debug=app_config.FLASK_DEBUG
    )