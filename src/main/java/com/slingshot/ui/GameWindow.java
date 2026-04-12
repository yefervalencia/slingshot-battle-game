package com.slingshot.ui;

import com.slingshot.core.GameEngine;
import com.slingshot.core.InputManager;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class GameWindow {
    private GameEngine engine;
    private boolean isHost;
    private String mapId;
    private String charId;
    
    private Canvas canvas;
    private InputManager inputManager;

    private final double WIDTH = 1280;
    private final double HEIGHT = 720;
    
    private double playerX;
    private double playerY = HEIGHT / 2;
    private final double PLAYER_SPEED = 5.0;
    private final double PLAYER_SIZE = 60.0; // Lo hicimos más grande para que se vea el skin

    // Variables para las imágenes
    private Image bgImage;
    private Image playerSkin;

    public GameWindow(GameEngine engine, boolean isHost, String mapId, String charId) {
        this.engine = engine;
        this.isHost = isHost;
        this.mapId = mapId;
        this.charId = charId;
        this.inputManager = new InputManager();
        this.playerX = isHost ? 100 : WIDTH - 150;

        loadAssets();
    }

    private void loadAssets() {
        try {
            // Intenta cargar el fondo del mapa (Ej: /assets/desert_bg.png)
            bgImage = new Image(getClass().getResourceAsStream("/assets/" + mapId + "_bg.png"));
        } catch (Exception e) {
            System.err.println("Aviso: Fondo " + mapId + " no encontrado.");
        }

        try {
            // Intenta cargar el skin del personaje (Ej: /assets/sniper_skin.png)
            playerSkin = new Image(getClass().getResourceAsStream("/assets/" + charId + "_skin.png"));
        } catch (Exception e) {
            System.err.println("Aviso: Skin " + charId + " no encontrado.");
        }
    }

    public Scene createScene() {
        Pane root = new Pane();
        canvas = new Canvas(WIDTH, HEIGHT);
        root.getChildren().add(canvas);

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        inputManager.attachToScene(scene);

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render();
            }
        };
        timer.start();

        return scene;
    }

    private void update() {
        if (inputManager.isKeyPressed("W")) playerY -= PLAYER_SPEED;
        if (inputManager.isKeyPressed("S")) playerY += PLAYER_SPEED;
        if (inputManager.isKeyPressed("A")) playerX -= PLAYER_SPEED;
        if (inputManager.isKeyPressed("D")) playerX += PLAYER_SPEED;

        if (playerY < 0) playerY = 0;
        if (playerY > HEIGHT - PLAYER_SIZE) playerY = HEIGHT - PLAYER_SIZE;

        double limit30Percent = WIDTH * 0.30;
        double limit70Percent = WIDTH * 0.70;

        if (isHost) {
            if (playerX < 0) playerX = 0;
            if (playerX > limit30Percent - PLAYER_SIZE) playerX = limit30Percent - PLAYER_SIZE;
        } else {
            if (playerX < limit70Percent) playerX = limit70Percent;
            if (playerX > WIDTH - PLAYER_SIZE) playerX = WIDTH - PLAYER_SIZE;
        }
    }

    private void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // 1. Dibujar Fondo (Imagen o Color de respaldo)
        if (bgImage != null) {
            gc.drawImage(bgImage, 0, 0, WIDTH, HEIGHT);
        } else {
            gc.setFill(Color.web("#2c3e50")); 
            gc.fillRect(0, 0, WIDTH, HEIGHT);
        }

        // 2. Filtros de Zonas (Para saber dónde estamos)
        double limit30 = WIDTH * 0.30;
        double limit70 = WIDTH * 0.70;
        if (isHost) {
            gc.setFill(Color.rgb(255, 0, 0, 0.15)); // Zona enemiga
            gc.fillRect(limit30, 0, WIDTH - limit30, HEIGHT);
        } else {
            gc.setFill(Color.rgb(255, 0, 0, 0.15)); // Zona enemiga
            gc.fillRect(0, 0, limit70, HEIGHT);
        }

        // 3. Dibujar Jugador (Skin o Cuadrado de respaldo)
        if (playerSkin != null) {
            // IMPORTANTE: Si es el cliente (que está a la derecha), miramos hacia la izquierda
            if (!isHost) {
                // Truco para voltear la imagen horizontalmente
                gc.save();
                gc.translate(playerX + PLAYER_SIZE, playerY);
                gc.scale(-1, 1);
                gc.drawImage(playerSkin, 0, 0, PLAYER_SIZE, PLAYER_SIZE);
                gc.restore();
            } else {
                gc.drawImage(playerSkin, playerX, playerY, PLAYER_SIZE, PLAYER_SIZE);
            }
        } else {
            gc.setFill(isHost ? Color.BLUE : Color.ORANGE);
            gc.fillRect(playerX, playerY, PLAYER_SIZE, PLAYER_SIZE);
        }
        
        // Etiqueta del jugador
        gc.setFill(Color.WHITE);
        gc.fillText(charId.toUpperCase(), playerX, playerY - 5);
    }
}