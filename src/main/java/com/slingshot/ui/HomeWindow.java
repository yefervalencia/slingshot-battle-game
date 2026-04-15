package com.slingshot.ui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class HomeWindow {
    private VBox root;
    private Scene scene;

    public HomeWindow(Runnable onPlay, Runnable onRules, Runnable onControls) {
        root = new VBox(30); // Aumenté un poco la separación a 30
        root.setAlignment(Pos.CENTER);

        // Estilo de fondo
        try {
            Image bg = new Image(getClass().getResourceAsStream("/assets/lobby_bg.png"));
            root.setBackground(new Background(new BackgroundImage(bg, BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                    new BackgroundSize(1280, 720, false, false, false, true))));
        } catch (Exception e) {
        }

        // --- ESTILO DEL TÍTULO ---
        Text title = new Text("SLINGSHOT BATTLE");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 80)); // Letra más grande
        title.setFill(Color.WHITE);

        // Borde negro a la letra
        title.setStroke(Color.BLACK);
        title.setStrokeWidth(3);

        // Sombreado del título
        DropShadow textShadow = new DropShadow();
        textShadow.setColor(Color.color(0, 0, 0, 0.8));
        textShadow.setRadius(15);
        textShadow.setSpread(0.6); // Hace la sombra más sólida
        textShadow.setOffsetY(6); // Desplaza la sombra hacia abajo
        title.setEffect(textShadow);

        // --- CREACIÓN DE BOTONES ---
        Button btnPlay = UIFactory.createMenuButton("JUGAR", "#2ecc71", onPlay);
        Button btnRules = UIFactory.createMenuButton("REGLAS", "#2ecc71", onRules);
        Button btnControls = UIFactory.createMenuButton("CONTROLES", "#2ecc71", onControls);

        btnPlay.setOnAction(e -> onPlay.run());
        btnRules.setOnAction(e -> onRules.run());
        btnControls.setOnAction(e -> onControls.run());

        root.getChildren().addAll(title, btnPlay, btnRules, btnControls);
        scene = new Scene(root, 1280, 720);
    }

    public Scene getScene() {
        return scene;
    }
}