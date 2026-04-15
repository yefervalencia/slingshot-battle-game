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

public class ControlsWindow {
    private StackPane root;
    private Scene scene;

    public ControlsWindow(Runnable onBack) {
        root = new StackPane();

        try {
            Image bg = new Image(getClass().getResourceAsStream("/assets/lobby_bg.png"));
            root.setBackground(new Background(new BackgroundImage(bg, BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                    new BackgroundSize(1280, 720, false, false, false, true))));
        } catch (Exception e) {
        }

        VBox container = new VBox(25);
        container.setAlignment(Pos.CENTER);
        container.setMaxSize(800, 500);
        container.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.75); -fx-background-radius: 20; -fx-border-color: #3498db; -fx-border-width: 2; -fx-border-radius: 20;");
        container.setPadding(new Insets(40));

        Label title = new Label("CONTROLES DE COMBATE");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        title.setTextFill(Color.web("#3498db"));

        String controls = "MOUSE: Moverse y Apuntar\n" +
                "CLICK IZQ: Cargar Potencia / Disparar\n" +
                "TECLA [Z]: Modo Sniper (Bala rápida)\n" +
                "TECLA [X]: Modo Artillería (Parabólica)\n" +
                "TECLA [C]: Modo Construcción (Poner Barrera)";

        Label info = new Label(controls);
        info.setFont(Font.font("Monospaced", FontWeight.BOLD, 24));
        info.setTextFill(Color.LIME);
        info.setAlignment(Pos.CENTER);

        container.getChildren().addAll(title, info);

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