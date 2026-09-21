package com.jessy.controller;

import com.jessy.dao.evenementDao;
import com.jessy.models.evenement;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

public class NotificationsController {
    @FXML private VBox listeNotifications;
    @FXML private VBox etatVide;
    @FXML private Label lblCompteurNonLues;
    @FXML private ToggleButton filtreToutes;
    @FXML private ToggleButton filtreRappels;
    @FXML private ToggleButton filtreModifs;
    @FXML private ToggleButton filtreSysteme;

    private final evenementDao dao = new evenementDao();

    @FXML
    private void initialize() {
        ToggleGroup filtres = new ToggleGroup();
        filtreToutes.setToggleGroup(filtres);
        filtreRappels.setToggleGroup(filtres);
        filtreModifs.setToggleGroup(filtres);
        filtreSysteme.setToggleGroup(filtres);
        filtres.selectedToggleProperty().addListener((obs, ancien, nouveau) -> chargerNotifications());
        chargerNotifications();
    }

    private void chargerNotifications() {
        List<evenement> evenements = dao.lireTousLesEvenements();
        listeNotifications.getChildren().clear();
        int count = 0;
        for (evenement evenement : evenements) {
            if (evenement.getDateDebut() == null || !evenement.getDateDebut().equals(LocalDate.now().toString())) {
                continue;
            }
            count++;
            HBox notification = new HBox(14);
            notification.getStyleClass().add("notif-item");
            VBox contenu = new VBox(3);
            Label titre = new Label("Rappel : " + evenement.getNom());
            titre.getStyleClass().add("body-text");
            Label details = new Label(evenement.getHeure() + " · " + valeur(evenement.getLieu()));
            details.getStyleClass().add("muted-text");
            contenu.getChildren().addAll(titre, details);
            notification.getChildren().add(contenu);
            listeNotifications.getChildren().add(notification);
        }
        lblCompteurNonLues.setText(count + " notification(s) aujourd'hui");
        etatVide.setVisible(count == 0);
        etatVide.setManaged(count == 0);
    }

    @FXML
    private void toutMarquerLu() {
        lblCompteurNonLues.setText("0 notification(s) non lue(s)");
    }

    @FXML
    private void viderNotifications() {
        listeNotifications.getChildren().clear();
        etatVide.setVisible(true);
        etatVide.setManaged(true);
        lblCompteurNonLues.setText("0 notification(s)");
    }

    private String valeur(String valeur) {
        return valeur == null || valeur.isBlank() ? "Lieu non précisé" : valeur;
    }
}