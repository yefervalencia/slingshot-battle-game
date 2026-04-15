package com.slingshot.ui;

import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

public class UIFactory {

    // Estilo base compartido (Borde negro + Sombra)
    private static final String COMMON_STYLE = 
        "-fx-border-color: black; " +
        "-fx-border-width: 3; " +
        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 10, 0.5, 0, 5); " +
        "-fx-cursor: hand; ";

    /**
     * Crea un botón rectangular para los menús principales (Jugar, Reglas, etc.)
     */
    public static Button createMenuButton(String text, String hexColor, Runnable action) {
        Button b = new Button(text);
        b.setPrefWidth(280);
        b.setPrefHeight(60);

        String style = 
            "-fx-background-color: " + hexColor + "; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 22px; " +
            "-fx-border-radius: 12; " +
            "-fx-background-radius: 12; " + COMMON_STYLE;

        String hoverStyle = style.replace(hexColor, darken(hexColor));

        b.setStyle(style);
        b.setOnMouseEntered(e -> b.setStyle(hoverStyle));
        b.setOnMouseExited(e -> b.setStyle(style));
        b.setOnAction(e -> action.run());

        return b;
    }

    /**
     * Crea el botón circular de "Volver" con flecha
     */
    public static Button createBackButton(Runnable action) {
        Button b = new Button("←");
        String color = "#e74c3c"; // Rojo

        String style = 
            "-fx-background-color: " + color + "; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 32px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 50em; " +
            "-fx-border-radius: 50em; " + 
            "-fx-min-width: 65px; -fx-min-height: 65px; " +
            "-fx-max-width: 65px; -fx-max-height: 65px; " +
            "-fx-padding: 0 0 5 0; " + 
            "-fx-alignment: center; " + COMMON_STYLE;

        String hoverStyle = style.replace(color, "#c0392b");

        b.setStyle(style);
        b.setOnMouseEntered(e -> b.setStyle(hoverStyle));
        b.setOnMouseExited(e -> b.setStyle(style));
        b.setOnAction(e -> action.run());

        return b;
    }

    // Pequeño truco para oscurecer el color en el hover
    private static String darken(String hex) {
        if (hex.equals("#2ecc71")) return "#27ae60"; // Verde
        if (hex.equals("#3498db")) return "#2980b9"; // Azul
        return hex;
    }
}