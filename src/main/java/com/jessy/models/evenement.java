package com.jessy.models;

public class evenement {
    private int id;
    private String nom;
    private String dateDebut;
    private String dateFin;
    private String heure;
    private String heureFin;
    private String lieu;
    private String description;

    public evenement(String nom, String dateDebut, String dateFin, String heure, String lieu, String description) {
        this.nom = nom;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.heure = heure;
        this.heureFin = heure;
        this.lieu = lieu;
        this.description = description;
    }

    public evenement(int id, String nom, String dateDebut, String dateFin, String heure,
                     String heureFin, String lieu, String description) {
        this(nom, dateDebut, dateFin, heure, lieu, description);
        this.id = id;
        this.heureFin = heureFin;
    }

    // Utilisé quand l'événement provient de la base (id déjà connu),
    // ou pour préparer une modification/suppression ciblée.
    public evenement(int id, String nom, String dateDebut, String dateFin, String heure, String lieu, String description) {
        this(nom, dateDebut, dateFin, heure, lieu, description);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDateDebut() {
        return dateDebut;
    }

    public String getDateFin() {
        return dateFin;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public void setDateDebut(String dateDebut) {
        this.dateDebut = dateDebut;
    }

    public void setDateFin(String dateFin) {
        this.dateFin = dateFin;
    }

    public String getHeure() {
        return heure;
    }

    public void setHeure(String heure) {
        this.heure = heure;
    }

    public String getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(String heureFin) {
        this.heureFin = heureFin;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}