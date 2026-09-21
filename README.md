# CaféTime - Gestion d'emploi du temps

Application de bureau JavaFX pour gérer un emploi du temps personnel : événements, calendrier interactif, recherche, notifications et profil utilisateur.

## Fonctionnalités

- Inscription et connexion sécurisées avec mots de passe BCrypt.
- Dashboard synchronisé avec les événements de l'utilisateur.
- Création, modification et suppression d'événements.
- Calendrier mensuel interactif intégré avec `WebView`.
- Recherche asynchrone des événements en base de données.
- Affichage des événements du jour sous forme de notifications.
- Gestion du profil : affichage et modification de l'identité, changement du mot de passe et suppression du compte.
- Navigation latérale avec page active.
- Touche `Entrée` utilisable comme validation dans les principaux formulaires.
- Fenêtre maximisée sur toutes les pages.

## Technologies

- Java 11
- JavaFX 13 : Controls, FXML et WebView
- Maven
- MySQL 8+
- MySQL Connector/J 8.0.33
- jBCrypt 0.4
- HTML, CSS et JavaScript pour le calendrier

## Prérequis

Installer et vérifier :

- JDK 11 ou version compatible avec la configuration Maven ;
- Maven ;
- MySQL Server 8 ou supérieur.


## Installation et lancement

Depuis la racine du projet :

```bash
mvn clean javafx:run
```

Pour compiler sans lancer l'application :

```bash
mvn -DskipTests package
```

Le point d'entrée est `com.jessy.App`.

## Structure du projet

```text
src/main/java/com/jessy/
├── App.java
├── config/              # Connexion MySQL
├── controller/          # Contrôleurs JavaFX et session utilisateur
├── dao/                 # Accès aux données
└── models/              # Modèles métier

src/main/resources/com/jessy/
├── css/                 # Styles JavaFX et calendrier
├── js/                  # Logique du calendrier WebView
├── view/                # Interfaces FXML
└── web/                 # Page HTML du calendrier
```

## Notes

- Les données sont filtrées par l'utilisateur connecté.
- Les dates de fin doivent être strictement postérieures aux dates de début.
- La recherche est exécutée de manière asynchrone côté JavaFX avec une requête SQL `LIKE`.
- La catégorie d'un événement et la persistance complète des paramètres de récurrence nécessitent encore une extension du modèle `evenement` et du schéma SQL.
- Les notifications actuelles sont générées à partir des événements du jour ; elles ne sont pas encore stockées dans une table dédiée.
