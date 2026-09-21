package com.jessy.controller;

import com.jessy.dao.evenementDao;
import com.jessy.models.evenement;
import com.jessy.models.Utilisateur;
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
import javafx.geometry.Pos;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label lblDateAujourdhui;
    @FXML private Label lblBienvenue;
    @FXML private Button btnRechercher;
    @FXML private Button btnNotifTop;

    @FXML private Label lblStatAujourdhui;
    @FXML private Label lblStatSemaine;
    @FXML private Label lblStatRecurrents;
    @FXML private Label lblStatNotifs;
    @FXML private Label lblDetailAujourdhui;
    @FXML private Label lblDetailSemaine;
    @FXML private Label lblDetailRecurrents;
    @FXML private Label lblDetailNotifs;

    @FXML private ListView<String> listeAgendaJour;

    @FXML private Label lblMoisAffiche;
    @FXML private GridPane grilleMiniCalendrier;
    @FXML private VBox listeProchainsEvenements;

    private LocalDate moisAffiche = LocalDate.now();
    private final evenementDao dao = new evenementDao();
    private List<evenement> evenements = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        afficherDateAujourdhui();
        afficherMoisCourant();
        afficherUtilisateur();
        rafraichirDonnees();
    }

    private void afficherDateAujourdhui() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);
        String texte = LocalDate.now().format(formatter);
        lblDateAujourdhui.setText(capitaliser(texte));
    }

    private void afficherMoisCourant() {
        String nomMois = moisAffiche.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        lblMoisAffiche.setText(capitaliser(nomMois) + " " + moisAffiche.getYear());
        construireMiniCalendrier();
    }

    private void afficherUtilisateur() {
        if (Session.estConnecte()) {
            Utilisateur utilisateur = Session.getUtilisateurConnecte();
            lblBienvenue.setText("Bonjour, " + utilisateur.getPrenom() + " ☕");
        }
    }

    private void rafraichirDonnees() {
        evenements = dao.lireTousLesEvenements();
        LocalDate aujourdHui = LocalDate.now();
        long aujourdHuiCount = evenements.stream().filter(e -> aujourdHui.equals(dateDe(e))).count();
        long semaineCount = evenements.stream().filter(e -> {
            LocalDate date = dateDe(e);
            return date != null && !date.isBefore(aujourdHui) && !date.isAfter(aujourdHui.plusDays(6));
        }).count();
        lblStatAujourdhui.setText(String.valueOf(aujourdHuiCount));
        lblStatSemaine.setText(String.valueOf(semaineCount));
        lblStatRecurrents.setText(String.valueOf(dao.compterRecurrences()));
        lblStatNotifs.setText(String.valueOf(aujourdHuiCount));
        lblDetailAujourdhui.setText(aujourdHuiCount == 0 ? "Aucun événement prévu" : "Événement(s) prévu(s) aujourd'hui");
        lblDetailSemaine.setText("Dans les 7 prochains jours");
        lblDetailNotifs.setText("Rappels du jour");
        afficherAgendaDuJour(aujourdHui);
        afficherProchainsEvenements(aujourdHui);
        construireMiniCalendrier();
    }

    private void afficherAgendaDuJour(LocalDate date) {
        List<String> agenda = new ArrayList<>();
        evenements.stream().filter(e -> date.equals(dateDe(e))).forEach(e ->
                agenda.add(heureDe(e) + "  ·  " + e.getNom()
                        + (e.getLieu() == null || e.getLieu().isBlank() ? "" : "  ·  " + e.getLieu())));
        listeAgendaJour.setItems(javafx.collections.FXCollections.observableArrayList(agenda));
        if (agenda.isEmpty()) listeAgendaJour.setPlaceholder(new Label("Aucun événement aujourd'hui"));
    }

    private void afficherProchainsEvenements(LocalDate date) {
        listeProchainsEvenements.getChildren().clear();
        evenements.stream().filter(e -> dateDe(e) != null && !dateDe(e).isBefore(date))
                .limit(6).forEach(e -> {
                    Label titre = new Label(e.getNom());
                    titre.getStyleClass().add("body-text");
                    Label details = new Label(dateDe(e) + " · " + heureDe(e));
                    details.getStyleClass().add("muted-text");
                    VBox ligne = new VBox(3, titre, details);
                    ligne.getStyleClass().add("notif-item");
                    listeProchainsEvenements.getChildren().add(ligne);
                });
    }

    private void construireMiniCalendrier() {
        if (grilleMiniCalendrier == null) return;
        grilleMiniCalendrier.getChildren().clear();
        String[] jours = {"L", "M", "M", "J", "V", "S", "D"};
        for (int i = 0; i < jours.length; i++) {
            Label jour = new Label(jours[i]);
            jour.getStyleClass().add("muted-text");
            jour.setMaxWidth(Double.MAX_VALUE);
            jour.setAlignment(Pos.CENTER);
            grilleMiniCalendrier.add(jour, i, 0);
        }
        YearMonth mois = YearMonth.from(moisAffiche);
        int debut = mois.atDay(1).getDayOfWeek().getValue() - 1;
        for (int jourNumero = 1; jourNumero <= mois.lengthOfMonth(); jourNumero++) {
            LocalDate date = mois.atDay(jourNumero);
            Label cellule = new Label(String.valueOf(jourNumero));
            cellule.setMaxWidth(Double.MAX_VALUE);
            cellule.setAlignment(Pos.CENTER);
            cellule.getStyleClass().add("calendar-day");
            if (date.equals(LocalDate.now())) cellule.getStyleClass().add("calendar-day-today");
            if (evenements.stream().anyMatch(e -> date.equals(dateDe(e)))) cellule.getStyleClass().add("calendar-day-event");
            grilleMiniCalendrier.add(cellule, (debut + jourNumero - 1) % 7, 1 + (debut + jourNumero - 1) / 7);
        }
    }

    private LocalDate dateDe(evenement event) {
        return event == null || event.getDateDebut() == null ? null : LocalDate.parse(event.getDateDebut());
    }

    private String heureDe(evenement event) {
        return event.getHeure() == null ? "--:--" : event.getHeure().substring(0, Math.min(5, event.getHeure().length()));
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
    private void ouvrirRecherche() {
        naviguerVers("/com/jessy/view/Evenements.fxml");
    }

    @FXML
    private void ouvrirNotifications() {
        naviguerVers("/com/jessy/view/Notifications.fxml");
    }

    @FXML
    private void moisPrecedent() {
        moisAffiche = moisAffiche.minusMonths(1);
        afficherMoisCourant();
    }

    @FXML
    private void moisSuivant() {
        moisAffiche = moisAffiche.plusMonths(1);
        afficherMoisCourant();
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
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            scene.getStylesheets().add(getClass().getResource("/com/jessy/css/theme-cafe-glass.css").toExternalForm());
            stage.setScene(scene);
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}