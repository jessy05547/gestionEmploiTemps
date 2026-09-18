package com.jessy.config;

import com.jessy.config.Connexion;

import java.sql.Connection;

public class TestConnection {

    public static void main(String[] args) {

        try {

            Connection connection =
                    Connexion.getConnection();

            System.out.println(
                    "Connexion réussie à MySQL !"
            );

            connection.close();

        } catch (Exception e) {

            System.out.println(
                    "Erreur de connexion : "
                    + e.getMessage()
            );
        }
    }
}