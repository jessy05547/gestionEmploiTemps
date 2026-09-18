package com.jessy.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SidebarController implements Initializable {

    @FXML private VBox sidebarRoot;
    @FXML private Button btnDashboard;
    @FXML private Button btnEvenements;
    @FXML private Button btnCreer;
    @FXML private Button btnNotifications;
    @FXML private Button btnProfil;
    @FXML private Button btnDeconnexion;
    @FXML private ImageView avatarMini;
    @FXML private Label lblNomUtilisateur;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Pour l'instant, un nom fixe. On branchera Session.getUtilisateurConnecte()
        // une fois que la classe Session sera bien en place.
        lblNomUtilisateur.setText("Utilisateur");
    }

    private void naviguerVers(String cheminFxml, String cheminCss) {
    try {
        var url = getClass().getResource(cheminFxml);
        if (url == null) {
            System.err.println("FXML introuvable : " + cheminFxml);
            return;
        }
        Parent root = FXMLLoader.load(url);
        Scene scene = new Scene(root);

        if (cheminCss != null) {
            var cssUrl = getClass().getResource(cheminCss);
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.err.println("CSS introuvable : " + cheminCss);
            }
        }

        Stage stage = (Stage) sidebarRoot.getScene().getWindow();
        stage.setScene(scene);
    } catch (IOException e) {
        e.printStackTrace();
    }
    }

    @FXML
    private void seDeconnecter() {
        naviguerVers("/com/jessy/view/login.fxml", "/com/jessy/css/style.css");
    }

    @FXML
    private void allerEvenements() {
        naviguerVers("/com/jessy/view/Evenements.fxml", "/com/jessy/css/theme-cafe-glass.css");
    }

    @FXML
    private void allerCreerEvenement() {
        naviguerVers("/com/jessy/view/FormulaireEvenement.fxml", "/com/jessy/css/theme-cafe-glass.css");
    }

    @FXML
    private void allerNotifications() {
        naviguerVers("/com/jessy/view/Notifications.fxml", "/com/jessy/css/theme-cafe-glass.css");
    }

    @FXML
    private void allerProfil() {
        naviguerVers("/com/jessy/view/Profil.fxml", "/com/jessy/css/theme-cafe-glass.css");
    }

    @FXML
    private void allerDashboard() {
        naviguerVers("/com/jessy/view/dashboard.fxml", "/com/jessy/css/theme-cafe-glass.css");
    }
}