package com.jessy.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label lblDateAujourdhui;
    @FXML private Button btnRechercher;
    @FXML private Button btnNotifTop;

    @FXML private Label lblStatAujourdhui;
    @FXML private Label lblStatSemaine;
    @FXML private Label lblStatRecurrents;
    @FXML private Label lblStatNotifs;

    @FXML private ListView<String> listeAgendaJour;

    @FXML private Label lblMoisAffiche;
    @FXML private GridPane grilleMiniCalendrier;
    @FXML private VBox listeProchainsEvenements;

    private LocalDate moisAffiche = LocalDate.now();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        afficherDateAujourdhui();
        afficherMoisCourant();
        // TODO : charger les vraies statistiques / événements depuis la base
    }

    private void afficherDateAujourdhui() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);
        String texte = LocalDate.now().format(formatter);
        lblDateAujourdhui.setText(capitaliser(texte));
    }

    private void afficherMoisCourant() {
        String nomMois = moisAffiche.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        lblMoisAffiche.setText(capitaliser(nomMois) + " " + moisAffiche.getYear());
    }

    private String capitaliser(String texte) {
        if (texte == null || texte.isEmpty()) return texte;
        return texte.substring(0, 1).toUpperCase() + texte.substring(1);
    }

    @FXML
    private void nouvelEvenement() {
        naviguerVers("/com/jessy/view/FormulaireEvenement.fxml");
    }

    @FXML
    private void allerEvenements() {
        naviguerVers("/com/jessy/view/Evenements.fxml");
    }

    @FXML
    private void moisPrecedent() {
        moisAffiche = moisAffiche.minusMonths(1);
        afficherMoisCourant();
        // TODO : rafraîchir grilleMiniCalendrier avec le nouveau mois
    }

    @FXML
    private void moisSuivant() {
        moisAffiche = moisAffiche.plusMonths(1);
        afficherMoisCourant();
        // TODO : rafraîchir grilleMiniCalendrier avec le nouveau mois
    }

    private void naviguerVers(String cheminFxml) {
        try {
            var url = getClass().getResource(cheminFxml);
            if (url == null) {
                System.err.println("FXML introuvable : " + cheminFxml);
                return;
            }
            Parent root = FXMLLoader.load(url);
            Stage stage = (Stage) lblDateAujourdhui.getScene().getWindow();
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}