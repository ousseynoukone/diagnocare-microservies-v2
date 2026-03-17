"""
Point d’entrée pour l’entraînement du modèle de prédiction maladies / spécialistes.

Lance : chargement des données, construction des features, entraînement,
évaluation (accuracy, top-k, matrices de confusion), sauvegarde des .joblib.

Usage (depuis la racine MlPredictionService) :
    python training/train_model.py
"""
import os
import sys

# Permettre les imports du projet (config, utils) et des modules du dossier training/
_base = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
_training = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, _base)
sys.path.insert(0, _training)

from model_trainer import ModelTrainer


def main():
    trainer = ModelTrainer()
    trainer.train()


if __name__ == "__main__":
    main()
