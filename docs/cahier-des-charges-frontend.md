## Cahier des charges — Frontend DiagnoCare (Figma)

### 1) Objectif du produit
DiagnoCare permet aux patients de:
- Décrire leurs symptômes et obtenir une prédiction AI (pathologies potentielles + spécialiste).
- Suivre l’évolution via des suivis (check-ins) comparant les prédictions dans le temps.
- Consulter un résumé clair et exportable en PDF.

### 2) Public cible
- Patients grand public.
- Utilisateurs non techniques, besoin d’un parcours simple, rassurant, et très lisible.

### 3) Présentation & objectifs (cahier des charges)
**Présentation du projet**
DiagnoCare est une plateforme de santé intelligente conçue pour accompagner l’utilisateur
de l’apparition des symptômes jusqu’à la consultation médicale. Contrairement aux
plateformes de gestion de cabinet, DiagnoCare se concentre sur l’analyse prédictive,
l’orientation vers le bon spécialiste et la préparation du patient, avant de le rediriger
vers des services de prise de rendez‑vous externes.

**Objectifs principaux**
- Analyse prédictive: identifier les pathologies potentielles à partir des symptômes saisis.
- Orientation intelligente: mapper les résultats vers la spécialité médicale appropriée.
- Localisation et redirection: trouver des praticiens à proximité et rediriger vers leurs
  plateformes de réservation habituelles (ex: Doctolib).
- Accompagnement: suivre l’évolution de l’état de santé et préparer le patient à sa consultation.

**Fonctionnalités clés (Phase 1: MVP)**
1) Moteur de prédiction de symptômes
   - Interface intuitive pour la saisie des symptômes.
   - Algorithme de probabilité suggérant les pathologies possibles.
   - Alerte "Red Flag": en cas de risque critique, afficher les numéros d’urgence et ne
     pas orienter vers une prise de rendez‑vous.
2) Suivi de l’évolution des symptômes
   - Historisation de la première saisie.
   - Check‑in temporel (24h / 48h) demandant amélioration, stabilité ou aggravation.
   - Réévaluation: si aggravation, recommandation vers un niveau de soin supérieur.
3) Recherche et redirection géolocalisée
   - Identification automatique des spécialistes correspondant à la pathologie prédite.
   - Recherche via Google (Google Maps Platform / Places API) pour trouver les praticiens.
   - Affichage des médecins à proximité via carte interactive (Google Maps).
   - Lien direct vers les plateformes externes pour finaliser la réservation.
4) Générateur de "Résumé de consultation"
   - Objectif: éviter l’oubli d’informations cruciales avant/pdt la consultation.
   - Génération automatique d’un résumé médical structuré après l’évaluation.
   - Format: PDF, copiable ou consultable sur téléphone.
   - Contenu clé: symptômes (durée/évolution), red flags, pathologies potentielles,
     spécialité recommandée, questions pertinentes à poser.
   - Valeur: préparation efficace sans risque médical.

**Évolutions stratégiques (Phase 2: assistant proactif)**
5) Intégration des données de santé connectées (wearables)
   - Hub de données: Google Health Connect (Android) + Apple HealthKit (iOS).
   - Données objectives: fréquence cardiaque, SpO2, sommeil, etc.
   - Précision accrue: croiser symptômes déclarés et données réelles.

**Architecture technique simplifiée**
- Frontend: interface web/mobile réactive et claire.
- Backend: API REST (prédiction + profils utilisateurs).
- Intégrations tierces:
  - Cartographie et recherche: Google Maps Platform (Maps + Places).
  - Données santé: HealthKit / Health Connect.
  - Deep‑linking vers plateformes de réservation.

**Sécurité & confidentialité**
- Anonymisation des données de symptômes lors des phases d’analyse.
- Chiffrement de bout en bout pour les résumés.
- Conformité RGPD.

### 4) Langues
- FR par défaut, EN supportée.
- Toute information affichée doit respecter la langue utilisateur.
- Bouton de changement de langue (si prévu par le produit) ou auto-détection via profil.

### 5) Règles UX globales
- Ton rassurant et médical, éviter les formulations alarmistes.
- Afficher un avertissement clair: “Ce n’est pas un diagnostic médical”.
- Toujours proposer une action suivante (rendez‑vous, questions à poser).
- Accessibilité: contrastes élevés, tailles de police lisibles, icônes explicites.

### 6) Arborescence (IA)
- Page de présentation (avant authentification)
  - Hero + proposition de valeur
  - Fonctionnalités clés
  - FAQ rapide
  - CTA “Commencer”
- Auth
  - Connexion
  - Inscription
  - Mot de passe oublié / Réinitialisation
- Accueil
  - Nouvelle évaluation
  - Dernières prédictions
  - Alertes/Red flags
- Evaluation (symptômes)
  - Sélection ou saisie libre
  - Profil médical (si manquant)
  - Résultats
- Suivi (check-ins)
  - Liste des suivis
  - Détails suivi
  - Comparaison (timeline)
- Historique
  - Toutes les prédictions
  - Filtrer par date / état / red flag
