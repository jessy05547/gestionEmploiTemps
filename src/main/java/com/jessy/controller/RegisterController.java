package com.jessy.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.jessy.dao.UserDao;
import org.mindrot.jbcrypt.BCrypt;
import java.io.IOException;
import java.sql.SQLException;
import com.jessy.models.Utilisateur;

public class RegisterController {

    private static final String LOGIN_FXML_PATH = "/com/jessy/view/login.fxml";
    private static final String CSS_PATH = "/com/jessy/css/registerStyle.css";

    @FXML private AnchorPane rootPane;
    @FXML private VBox cardRegister;
    @FXML private TextField champNom;
    @FXML private TextField champPrenom;
    @FXML private TextField champEmail;
    @FXML private PasswordField champPassword;
    @FXML private PasswordField champConfirmation;
    @FXML private Label labelErreur;

    @FXML
    private void onClickInscription() {
        String nom = champNom.getText().trim();
        String prenom = champPrenom.getText().trim();
        String email = champEmail.getText().trim();
        String motDePasse = champPassword.getText().trim();
        String confirmation = champConfirmation.getText().trim();

        if (nom.isBlank() || prenom.isBlank() || email.isBlank() || motDePasse.isBlank()) {
            afficherErreur("Merci de remplir tous les champs.");
            return;
        }

        if (!motDePasse.equals(confirmation)) {
            afficherErreur("Les mots de passe ne correspondent pas.");
            return;
        }
        
        if (!email.contains("@") || !email.contains(".")) {
            afficherErreur("Merci de saisir un email valide.");
            return;
        }

        try{
            if (new UserDao().emailExists(email)) {
                afficherErreur("Cet email est déjà utilisé.");
                return;
            }

            String hashedPassword = BCrypt.hashpw(motDePasse, BCrypt.gensalt());

            Utilisateur nouvelUtilisateur = new Utilisateur(nom, prenom, email, hashedPassword);
            nouvelUtilisateur.setPasswordHash(hashedPassword); // Stockage du hash du mot de passe
            new UserDao().inserer(nouvelUtilisateur);


            masquerErreur();
            onClickAllerLogin();
        } catch (SQLException e) {
            e.printStackTrace();
            afficherErreur("Une erreur est survenue lors de l'inscription.");
        }
        // TODO : appeler UserDao.ajouter(...) avec BCrypt.hashpw(motDePasse, BCrypt.gensalt())
        masquerErreur();
    }

    @FXML
    private void onClickAllerLogin() {
        Stage stage = (Stage) rootPane.getScene().getWindow();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(LOGIN_FXML_PATH));
            Parent root = loader.load();
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource(CSS_PATH).toExternalForm());
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void afficherErreur(String message) {
        labelErreur.setText(message);
        labelErreur.setVisible(true);
        labelErreur.setManaged(true);
    }

    private void masquerErreur() {
        labelErreur.setVisible(false);
        labelErreur.setManaged(false);
    }
}