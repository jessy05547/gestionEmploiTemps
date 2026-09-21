package com.jessy.dao;

import com.jessy.config.Connexion;
import com.jessy.models.Utilisateur;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.mindrot.jbcrypt.BCrypt;

public class UserDao {

    public void modifierProfil(Utilisateur utilisateur) throws SQLException {
        String sql = "UPDATE utilisateur SET nom = ?, prenom = ?, email = ? WHERE id_utilisateur = ?";
        try (Connection conn = Connexion.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, utilisateur.getNom());
            stmt.setString(2, utilisateur.getPrenom());
            stmt.setString(3, utilisateur.getEmail());
            stmt.setInt(4, utilisateur.idUtilisateur);
            stmt.executeUpdate();
        }
    }

    public boolean verifierMotDePasse(Utilisateur utilisateur, String motDePasse) {
        return utilisateur != null && motDePasse != null && !motDePasse.isBlank()
                && BCrypt.checkpw(motDePasse, utilisateur.getPasswordHash());
    }

    public void modifierMotDePasse(Utilisateur utilisateur, String motDePasse) throws SQLException {
        String sql = "UPDATE utilisateur SET password_hash = ? WHERE id_utilisateur = ?";
        try (Connection conn = Connexion.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, BCrypt.hashpw(motDePasse, BCrypt.gensalt()));
            stmt.setInt(2, utilisateur.idUtilisateur);
            stmt.executeUpdate();
        }
    }

    public void supprimerCompte(int utilisateurId) throws SQLException {
        try (Connection conn = Connexion.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "DELETE r FROM recurrence r INNER JOIN evenement e ON e.id_evenement = r.fk_id_even "
                                + "WHERE e.fk_id_utilisateur = ?")) {
                    stmt.setInt(1, utilisateurId);
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM evenement WHERE fk_id_utilisateur = ?")) {
                    stmt.setInt(1, utilisateurId);
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM utilisateur WHERE id_utilisateur = ?")) {
                    stmt.setInt(1, utilisateurId);
                    stmt.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Insère un nouvel utilisateur en base.
     * Le mot de passe doit déjà être haché (BCrypt) AVANT d'appeler cette méthode —
     * le DAO ne s'occupe jamais du hachage, seulement du stockage.
     */
    public void inserer(Utilisateur utilisateur) throws SQLException {
        String sql = "INSERT INTO utilisateur (nom, prenom, email, password_hash) VALUES (?, ?, ?, ?)";

        try (Connection conn = Connexion.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, utilisateur.getNom());
            stmt.setString(2, utilisateur.getPrenom());
            stmt.setString(3, utilisateur.getEmail());
            stmt.setString(4, utilisateur.getPasswordHash());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    utilisateur.setIdUtilisateur(generatedKeys.getInt(1));
                }
            }
        }
    }

    /**
     * Vérifie si un email est déjà utilisé — utile avant l'inscription
     * pour éviter une erreur de contrainte UNIQUE brute et donner un message clair.
     */
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM utilisateur WHERE email = ?";

        try (Connection conn = Connexion.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // true si au moins une ligne trouvée
            }
        }
    }

    /**
     * Récupère un utilisateur par son email — utile pour le login.
     */
    public Utilisateur trouverParEmail(String email) throws SQLException {
    String sql = "SELECT * FROM utilisateur WHERE email = ?";

    try (Connection conn = Connexion.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, email);

        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                Utilisateur u = new Utilisateur(
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("email"),
                    ""
                );
                u.setIdUtilisateur(rs.getInt("id_utilisateur"));

                String hash = rs.getString("password_hash"); // nom EXACT confirmé par ta table
                System.out.println("Hash récupéré depuis la base : " + hash); // debug temporaire

                u.setPasswordHashExistant(hash);
                return u;
            }
        }
    }
    return null;
    }
}