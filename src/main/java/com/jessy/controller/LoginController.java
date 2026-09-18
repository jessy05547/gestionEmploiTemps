package com.jessy.controller;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.stage.Stage;
import javafx.scene.Scene;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import com.jessy.dao.UserDao;
import com.jessy.models.Utilisateur;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.SQLException;
import com.jessy.controller.Session;


public class LoginController {
    private static final String REGISTER_FXML_PATH = "/com/jessy/view/register.fxml";
    private static final String CSS_PATH = "/com/jessy/css/registerStyle.css";
    @FXML private AnchorPane rootPane;
    @FXML private VBox cardLogin;
    @FXML private TextField champEmail;
    @FXML private PasswordField champPassword;
    @FXML private Label labelErreur;

    @FXML
    public void initialize() {
        animerApparitionCarte();
        rendreResponsive();
    }

    /**
     * Animation d'entrée : la carte apparaît en fondu + léger glissement vers le haut.
     * (Impossible en CSS pur en JavaFX — on utilise l'API Animation de Java)
     */
    private void animerApparitionCarte() {
        cardLogin.setOpacity(0);
        cardLogin.setTranslateY(30);

        FadeTransition fade = new FadeTransition(Duration.millis(500), cardLogin);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition glisse = new TranslateTransition(Duration.millis(500), cardLogin);
        glisse.setFromY(30);
        glisse.setToY(0);

        fade.play();
        glisse.play();
    }

    /**
     * Responsivité : la carte garde une largeur confortable sur grand écran,
     * mais se resserre sur petite fenêtre plutôt que de déborder.
     */
    private void rendreResponsive() {
        // Limite à 380px sur grand écran, mais ne dépasse jamais 90% de la largeur de la fenêtre
        rootPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            double largeurDisponible = newVal.doubleValue() * 0.9;
            cardLogin.setMaxWidth(Math.min(380, largeurDisponible));
        });
    }

    private final UserDao userDao = new UserDao();

    @FXML
    private void onClickConnexion() {
        String email = champEmail.getText().trim();
        String motDePasse = champPassword.getText();

        if (email.isBlank() || motDePasse.isBlank()) {
            afficherErreur("Merci de remplir tous les champs.");
            return;
        }

        try {
            Utilisateur utilisateur = userDao.trouverParEmail(email);

            if (utilisateur == null) {
                afficherErreur("Email ou mot de passe incorrect.");
                return;
            }

            boolean motDePasseValide = BCrypt.checkpw(motDePasse, utilisateur.getPasswordHash());

            if (!motDePasseValide) {
                afficherErreur("Email ou mot de passe incorrect.");
                return;
            }

            masquerErreur();
            Session.connecter(utilisateur);
            onClickAllerDashboard(); // TODO : rediriger vers la page d'accueil / tableau de bord après connexion réussie

        } catch (SQLException e) {
            e.printStackTrace();
            afficherErreur("Erreur de connexion à la base de données.");
        }
    }
    @FXML
    private void onClickAllerDashboard() {
        Stage stage = (Stage) rootPane.getScene().getWindow();

        try {
            var fxmlUrl = getClass().getResource("/com/jessy/view/dashboard.fxml");
            if (fxmlUrl == null) {
                System.err.println("FXML introuvable : /com/jessy/view/dashboard.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Scene scene = new Scene(root, 900, 600);

            var cssUrl = getClass().getResource("/com/jessy/css/theme-cafe-glass.css");
            if (cssUrl == null) {
                System.err.println("CSS introuvable : /com/jessy/css/theme-cafe-glass.css");
            } else {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            stage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onClickAllerRegister() {
    Stage stage = (Stage) rootPane.getScene().getWindow();

    try {
        var fxmlUrl = getClass().getResource(REGISTER_FXML_PATH);
        if (fxmlUrl == null) {
            System.err.println("FXML introuvable : " + REGISTER_FXML_PATH);
            return;
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();
        Scene scene = new Scene(root, 900, 600);

        var cssUrl = getClass().getResource(CSS_PATH);
        if (cssUrl == null) {
            System.err.println("CSS introuvable : " + CSS_PATH);
        } else {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

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
