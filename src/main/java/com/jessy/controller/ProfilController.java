package com.jessy.controller;

import com.jessy.dao.UserDao;
import com.jessy.models.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class ProfilController {
    @FXML private TextField champNom;
    @FXML private TextField champPrenom;
    @FXML private TextField champEmail;
    @FXML private ComboBox<String> champFuseauHoraire;
    @FXML private PasswordField champMotDePasseActuel;
    @FXML private PasswordField champNouveauMotDePasse;
    @FXML private PasswordField champConfirmationMotDePasse;
    @FXML private Label lblNomComplet;
    @FXML private Label lblEmailProfil;
    @FXML private Label lblErreurProfil;
    @FXML private CheckBox prefRappels;
    @FXML private CheckBox prefEmail;
    @FXML private CheckBox prefRecurrence;

    private final UserDao userDao = new UserDao();

    @FXML
    private void initialize() {
        champFuseauHoraire.getItems().addAll("Indian/Antananarivo (GMT+3)", "Europe/Paris (GMT+1)", "UTC (GMT+0)");
        champFuseauHoraire.getSelectionModel().selectFirst();
        afficherUtilisateur();
        ValidationClavier.installer(champNom, this::enregistrerProfil);
    }

    private void afficherUtilisateur() {
        Utilisateur utilisateur = Session.getUtilisateurConnecte();
        if (utilisateur == null) {
            afficherErreur("Aucun utilisateur connecté.");
            return;
        }
        champNom.setText(utilisateur.getNom());
        champPrenom.setText(utilisateur.getPrenom());
        champEmail.setText(utilisateur.getEmail());
        lblNomComplet.setText(utilisateur.getPrenom() + " " + utilisateur.getNom());
        lblEmailProfil.setText(utilisateur.getEmail());
    }

    @FXML
    private void enregistrerProfil() {
        Utilisateur utilisateur = Session.getUtilisateurConnecte();
        if (utilisateur == null) {
            afficherErreur("Aucun utilisateur connecté.");
            return;
        }
        String nom = champNom.getText().trim();
        String prenom = champPrenom.getText().trim();
        String email = champEmail.getText().trim();
        if (nom.isBlank() || prenom.isBlank() || email.isBlank() || !email.contains("@")) {
            afficherErreur("Veuillez saisir un nom, un prénom et un e-mail valides.");
            return;
        }
        try {
            if (!email.equalsIgnoreCase(utilisateur.getEmail()) && userDao.emailExists(email)) {
                afficherErreur("Cet e-mail est déjà utilisé.");
                return;
            }
            utilisateur.setNom(nom);
            utilisateur.setPrenom(prenom);
            utilisateur.setEmail(email);
            userDao.modifierProfil(utilisateur);
            afficherUtilisateur();
            afficherErreur("Profil enregistré.");
        } catch (Exception e) {
            afficherErreur("Impossible de modifier le profil : " + e.getMessage());
        }
    }

    @FXML
    private void changerMotDePasse() {
        Utilisateur utilisateur = Session.getUtilisateurConnecte();
        String actuel = champMotDePasseActuel.getText();
        String nouveau = champNouveauMotDePasse.getText();
        if (!userDao.verifierMotDePasse(utilisateur, actuel)) {
            afficherErreur("Le mot de passe actuel est incorrect.");
            return;
        }
        if (nouveau.isBlank() || !nouveau.equals(champConfirmationMotDePasse.getText())) {
            afficherErreur("Les nouveaux mots de passe ne correspondent pas.");
            return;
        }
        try {
            userDao.modifierMotDePasse(utilisateur, nouveau);
            champMotDePasseActuel.clear();
            champNouveauMotDePasse.clear();
            champConfirmationMotDePasse.clear();
            afficherErreur("Mot de passe modifié.");
        } catch (Exception e) {
            afficherErreur("Impossible de modifier le mot de passe : " + e.getMessage());
        }
    }

    @FXML
    private void supprimerCompte() {
        Utilisateur utilisateur = Session.getUtilisateurConnecte();
        if (utilisateur == null) {
            return;
        }
        try {
            userDao.supprimerCompte(utilisateur.idUtilisateur);
            Session.deconnecter();
            naviguer("/com/jessy/view/login.fxml", "/com/jessy/css/style.css");
        } catch (Exception e) {
            afficherErreur("Impossible de supprimer le compte : " + e.getMessage());
        }
    }

    @FXML
    private void seDeconnecter() {
        Session.deconnecter();
        naviguer("/com/jessy/view/login.fxml", "/com/jessy/css/style.css");
    }

    @FXML
    private void changerPhoto() {
        afficherErreur("La photo de profil sera disponible prochainement.");
    }

    private void naviguer(String fxml, String css) {
        try {
            Stage stage = (Stage) champNom.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            scene.getStylesheets().add(getClass().getResource(css).toExternalForm());
            stage.setScene(scene);
            stage.setMaximized(true);
        } catch (IOException e) {
            afficherErreur("Impossible de changer de page.");
        }
    }

    private void afficherErreur(String message) {
        lblErreurProfil.setText(message);
        lblErreurProfil.setVisible(true);
        lblErreurProfil.setManaged(true);
    }
}
