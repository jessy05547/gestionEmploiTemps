package com.jessy.controller;

import com.jessy.models.Utilisateur;

public class Session {

    private static Utilisateur utilisateurConnecte;

    private Session() {
    }

    public static void connecter(Utilisateur utilisateur) {
        utilisateurConnecte = utilisateur;
    }

    public static Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    public static boolean estConnecte() {
        return utilisateurConnecte != null;
    }

    public static void deconnecter() {
        utilisateurConnecte = null;
    }
}