package com.slingshot.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class RulesWindow {
    private StackPane root;
    private Scene scene;

    public RulesWindow(Runnable onBack) {
        root = new StackPane();

        // Fondo de imagen
        try {
            Image bg = new Image(getClass().getResourceAsStream("/assets/lobby_bg.png"));
            root.setBackground(new Background(new BackgroundImage(bg, BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                    new BackgroundSize(1280, 720, false, false, false, true))));
        } catch (Exception e) {
        }

        // Capa oscura central
        VBox container = new VBox(20);
        container.setAlignment(Pos.CENTER);
        container.setMaxSize(900, 600);
        container.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.75); -fx-background-radius: 20; -fx-border-color: #2ecc71; -fx-border-width: 2; -fx-border-radius: 20;");
        container.setPadding(new Insets(30));

        Label title = new Label("REGLAS Y PUNTUACIÓN");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        title.setTextFill(Color.web("#2ecc71"));

        String textRules = "• VICTORIA: Gana quien tenga más puntos cuando alguien agote sus vidas.\n\n" +
                "• IMPACTO DIRECTO: +50 puntos al acertar al rival.\n" +
                "• CAJA MADERA: +10 puntos.\n" +
                "• CAJA MÉDICA (Verde): +1 vida.\n" +
                "• CAJA MUNICIÓN (Amarilla): +5 balas.\n" +
                "• CAJA ÉPICA (Morada): Puntos dobles x8 segundos.\n" +
                "• BLOQUE METÁLICO: Indestructible y hace rebotar disparos sniper.";

        Label content = new Label(textRules);
        content.setFont(Font.font("Arial", 22));
        content.setTextFill(Color.WHITE);
        content.setWrapText(true);

        container.getChildren().addAll(title, content);

        // Botón Circular Unificado
        Button btnBack = UIFactory.createBackButton(onBack);
        StackPane.setAlignment(btnBack, Pos.TOP_LEFT);
        StackPane.setMargin(btnBack, new Insets(20));

        // IMPORTANTE: container primero, btnBack de último para que quede encima
        root.getChildren().addAll(container, btnBack);
        scene = new Scene(root, 1280, 720);
    }


    public Scene getScene() {
        return scene;
    }
}