package com.jessy.controller;

import com.jessy.dao.evenementDao;
import com.jessy.models.evenement;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;

public class FormulaireEvenementController {

    @FXML private Label lblTitreFormulaire;
    @FXML private TextField champTitre;
    @FXML private TextArea champDescription;
    @FXML private DatePicker dateDebut;
    @FXML private ComboBox<String> heureDebut;
    @FXML private DatePicker dateFin;
    @FXML private ComboBox<String> heureFin;
    @FXML private TextField champLieu;
    @FXML private ComboBox<String> champCategorie;
    @FXML private ToggleButton couleurAccent;
    @FXML private ToggleButton couleurBleu;
    @FXML private ToggleButton couleurVert;
    @FXML private ToggleButton couleurRouge;
    @FXML private CheckBox caseRecurrence;
    @FXML private CheckBox caseRappel;
    @FXML private ComboBox<String> delaiRappel;
    @FXML private VBox panneauRecurrence;
    @FXML private Spinner<Integer> intervalleRecurrence;
    @FXML private Spinner<Integer> nbOccurrences;
    @FXML private ToggleButton freqAucune;
    @FXML private ToggleButton freqQuotidienne;
    @FXML private ToggleButton freqHebdomadaire;
    @FXML private ToggleButton freqMensuelle;
    @FXML private RadioButton finJamais;
    @FXML private RadioButton finLe;
    @FXML private RadioButton finApres;
    @FXML private Button btnValider;
    @FXML private Label lblErreurFormulaire;

    private final evenementDao dao = new evenementDao();
    private Integer idAModifier;

    @FXML
    private void initialize() {
        heureDebut.setItems(FXCollections.observableArrayList(heuresDisponibles()));
        heureFin.setItems(FXCollections.observableArrayList(heuresDisponibles()));
        champCategorie.setItems(FXCollections.observableArrayList("Cours", "Réunion", "Examen", "Autre"));
        delaiRappel.setItems(FXCollections.observableArrayList("5 minutes avant", "15 minutes avant", "30 minutes avant", "1 heure avant"));
        configurerGroupesCouleurs();
        intervalleRecurrence.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 1));
        nbOccurrences.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1));
        configurerGroupesRecurrence();
        ValidationClavier.installer(champTitre, this::enregistrerEvenement);
        caseRecurrence.selectedProperty().addListener((obs, ancienneValeur, activee) ->
            panneauRecurrence.setDisable(!activee));
        panneauRecurrence.setDisable(!caseRecurrence.isSelected());
        dateFin.setValue(LocalDate.now());
    }

    public void initialiser(Integer idAModifier, String dateInitiale) {
        this.idAModifier = idAModifier;
        if (idAModifier == null) {
            lblTitreFormulaire.setText("Créer un événement");
            btnValider.setText("Créer l'événement");
            if (dateInitiale != null && !dateInitiale.isBlank()) {
                dateDebut.setValue(parseDate(dateInitiale));
            }
            if (dateDebut.getValue() != null) {
                dateFin.setValue(dateDebut.getValue());
            }
            return;
        }

        evenement event = dao.lireEvenementParId(idAModifier);
        if (event == null) {
            afficherErreur("Événement introuvable.");
            return;
        }
        lblTitreFormulaire.setText("Modifier l'événement");
        btnValider.setText("Enregistrer les modifications");
        champTitre.setText(event.getNom());
        champDescription.setText(event.getDescription());
        champLieu.setText(event.getLieu());
        dateDebut.setValue(parseDate(event.getDateDebut()));
        dateFin.setValue(parseDate(event.getDateFin()));
        selectionnerHeure(heureDebut, event.getHeure());
        selectionnerHeure(heureFin, event.getHeureFin());
    }

    @FXML
    private void enregistrerEvenement() {
        if (champTitre.getText() == null || champTitre.getText().trim().isEmpty()) {
            afficherErreur("Le titre est obligatoire.");
            return;
        }
        if (dateDebut.getValue() == null || dateFin.getValue() == null) {
            afficherErreur("Les dates de début et de fin sont obligatoires.");
            return;
        }
        if (dateFin.getValue().isBefore(dateDebut.getValue())) {
            afficherErreur("La date de fin doit être postérieure à la date de début.");
            return;
        }

        String heure = heureDebut.getValue() == null ? "00:00" : heureDebut.getValue();
        String heureDeFin = heureFin.getValue() == null ? heure : heureFin.getValue();
        LocalDateTime debut = LocalDateTime.of(dateDebut.getValue(), LocalTime.parse(heure));
        LocalDateTime fin = LocalDateTime.of(dateFin.getValue(), LocalTime.parse(heureDeFin));
        if (!fin.isAfter(debut)) {
            afficherErreur("La date et l'heure de fin doivent être après le début.");
            return;
        }
        evenement event = new evenement(
                idAModifier == null ? 0 : idAModifier,
                champTitre.getText().trim(),
                dateDebut.getValue().toString(),
                dateFin.getValue().toString(),
                heure,
                heureDeFin,
                valeurOuVide(champLieu.getText()),
                valeurOuVide(champDescription.getText())
        );

        boolean succes;
        if (idAModifier == null) {
            succes = dao.ajouterEvenement(event);
        } else {
            succes = dao.modifierEvenement(event);
        }
        if (succes) {
            fermer();
        } else {
            afficherErreur(dao.getDerniereErreur());
        }
    }

    @FXML
    private void annuler() {
        fermer();
    }

    private void configurerGroupesCouleurs() {
        ToggleGroup groupe = new ToggleGroup();
        couleurAccent.setToggleGroup(groupe);
        couleurBleu.setToggleGroup(groupe);
        couleurVert.setToggleGroup(groupe);
        couleurRouge.setToggleGroup(groupe);
    }

    private void configurerGroupesRecurrence() {
        ToggleGroup frequences = new ToggleGroup();
        freqAucune.setToggleGroup(frequences);
        freqQuotidienne.setToggleGroup(frequences);
        freqHebdomadaire.setToggleGroup(frequences);
        freqMensuelle.setToggleGroup(frequences);

        ToggleGroup fins = new ToggleGroup();
        finJamais.setToggleGroup(fins);
        finLe.setToggleGroup(fins);
        finApres.setToggleGroup(fins);
        finJamais.setSelected(true);
    }

    private String[] heuresDisponibles() {
        return new String[] {"08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00"};
    }

    private void selectionnerHeure(ComboBox<String> combo, String heure) {
        if (heure != null && Arrays.asList(heuresDisponibles()).contains(heure)) {
            combo.setValue(heure);
        }
    }

    private LocalDate parseDate(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(valeur.length() > 10 ? valeur.substring(0, 10) : valeur);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private String valeurOuVide(String valeur) {
        return valeur == null ? "" : valeur.trim();
    }

    private void afficherErreur(String message) {
        lblErreurFormulaire.setText(message);
        lblErreurFormulaire.setVisible(true);
        lblErreurFormulaire.setManaged(true);
    }

    private void fermer() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/jessy/view/Evenements.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) champTitre.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            String css = getClass().getResource("/com/jessy/css/theme-cafe-glass.css").toExternalForm();
            scene.getStylesheets().add(css);
            stage.setScene(scene);
            stage.setMaximized(true);
        } catch (IOException | NullPointerException e) {
            afficherErreur("Impossible de revenir à la page des événements.");
        }
    }
}
