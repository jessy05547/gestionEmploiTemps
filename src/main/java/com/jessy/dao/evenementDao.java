package com.jessy.dao;
import com.jessy.models.evenement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.jessy.config.Connexion;

public class evenementDao {

    public void ajouterEvenement(evenement event) {
        String sql = "INSERT INTO evenement (nom, date_debut, date_fin, lieu, heure, description) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Connexion.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, event.getNom());
            pstmt.setString(2, event.getDateDebut());
            pstmt.setString(3, event.getDateFin());
            pstmt.setString(4, event.getLieu());
            pstmt.setString(5, event.getHeure());
            pstmt.setString(6, event.getDescription());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