- Résumé & PDF
  - Résumé complet
  - Export PDF
- Profil & Paramètres
  - Informations personnelles
  - Profil médical
  - Langue
  - Gestion du compte
- Aide / FAQ / Contact

### 7) Écrans détaillés

#### 6.0 Page de présentation (Landing)
Sections obligatoires:
- Hero: titre, sous‑titre, CTA “Commencer”
- Valeur: “Prédiction AI”, “Suivi santé”, “PDF résumé”
- Explications: 3-4 blocs illustrés (icône + texte)
- Avertissement légal (pas un diagnostic)
- FAQ courte (3-5 questions)
- Footer (contact, mentions, confidentialité)
Actions:
- “Commencer”
- “Se connecter”

#### 6.1 Connexion / Inscription
- Champs: email, mot de passe.
- États: erreur auth, email invalide, mot de passe faible.
- CTA: “Se connecter”, “Créer un compte”.
Liens:
- “Mot de passe oublié”
- “Créer un compte”

#### 6.1.1 Mot de passe oublié / Réinitialisation
- Écran 1: email + CTA “Envoyer le lien”
- Écran 2: nouveau mot de passe + confirmation
- États: lien expiré, mot de passe invalide

#### 6.2 Accueil
- Carte “Nouvelle évaluation”.
- Bloc “Derniers résultats” (liste 3-5).
- Badge “Alerte” si red flag.
- Bouton “Voir historique”.

#### 6.3 Évaluation — Symptômes
Deux modes:
- Mode sélection: liste de symptômes (recherche + tags).
- Mode texte libre: zone de saisie (extraction automatique).
Composants:
- Champ recherche
- Tags sélectionnés (pills)
- Bouton “Analyser”
États:
- Aucun symptôme sélectionné -> CTA désactivé.
- Chargement -> skeleton + spinner.

#### 6.4 Résultat de prédiction
Doit afficher:
- Pathologies potentielles (top 3)
- Spécialité recommandée
- Score / confiance
- Explications (courtes)
- Avertissement “pas un diagnostic”
Actions:
- “Télécharger PDF”
- “Poser des questions au médecin”
- “Planifier un suivi”

#### 6.5 Suivi (check‑in)
Affichage d’un suivi:
- Date du suivi
- Statut (en attente, envoyé, terminé)
- Évolution (amélioration, stable, aggravation)
- Motif d’aggravation (si présent)
- Comparaison score (delta)
- Symptômes déclarés

#### 6.6 Timeline (chronologie)
Format carte verticale:
- Chaque entrée = une prédiction.
- Champs:
  - Type: Initial / Suivi
  - Date/heure
  - Symptômes (liste courte + “+n”)
  - Confiance
  - Variation du score (si suivi)
  - Statut + évolution
- Tri: du plus récent au plus ancien.

#### 6.7 Résumé (consultation summary)
Sections obligatoires:
- Informations patient
- Description symptômes
- Symptômes déclarés
- Pathologies potentielles
- Détails pathologies (score, spécialiste, description)
- Spécialité recommandée
- Questions à poser
- Suivi (type, statut, évolution, delta)
- Timeline
CTA:
- “Télécharger PDF”

#### 6.8 Historique
Liste paginée des prédictions:
- Date
- Résumé court (top pathologie + spécialiste)
- Badge red flag
Filtres:
- Date
- Red flag
- Type (Initial / Suivi)

#### 6.9 Profil médical
Champs:
- Âge, poids, IMC
- Tension moyenne
- Cholestérol moyen
- Sexe
- Tabac, alcool, sédentarité
- Antécédents familiaux
Objectif: enrichir la prédiction.

#### 6.10 Paramètres
- Langue
- Notifications
- Export de données (optionnel)

#### 6.11 Gestion du compte
Objectif: permettre à l’utilisateur de gérer sécurité et identité.
Sections:
- Informations personnelles: nom, prénom, email (readonly ou éditable)
- Changer mot de passe: ancien, nouveau, confirmation
- Gestion sessions: liste des connexions actives + “Déconnecter tout”
- Suppression de compte: flow de confirmation + conséquences
États:
- Succès / erreur API
- Mot de passe faible
- Email déjà utilisé

### 8) Composants UI réutilisables
- Cards résultats
- Badges (red flag, suivi)
- Timeline item
- Listes de symptômes (tags)
- Toasts succès/erreur
- Loader/skeleton

### 9) États & erreurs
Prévoir:
- Aucun résultat
- Erreur API
- Service ML indisponible
- Red flag détecté (bannière prioritaire)
- PDF généré / échec PDF

### 10) Données clés à afficher (référence API)
Pour une prédiction:
- id, date, bestScore, isRedAlert
- pathologies (nom + score)
- spécialiste recommandé
- symptômes
Pour un suivi:
- previousPredictionId
- outcome, status
- delta score

### 11) Ton et style visuel
- Couleurs sobres, médicales (bleu/gris)
- Rouge réservé aux alertes
- Icônes simples: cœur, stéthoscope, alerte, graphique
- Mise en page aérée, forte hiérarchie visuelle
