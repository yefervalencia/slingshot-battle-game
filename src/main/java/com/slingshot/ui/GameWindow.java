package com.slingshot.ui;

import com.slingshot.core.GameEngine;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class GameWindow {
    private GameEngine engine;
    private Canvas canvas;

    public GameWindow(GameEngine engine) {
        this.engine = engine;
    }

    public Scene createScene() {
        Pane root = new Pane();
        // Resolución estándar del campo de batalla
        canvas = new Canvas(1000, 600); 
        root.getChildren().add(canvas);

        // BUCLE DE JUEGO (GAME LOOP) - Se ejecuta 60 veces por segundo
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                render();
            }
        };
        timer.start();

        return new Scene(root, 1000, 600);
    }

    private void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // 1. Limpiar pantalla en cada frame (Cielo)
        gc.setFill(Color.web("#87CEEB")); // Azul cielo
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // 2. Dibujar el piso (Tierra)
        gc.setFill(Color.web("#2E8B57")); // Verde oscuro
        gc.fillRect(0, 500, canvas.getWidth(), 100);

        // 3. Dibujar Zonas Asimétricas (Visualización temporal para guiarnos)
        // Zona Izquierda (PC1) - 25% del mapa (250px)
        gc.setFill(Color.rgb(255, 0, 0, 0.2)); // Rojo semitransparente
        gc.fillRect(0, 0, 250, 600);

        // Zona Derecha (PC2) - 25% del mapa (250px)
        gc.setFill(Color.rgb(0, 0, 255, 0.2)); // Azul semitransparente
        gc.fillRect(750, 0, 250, 600);

        // TODO: Aquí más adelante le pediremos al GameEngine que dibuje a los Jugadores, Cajas y Balas.
        gc.setFill(Color.BLACK);
        gc.fillText("ZONA PC1", 100, 50);
        gc.fillText("ZONA BATALLA (CAJAS)", 450, 50);
        gc.fillText("ZONA PC2", 850, 50);
    }
}