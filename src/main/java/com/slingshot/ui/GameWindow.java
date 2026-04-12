package com.slingshot.ui;

import com.slingshot.core.GameEngine;
import com.slingshot.core.InputManager;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class GameWindow {
  private GameEngine engine;
  private boolean isHost;
  private Canvas canvas;
  private InputManager inputManager;

  // Constantes del mapa
  private final double WIDTH = 1280;
  private final double HEIGHT = 720;

  // Posición del jugador (Temporal, luego vivirá en Player.java)
  private double playerX;
  private double playerY = HEIGHT / 2;
  private final double PLAYER_SPEED = 5.0;
  private final double PLAYER_SIZE = 40.0;

  public GameWindow(GameEngine engine, boolean isHost) {
    this.engine = engine;
    this.isHost = isHost;
    this.inputManager = new InputManager();

    // Spawn inicial dependiendo del rol
    this.playerX = isHost ? 100 : WIDTH - 100;
  }

  public Scene createScene() {
    Pane root = new Pane();
    canvas = new Canvas(WIDTH, HEIGHT);
    root.getChildren().add(canvas);

    Scene scene = new Scene(root, WIDTH, HEIGHT);
    inputManager.attachToScene(scene);

    // GAME LOOP - 60 FPS
    AnimationTimer timer = new AnimationTimer() {
      @Override
      public void handle(long now) {
        update(); // 1. Matemáticas y lógicas
        render(); // 2. Dibujar en pantalla
      }
    };
    timer.start();

    return scene;
  }

  private void update() {
    // --- MOVIMIENTO DEL JUGADOR ---
    if (inputManager.isKeyPressed("W"))
      playerY -= PLAYER_SPEED;
    if (inputManager.isKeyPressed("S"))
      playerY += PLAYER_SPEED;
    if (inputManager.isKeyPressed("A"))
      playerX -= PLAYER_SPEED;
    if (inputManager.isKeyPressed("D"))
      playerX += PLAYER_SPEED;

    // --- COLISIONES CON EL MAPA (LÍMITES) ---
    // Límite Y (Piso y Techo)
    if (playerY < 0)
      playerY = 0;
    if (playerY > HEIGHT - PLAYER_SIZE)
      playerY = HEIGHT - PLAYER_SIZE;

    // Límite X (El muro invisible del 30%)
    double limit30Percent = WIDTH * 0.30; // 384 px
    double limit70Percent = WIDTH * 0.70; // 896 px

    if (isHost) {
      // El Host no puede pasar del 30% hacia la derecha
      if (playerX < 0)
        playerX = 0;
      if (playerX > limit30Percent - PLAYER_SIZE)
        playerX = limit30Percent - PLAYER_SIZE;
    } else {
      // El Cliente no puede pasar del 70% hacia la izquierda
      if (playerX < limit70Percent)
        playerX = limit70Percent;
      if (playerX > WIDTH - PLAYER_SIZE)
        playerX = WIDTH - PLAYER_SIZE;
    }
  }

  private void render() {
    GraphicsContext gc = canvas.getGraphicsContext2D();

    // 1. Dibujar Cielo/Fondo
    gc.setFill(Color.web("#2c3e50"));
    gc.fillRect(0, 0, WIDTH, HEIGHT);

    // 2. Dibujar Guías Visuales de Zonas (Solo para desarrollo)
    double limit30 = WIDTH * 0.30;
    double limit70 = WIDTH * 0.70;

    if (isHost) {
      gc.setFill(Color.rgb(46, 204, 113, 0.2)); // Verde (Mi zona 30%)
      gc.fillRect(0, 0, limit30, HEIGHT);
      gc.setFill(Color.rgb(231, 76, 60, 0.1)); // Rojo suave (Zona cajas 70%)
      gc.fillRect(limit30, 0, WIDTH - limit30, HEIGHT);
    } else {
      gc.setFill(Color.rgb(231, 76, 60, 0.1)); // Rojo suave (Zona cajas 70%)
      gc.fillRect(0, 0, limit70, HEIGHT);
      gc.setFill(Color.rgb(46, 204, 113, 0.2)); // Verde (Mi zona 30%)
      gc.fillRect(limit70, 0, WIDTH - limit70, HEIGHT);
    }

    // 3. Dibujar al Jugador Local
    gc.setFill(isHost ? Color.BLUE : Color.ORANGE);
    gc.fillRect(playerX, playerY, PLAYER_SIZE, PLAYER_SIZE);

    // Etiqueta sobre el jugador
    gc.setFill(Color.WHITE);
    gc.fillText(isHost ? "HOST" : "CLIENTE", playerX, playerY - 10);
  }
}