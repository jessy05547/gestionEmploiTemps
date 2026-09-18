package com.jessy.models;
import org.mindrot.jbcrypt.BCrypt;

public class Utilisateur {
    public int idUtilisateur;
    public String nom;
    public String prenom;
    public String email;
    private String password;
    private String password_hash;

    public Utilisateur(String nom, String prenom, String email, String password) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
    }

    public final String getNom() {
        return this.nom;
    }
    public final String getPrenom() {
        return this.prenom;
    }
    public final String getEmail() {
        return this.email;
    }
    public final String getPassword() {
        return this.password;
    }

    public final String getPasswordHash() {
        return this.password_hash;
    }

    public final void setNom(String nom) {
        this.nom = nom;
    }
    public final void setPrenom(String prenom) {
        this.prenom = prenom;
    }
    public final void setEmail(String email) {
        this.email = email;
    }
    public final void setPassword(String password) {
        this.password_hash = BCrypt.hashpw(password, BCrypt.gensalt());
    }
    public final void setPasswordHash(String passwordHash) {
        this.password_hash = passwordHash;
    }
    public final void setIdUtilisateur(int id) {
        this.idUtilisateur = id;
    }
    public final void setPasswordHashExistant(String passwordHash) {
        this.password_hash = passwordHash;
    }
}

