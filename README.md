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

## Configuration de la base de données

La connexion est définie dans `src/main/java/com/jessy/config/Connexion.java` :

```java
jdbc:mysql://localhost:3306/gestion_emploie_temps
Utilisateur : root
Mot de passe :
```

Créer la base et les tables :

```sql
CREATE DATABASE gestion_emploie_temps
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE gestion_emploie_temps;

CREATE TABLE utilisateur (
    id_utilisateur INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    prenom VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE evenement (
    id_evenement INT AUTO_INCREMENT PRIMARY KEY,
    fk_id_utilisateur INT NOT NULL,
    date_debut DATETIME NOT NULL,
    date_fin DATETIME NOT NULL,
    titre VARCHAR(100) NOT NULL,
    description TEXT,
    lieu VARCHAR(100) NOT NULL,
    CONSTRAINT evenement_chk_1 CHECK (date_fin > date_debut),
    CONSTRAINT evenement_ibfk_1 FOREIGN KEY (fk_id_utilisateur)
        REFERENCES utilisateur(id_utilisateur)
);

CREATE TABLE recurrence (
    id_recurrence INT AUTO_INCREMENT PRIMARY KEY,
    fk_id_even INT NOT NULL,
    type_recurrence VARCHAR(100) NOT NULL,
    intervalle INT DEFAULT 1,
    date_fin_recurrence DATE,
    CONSTRAINT recurrence_fk_evenement FOREIGN KEY (fk_id_even)
        REFERENCES evenement(id_evenement)
);
```

Adapter l'URL, l'utilisateur et le mot de passe dans `Connexion.java` si nécessaire.

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
