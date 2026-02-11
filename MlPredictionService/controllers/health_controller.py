"""
Contrôleur pour les endpoints de santé
"""
from flask import jsonify


class HealthController:
    """
    Contrôleur pour les endpoints de santé de l'API
    """
    
    @staticmethod
    def health_check():
        """
        Endpoint de vérification de santé
        Returns:
            tuple: (réponse JSON, code HTTP)
        """
        return jsonify({
            "status": "healthy",
            "service": "DiagnoCare ML API"
        }), 200