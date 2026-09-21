package com.jessy.dao;
import com.jessy.models.evenement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import com.jessy.config.Connexion;
import com.jessy.controller.Session;

public class evenementDao {

    private String derniereErreur = "Impossible d'enregistrer l'événement.";

    public String getDerniereErreur() {
        return derniereErreur;
    }

    public boolean ajouterEvenement(evenement event) {
        Integer utilisateurId = utilisateurConnecteId();
        if (utilisateurId == null) {
            derniereErreur = "Aucun utilisateur connecté.";
            return false;
        }
        String sql = "INSERT INTO evenement (fk_id_utilisateur, date_debut, date_fin, titre, description, lieu) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Connexion.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, utilisateurId);
            pstmt.setTimestamp(2, Timestamp.valueOf(versDateHeure(event.getDateDebut(), event.getHeure())));
            pstmt.setTimestamp(3, Timestamp.valueOf(versDateHeure(event.getDateFin(), event.getHeureFin())));
            pstmt.setString(4, event.getNom());
            pstmt.setString(5, event.getDescription());
            pstmt.setString(6, event.getLieu());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            derniereErreur = "Erreur de base de données : " + e.getMessage();
            return false;
        }
    }

    /**
     * Récupère tous les événements, triés par date de début puis heure.
     */
    public List<evenement> lireTousLesEvenements() {
        List<evenement> evenements = new ArrayList<>();
        Integer utilisateurId = utilisateurConnecteId();
        if (utilisateurId == null) {
            return evenements;
        }
        String sql = "SELECT id_evenement, date_debut, date_fin, titre, description, lieu FROM evenement WHERE fk_id_utilisateur = ? ORDER BY date_debut";
        try (Connection conn = Connexion.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ) {
            pstmt.setInt(1, utilisateurId);
            try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                    evenements.add(mapResultSet(rs));
            }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return evenements;
    }

    public List<evenement> rechercherEvenements(String recherche) {
        List<evenement> evenements = new ArrayList<>();
        Integer utilisateurId = utilisateurConnecteId();
        if (utilisateurId == null) {
            return evenements;
        }
        String terme = recherche == null ? "" : recherche.trim();
        String sql = "SELECT id_evenement, date_debut, date_fin, titre, description, lieu "
                + "FROM evenement WHERE fk_id_utilisateur = ? "
                + "AND (titre LIKE ? OR lieu LIKE ? OR description LIKE ?) ORDER BY date_debut";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String motif = "%" + terme + "%";
            pstmt.setInt(1, utilisateurId);
            pstmt.setString(2, motif);
            pstmt.setString(3, motif);
            pstmt.setString(4, motif);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    evenements.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return evenements;
    }

    public int compterRecurrences() {
        Integer utilisateurId = utilisateurConnecteId();
        if (utilisateurId == null) return 0;
        String sql = "SELECT COUNT(*) FROM recurrence r INNER JOIN evenement e "
                + "ON e.id_evenement = r.fk_id_even WHERE e.fk_id_utilisateur = ?";
        try (Connection conn = Connexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, utilisateurId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Récupère un événement précis par son id, ou null s'il n'existe pas.
     */
    public evenement lireEvenementParId(int id) {
        Integer utilisateurId = utilisateurConnecteId();
        if (utilisateurId == null) {
            return null;
        }
        String sql = "SELECT id_evenement, date_debut, date_fin, titre, description, lieu FROM evenement WHERE id_evenement = ? AND fk_id_utilisateur = ?";
        try (Connection conn = Connexion.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setInt(2, utilisateurId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Met à jour un événement existant (identifié par event.getId()).
     * Retourne true si une ligne a bien été modifiée.
     */
    public boolean modifierEvenement(evenement event) {
        Integer utilisateurId = utilisateurConnecteId();
        if (utilisateurId == null) {
            return false;
        }
        String sql = "UPDATE evenement SET date_debut = ?, date_fin = ?, titre = ?, description = ?, lieu = ? WHERE id_evenement = ? AND fk_id_utilisateur = ?";
        try (Connection conn = Connexion.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, versDateHeure(event.getDateDebut(), event.getHeure()));
            pstmt.setString(2, versDateHeure(event.getDateFin(), event.getHeureFin()));
            pstmt.setString(3, event.getNom());
            pstmt.setString(4, event.getDescription());
            pstmt.setString(5, event.getLieu());
            pstmt.setInt(6, event.getId());
            pstmt.setInt(7, utilisateurId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Supprime un événement par son id.
     * Retourne true si une ligne a bien été supprimée.
     */
    public boolean supprimerEvenement(int id) {
        Integer utilisateurId = utilisateurConnecteId();
        if (utilisateurId == null) {
            derniereErreur = "Aucun utilisateur connecté.";
            return false;
        }
        try (Connection conn = Connexion.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement recurrence = conn.prepareStatement(
                        "DELETE FROM recurrence WHERE fk_id_even = ?")) {
                    recurrence.setInt(1, id);
                    recurrence.executeUpdate();
                }
                int lignes;
                try (PreparedStatement evenement = conn.prepareStatement(
                        "DELETE FROM evenement WHERE id_evenement = ? AND fk_id_utilisateur = ?")) {
                    evenement.setInt(1, id);
                    evenement.setInt(2, utilisateurId);
                    lignes = evenement.executeUpdate();
                }
                conn.commit();
                return lignes > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            derniereErreur = "Erreur de suppression : " + e.getMessage();
            return false;
        }
    }

    private evenement mapResultSet(ResultSet rs) throws SQLException {
        Timestamp dateDebut = rs.getTimestamp("date_debut");
        Timestamp dateFin = rs.getTimestamp("date_fin");
        return new evenement(
            rs.getInt("id_evenement"),
            rs.getString("titre"),
            dateDebut.toLocalDateTime().toLocalDate().toString(),
            dateFin.toLocalDateTime().toLocalDate().toString(),
            dateDebut.toLocalDateTime().toLocalTime().toString(),
            dateFin.toLocalDateTime().toLocalTime().toString(),
            rs.getString("lieu"),
            rs.getString("description")
        );
    }

    private Integer utilisateurConnecteId() {
        return Session.estConnecte() ? Session.getUtilisateurConnecte().idUtilisateur : null;
    }

    private String versDateHeure(String date, String heure) {
        String heureComplete = heure == null || heure.isBlank() ? "00:00:00" : heure + ":00";
        return date + " " + heureComplete;
    }
}