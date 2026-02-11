"""
Configuration de l'application Flask et Eureka
"""
import os
import socket
import logging
from typing import Optional

try:
    from dotenv import load_dotenv
except Exception:
    load_dotenv = None


class AppConfig:
    """
    Classe de configuration centralisée pour l'application
    """
    
    def __init__(self):
        """Initialise la configuration depuis les variables d'environnement"""
        if load_dotenv:
            load_dotenv()
        
        # Configuration du service
        self.SERVICE_NAME = os.getenv("SERVICE_NAME", "ml-prediction-service")
        self.EUREKA_SERVICE_URL = os.getenv("EUREKA_SERVICE_URL", "http://localhost:8761")
        self.EUREKA_INSTANCE_HOSTNAME = os.getenv("EUREKA_INSTANCE_HOSTNAME", self._detect_local_ip())
        self.EUREKA_INSTANCE_PORT = int(os.getenv("EUREKA_INSTANCE_PORT", "5000"))
        
        # Configuration Flask
        self.FLASK_HOST = os.getenv("FLASK_HOST", "0.0.0.0")
        self.FLASK_PORT = int(os.getenv("FLASK_PORT", "5000"))
        self.FLASK_DEBUG = os.getenv("FLASK_DEBUG", "True").lower() == "true"
        
        # Configuration Swagger
        self.SWAGGER_CONFIG = {
            "title": "DiagnoCare ML API",
            "uiversion": 3,
            "openapi": "3.0.2",
        }
        
        self.SWAGGER_TEMPLATE = {
            "openapi": "3.0.2",
            "info": {
                "title": "DiagnoCare ML API",
                "version": "1.0"
            },
            "servers": [
                {"url": "http://localhost:5000", "description": "Local ML Service"},
                {"url": "http://localhost:8765", "description": "Gateway"}
            ]
        }
        
        # Configuration du logging
        logging.basicConfig(level=logging.INFO)
        self.logger = logging.getLogger(__name__)
    
    @staticmethod
    def _detect_local_ip() -> str:
        """
        Détecte l'adresse IP locale
        Returns:
            str: Adresse IP locale ou "localhost" en cas d'erreur
        """
        try:
            with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as s:
                s.connect(("8.8.8.8", 80))
                return s.getsockname()[0]
        except Exception:
            return "localhost"
    
    def get_flask_config(self) -> dict:
        """
        Retourne la configuration Flask
        Returns:
            dict: Configuration Flask
        """
        return {
            "SERVICE_NAME": self.SERVICE_NAME,
            "EUREKA_SERVICE_URL": self.EUREKA_SERVICE_URL,
            "EUREKA_INSTANCE_HOSTNAME": self.EUREKA_INSTANCE_HOSTNAME,
            "EUREKA_INSTANCE_PORT": self.EUREKA_INSTANCE_PORT,
            "EUREKA_INSTANCE_IP_ADDRESS": self.EUREKA_INSTANCE_HOSTNAME,
            "EUREKA_INSTANCE_SECURE_PORT_ENABLED": False,
            "EUREKA_INSTANCE_NON_SECURE_PORT_ENABLED": True,
            "SWAGGER": self.SWAGGER_CONFIG
        }