package com.slingshot.ui;

import com.slingshot.core.GameEngine;
import com.slingshot.core.InputManager;
import com.slingshot.entities.Player;
import com.slingshot.entities.Projectile;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameWindow {
  private GameEngine engine;
  private boolean isHost;
  private String mapId, charId;

  private Canvas canvas;
  private InputManager inputManager;
  private final double WIDTH = 1280;
  private final double HEIGHT = 720;

  // Entidades
  private Image bgImage;
  private Player localPlayer;
  private List<Projectile> activeProjectiles = new ArrayList<>();

  // Controles de disparo
  private boolean canShoot = true;
  private String currentWeapon = "sniper"; // Por defecto Z

  public GameWindow(GameEngine engine, boolean isHost, String mapId, String charId) {
    this.engine = engine;
    this.isHost = isHost;
    this.mapId = mapId;
    this.charId = charId;
    this.inputManager = new InputManager();

    loadAssets();

    // Inicializar Jugador
    double startX = isHost ? 100 : WIDTH - 150;
    double startY = HEIGHT / 2;
    Image skin = null;
    try {
      skin = new Image(getClass().getResourceAsStream("/assets/" + charId + "_skin.png"));
    } catch (Exception e) {
    }
    this.localPlayer = new Player(startX, startY, skin);
  }

  private void loadAssets() {
    try {
      bgImage = new Image(getClass().getResourceAsStream("/assets/" + mapId + "_bg.png"));
    } catch (Exception e) {
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
    // 1. Selector de Armas
    if (inputManager.isKeyPressed("Z"))
      currentWeapon = "sniper";
    if (inputManager.isKeyPressed("X"))
      currentWeapon = "artillery";

    // 2. Límites de movilidad
    double minX = isHost ? 0 : WIDTH * 0.70;
    double maxX = isHost ? WIDTH * 0.30 : WIDTH;

    localPlayer.update(inputManager, minX, maxX, HEIGHT);

    // 3. Disparar (Click izquierdo)
    if (inputManager.isMousePressed() && canShoot && localPlayer.getAmmo() > 0) {
      shoot();
      canShoot = false; // Bloquea hasta soltar el click
    }
    if (!inputManager.isMousePressed()) {
      canShoot = true; // Permite disparar de nuevo
    }

    // 4. Actualizar Balas
    Iterator<Projectile> it = activeProjectiles.iterator();
    while (it.hasNext()) {
      Projectile p = it.next();
      p.update();
      if (!p.isAlive()) {
        it.remove(); // Borramos la bala si sale de la pantalla
      }
    }
  }

  private void shoot() {
    localPlayer.reduceAmmo();
    Projectile p = new Projectile(localPlayer.getCenterX(), localPlayer.getCenterY(), localPlayer.getAngle(),
        currentWeapon);
    activeProjectiles.add(p);

    // TODO: Fase 3 -> Enviar paquete UDP al oponente: "SHOOT;x;y;angulo;tipo"
    System.out.println("[Disparo] " + currentWeapon + " -> Munición restante: " + localPlayer.getAmmo());
  }

  private void render() {
    GraphicsContext gc = canvas.getGraphicsContext2D();

    // 1. Fondo y Zonas
    if (bgImage != null)
      gc.drawImage(bgImage, 0, 0, WIDTH, HEIGHT);
    else {
      gc.setFill(Color.web("#2c3e50"));
      gc.fillRect(0, 0, WIDTH, HEIGHT);
    }

    double limit30 = WIDTH * 0.30;
    double limit70 = WIDTH * 0.70;
    gc.setFill(Color.rgb(255, 0, 0, 0.15));
    if (isHost)
      gc.fillRect(limit30, 0, WIDTH - limit30, HEIGHT);
    else
      gc.fillRect(0, 0, limit70, HEIGHT);

    // 2. Entidades
    localPlayer.render(gc);
    for (Projectile p : activeProjectiles) {
      p.render(gc);
    }

    // 3. HUD (Interfaz de usuario rápida)
    gc.setFill(Color.WHITE);
    gc.fillText("Vidas: " + localPlayer.getLives(), 20, 30);
    gc.fillText("Munición: " + localPlayer.getAmmo(), 20, 50);
    gc.fillText("Arma (Z/X): " + currentWeapon.toUpperCase(), 20, 70);
  }
}