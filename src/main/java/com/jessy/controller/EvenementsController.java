package com.jessy.controller;

import com.jessy.dao.evenementDao;
import com.jessy.models.evenement;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur de la page Événements : bascule Calendrier (WebView + calendar.js) / Liste native.
 *
 * Le pont Java <-> JS suit exactement le contrat documenté en tête de calendar.js :
 *   - Java appelle window.setEvenements(jsonArray) pour injecter les événements.
 *   - Java expose window.javaBridge avec onCreerEvenement / onModifierEvenement /
 *     onSupprimerEvenement / onChangerMois.
 *
 * Limite actuelle : le modèle `evenement` n'a pas encore de champ "categorie" ni
 * "recurrent" (recurrenceDao n'est pas implémenté). On envoie donc une catégorie
 * par défaut et recurrent=false ; à revoir quand ces colonnes existeront.
 */
public class EvenementsController implements Initializable {

    @FXML private TextField champRecherche;
    @FXML private ComboBox<String> filtreCategorie;
    @FXML private ToggleButton toggleVueCalendrier;
    @FXML private ToggleButton toggleVueListe;
    @FXML private WebView webCalendrier;
    @FXML private ScrollPane scrollListe;
    @FXML private VBox listeEvenementsBox;

    private final evenementDao dao = new evenementDao();
    private WebEngine webEngine;
    private long rechercheVersion;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurerToggleVues();
        configurerRecherche();
        configurerFiltreCategorie();
        configurerWebViewCalendrier();
        rafraichirListeNative();
    }

    // ---------- Bascule Calendrier / Liste ----------

    private void configurerToggleVues() {
        ToggleGroup groupe = new ToggleGroup();
        toggleVueCalendrier.setToggleGroup(groupe);
        toggleVueListe.setToggleGroup(groupe);

        groupe.selectedToggleProperty().addListener((obs, ancien, nouveau) -> {
            if (nouveau == null) {
                // Empêche de désélectionner les deux boutons en même temps
                groupe.selectToggle(ancien != null ? ancien : toggleVueCalendrier);
                return;
            }
            boolean vueCalendrier = nouveau == toggleVueCalendrier;
            webCalendrier.setVisible(vueCalendrier);
            webCalendrier.setManaged(vueCalendrier);
            scrollListe.setVisible(!vueCalendrier);
            scrollListe.setManaged(!vueCalendrier);
        });
    }

    // ---------- Recherche / filtre catégorie ----------

    private void configurerRecherche() {
        champRecherche.textProperty().addListener((obs, ancien, nouveau) -> rechercherEvenementsAsync(nouveau));
    }

    private void configurerFiltreCategorie() {
        // Pas encore de colonne "categorie" en base : filtre désactivé pour l'instant.
        filtreCategorie.setPromptText("Catégorie (bientôt)");
        filtreCategorie.setDisable(true);
    }

    // ---------- Vue Calendrier (WebView + calendar.js) ----------

    private void configurerWebViewCalendrier() {
        webEngine = webCalendrier.getEngine();

        webEngine.getLoadWorker().stateProperty().addListener((obs, ancien, nouveau) -> {
            if (nouveau == Worker.State.SUCCEEDED) {
                JSObject fenetre = (JSObject) webEngine.executeScript("window");
                fenetre.setMember("javaBridge", new JavaBridge());
                injecterEvenementsDansCalendrier();
            }
        });

        URL urlCalendrier = getClass().getResource("/com/jessy/web/calendar.html");
        if (urlCalendrier == null) {
            System.err.println("calendar.html introuvable dans les ressources.");
            return;
        }
        webEngine.load(urlCalendrier.toExternalForm());
    }

    private void injecterEvenementsDansCalendrier() {
        injecterEvenementsDansCalendrier(dao.lireTousLesEvenements());
    }

    private void injecterEvenementsDansCalendrier(List<evenement> evenements) {
        if (webEngine == null) return;
        String json = construireJsonEvenements(evenements);
        Object apiDisponible = webEngine.executeScript("typeof window.setEvenements === 'function'");
        if (Boolean.TRUE.equals(apiDisponible)) {
            webEngine.executeScript("window.setEvenements(" + json + ")");
        }
    }

    private String construireJsonEvenements(List<evenement> evenements) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < evenements.size(); i++) {
            evenement e = evenements.get(i);
            if (i > 0) sb.append(",");
            sb.append("{");
            sb.append("\"id\":\"").append(e.getId()).append("\",");
            sb.append("\"titre\":\"").append(echapper(e.getNom())).append("\",");
            sb.append("\"dateDebut\":\"").append(echapper(versIso(e.getDateDebut(), e.getHeure()))).append("\",");
            sb.append("\"dateFin\":\"").append(echapper(versIso(e.getDateFin(), e.getHeureFin()))).append("\",");
            sb.append("\"lieu\":\"").append(echapper(e.getLieu())).append("\",");
            // TODO : remplacer par la vraie catégorie une fois la colonne ajoutée en base
            sb.append("\"categorie\":\"autre\",");
            // TODO : brancher sur recurrenceDao une fois implémenté
            sb.append("\"recurrent\":false");
            sb.append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Combine une date ("yyyy-MM-dd") et une heure ("HH:mm") en ISO local ("yyyy-MM-ddTHH:mm:ss").
     * Si la date contient déjà un "T", elle est renvoyée telle quelle.
     */
    private String versIso(String date, String heure) {
        if (date == null) return "";
        if (date.contains("T")) return date;
        String h = (heure == null || heure.isBlank()) ? "00:00" : heure;
        return date + "T" + h + ":00";
    }

    private String echapper(String valeur) {
        if (valeur == null) return "";
        return valeur.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }

    // ---------- Vue Liste native ----------

    private void rafraichirListeNative() {
        afficherEvenements(dao.lireTousLesEvenements());
    }

    private void rechercherEvenementsAsync(String recherche) {
        long version = ++rechercheVersion;
        Task<List<evenement>> rechercheTask = new Task<>() {
            @Override
            protected List<evenement> call() {
                return dao.rechercherEvenements(recherche);
            }
        };
        rechercheTask.setOnSucceeded(event -> {
            if (version != rechercheVersion) return;
            List<evenement> resultats = rechercheTask.getValue();
            afficherEvenements(resultats);
            injecterEvenementsDansCalendrier(resultats);
        });
        Thread thread = new Thread(rechercheTask, "recherche-evenements");
        thread.setDaemon(true);
        thread.start();
    }

    private void afficherEvenements(List<evenement> evenements) {

        listeEvenementsBox.getChildren().clear();
        for (evenement e : evenements) {
            listeEvenementsBox.getChildren().add(construireCarteEvenement(e));
        }
    }

    private VBox construireCarteEvenement(evenement e) {
        Label titre = new Label(e.getNom());
        titre.getStyleClass().add("title-md");

        Label details = new Label(
                (e.getDateDebut() != null ? e.getDateDebut() : "")
                + (e.getHeure() != null ? " · " + e.getHeure() : "")
                + (e.getLieu() != null && !e.getLieu().isBlank() ? " · " + e.getLieu() : "")
        );
        details.getStyleClass().add("subtitle");

        Button btnModifier = new Button("Modifier");
        btnModifier.setOnAction(ev -> ouvrirFormulaire(e.getId(), null));

        Button btnSupprimer = new Button("Supprimer");
        btnSupprimer.setOnAction(ev -> {
            supprimerEvenement(e.getId());
        });

        Region espace = new Region();
        HBox.setHgrow(espace, Priority.ALWAYS);
        HBox actions = new HBox(8, btnModifier, btnSupprimer);
        actions.setAlignment(Pos.CENTER_RIGHT);

        HBox ligne = new HBox(12, new VBox(2, titre, details), espace, actions);
        ligne.setAlignment(Pos.CENTER_LEFT);

        VBox carte = new VBox(ligne);
        carte.getStyleClass().add("glass-pane");
        carte.setPadding(new Insets(14));
        return carte;
    }

    private void supprimerEvenement(int id) {
        if (!dao.supprimerEvenement(id)) {
            System.err.println("Suppression impossible : " + dao.getDerniereErreur());
            return;
        }
        String recherche = champRecherche.getText();
        if (recherche == null || recherche.isBlank()) {
            rafraichirListeNative();
            injecterEvenementsDansCalendrier();
        } else {
            rechercherEvenementsAsync(recherche);
        }
    }

    // ---------- Navigation ----------

    @FXML
    private void nouvelEvenement() {
        ouvrirFormulaire(null, null);
    }

    /**
     * Ouvre FormulaireEvenement.fxml en création (idAModifier == null) ou modification.
     */
    private void ouvrirFormulaire(Integer idAModifier, String dateInitiale) {
        try {
            URL url = getClass().getResource("/com/jessy/view/FormulaireEvenement.fxml");
            if (url == null) {
                System.err.println("FormulaireEvenement.fxml introuvable.");
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            FormulaireEvenementController controleur = loader.getController();
            controleur.initialiser(idAModifier, dateInitiale);

            Stage stage = (Stage) listeEvenementsBox.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            URL cssUrl = getClass().getResource("/com/jessy/css/theme-cafe-glass.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            stage.setScene(scene);
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ---------- Pont Java <-> JS (appelé depuis calendar.js) ----------

    public class JavaBridge {

        public void onCreerEvenement(String dateISO) {
            Platform.runLater(() -> ouvrirFormulaire(null, dateISO));
        }

        public void onModifierEvenement(String idEvenement) {
            Platform.runLater(() -> {
                try {
                    ouvrirFormulaire(Integer.parseInt(idEvenement), null);
                } catch (NumberFormatException e) {
                    System.err.println("Id d'événement invalide : " + idEvenement);
                }
            });
        }

        public void onSupprimerEvenement(String idEvenement) {
            Platform.runLater(() -> {
                try {
                    supprimerEvenement(Integer.parseInt(idEvenement));
                } catch (NumberFormatException e) {
                    System.err.println("Id d'événement invalide : " + idEvenement);
                }
            });
        }

        public void onChangerMois(int anneeAffichee, int moisAffiche) {
            // calendar.js filtre déjà côté client à partir de la liste complète injectée ;
            // rien à recharger depuis la base pour l'instant.
        }
    }
}