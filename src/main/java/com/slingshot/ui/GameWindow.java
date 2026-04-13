package com.slingshot.ui;

import com.slingshot.core.GameEngine;
import com.slingshot.core.InputManager;
import com.slingshot.entities.AmmoCrate;
import com.slingshot.entities.Barrier;
import com.slingshot.entities.Crate;
import com.slingshot.entities.DoubleScoreCrate;
import com.slingshot.entities.EmptyCrate;
import com.slingshot.entities.HealthCrate;
import com.slingshot.entities.IndestructibleCrate;
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

  private List<Barrier> activeBarriers = new ArrayList<>();
  private boolean isBuildingMode = false;
  private final int MAX_BARRIERS = 4; // Límite de construcción

  public interface OnProjectileExitListener {
    void onExit(String type, double y, double angle, double power);
  }

  private OnProjectileExitListener exitListener;

  public void setOnProjectileExitListener(OnProjectileExitListener listener) {
    this.exitListener = listener;
  }

  private List<Crate> crates = new ArrayList<>();

  // --- VARIABLES DE ESCENARIO DINÁMICO ---
  private long lastRegenTime = System.currentTimeMillis();
  private final long REGEN_COOLDOWN = 60000; // Cada 10 segundos intenta regenerar
  private final int MAX_CRATES = 25; // Límite máximo de cajas en el mapa

  // --- NUEVAS VARIABLES DE DISPARO ---
  private boolean isCharging = false;
  private double chargePower = 5.0; // Potencia base
  private final double MAX_POWER = 28.0; // Límite de potencia de artillería

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
    generateCrates();
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

    // 3. Sistema de Disparo (Click sostenido y soltar)
    if (inputManager.isMousePressed() && localPlayer.getAmmo() > 0) {
      isCharging = true;
      // Solo la artillería carga potencia. El francotirador dispara con potencia
      // fija.
      if (currentWeapon.equals("artillery")) {
        chargePower += 0.4; // Velocidad de carga (puedes ajustarla)
        if (chargePower > MAX_POWER)
          chargePower = MAX_POWER; // Tope máximo
      }
    } else if (isCharging) {
      // El jugador SOLTÓ el click izquierdo. ¡Fuego!
      shoot();
      isCharging = false;
      chargePower = 5.0; // Reseteamos la potencia para el siguiente tiro
    }

    // 4. Actualizar Balas y Colisiones
    Iterator<Projectile> it = activeProjectiles.iterator();
    while (it.hasNext()) {
      Projectile p = it.next();
      p.update();

      // Dentro del bucle de proyectiles...
      boolean projectileDestroyed = false;

      // COLISIÓN CON BARRERAS (Solo balas enemigas, no ignoradas por artillería)
      if (p.isEnemy()) {
        Iterator<Barrier> bIt = activeBarriers.iterator();
        while (bIt.hasNext()) {
          Barrier b = bIt.next();
          if (p.getX() > b.getX() && p.getX() < b.getX() + b.getSize() &&
              p.getY() > b.getY() && p.getY() < b.getY() + b.getSize()) {

            b.takeDamage(p.getType());
            projectileDestroyed = true;
            if (!b.isAlive())
              bIt.remove();
            break;
          }
        }
      }

      // 1. Colisión con Jugador (Solo balas enemigas)
      if (p.isEnemy() && localPlayer.checkHit(p.getX(), p.getY())) {
        localPlayer.takeDamage();
        projectileDestroyed = true;
      }

      // 2. Colisión con Cajas (SOLO SI ES ENEMIGA Y NO ES ARTILLERÍA)
      if (p.isEnemy() && !p.getType().equals("artillery")) {
        for (Crate c : crates) {
          if (c.isAlive() && p.getX() > c.getX() && p.getX() < c.getX() + c.getSize() &&
              p.getY() > c.getY() && p.getY() < c.getY() + c.getSize()) {

            // La caja decide si destruye la bala (polimorfismo)
            boolean destruyeBala = c.onHitByBullet(null, p);
            if (destruyeBala)
              projectileDestroyed = true;

            // Enviar recompensa al oponente que nos disparó
            enviarRecompensaRed(c);
            break;
          }
        }
      }

      // C) LÓGICA DE RELEVO (Handover a la otra pantalla)
      boolean exited = false;
      if (isHost && p.getX() > WIDTH)
        exited = true;
      if (!isHost && p.getX() < 0)
        exited = true;

      if (exited) {
        if (exitListener != null) {
          exitListener.onExit(p.getType(), p.getY(), p.getAngle(), p.getPower());
        }
        it.remove();
      } else if (!p.isAlive() || projectileDestroyed) {
        it.remove(); // Borrar si la bala "murió" o chocó contra algo
      }

    }

    // 5. Lógica de Regeneración Dinámica
    if (System.currentTimeMillis() - lastRegenTime > REGEN_COOLDOWN) {
      lastRegenTime = System.currentTimeMillis();
      regenerarMapa();
    }

    // 1. Selector de modo construcción
    if (inputManager.isKeyPressed("C")) {
      isBuildingMode = !isBuildingMode;
    }

    // 2. Lógica de posicionamiento (dentro de update)
    if (isBuildingMode && inputManager.isMousePressed() && activeBarriers.size() < MAX_BARRIERS) {
      double mx = inputManager.getMouseX();
      double my = inputManager.getMouseY();

      // Verificamos si está en su 70% (el área de batalla/roja)
      double limit30 = WIDTH * 0.30;
      double limit70 = WIDTH * 0.70;
      boolean inValidZone = isHost ? (mx > limit30) : (mx < limit70);

      if (inValidZone) {
        activeBarriers.add(new Barrier(mx - 22, my - 22));
        isBuildingMode = false; // Salimos del modo tras ponerla
        // canShoot = false; // Opcional: evitar disparo accidental
      }
    }
  }

  public void spawnRemoteProjectile(String type, double y, double angle, double power) {
    // Si soy Host, la bala entra por la derecha (X=1280).
    // Si soy Cliente, entra por la izquierda (X=0).
    double startX = isHost ? WIDTH : 0;

    // Creamos la bala con los datos recibidos
    Projectile p = new Projectile(startX, y, angle, type, power, isHost, true);
    activeProjectiles.add(p);
  }

  private void shoot() {
    localPlayer.reduceAmmo();
    // Cuando TÚ disparas, isEnemy es FALSE
    Projectile p = new Projectile(localPlayer.getCenterX(), localPlayer.getCenterY(), localPlayer.getAngle(),
        currentWeapon, chargePower, isHost, false);
    activeProjectiles.add(p);

    // TODO: Fase 3 -> Enviar paquete UDP al oponente: "SHOOT;x;y;angulo;tipo"
    System.out.println("[Disparo] " + currentWeapon + " -> Munición restante: " + localPlayer.getAmmo());
  }

  private void render() {
    double hudX = isHost ? 20 : (WIDTH * 0.70) + 20;
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
    gc.fillText("Vidas: " + localPlayer.getLives(), hudX, 30);
    gc.fillText("Munición: " + localPlayer.getAmmo(), hudX, 50);
    gc.fillText("Puntos: " + localPlayer.getScore(), hudX, 70);
    gc.fillText("Arma (Z/X): " + currentWeapon.toUpperCase(), hudX, 90);

    if (localPlayer.isDoubleScoreActive()) {
      gc.setFill(Color.PURPLE);
      gc.fillText("¡DOBLE PUNTUACIÓN! (" + localPlayer.getDoubleScoreTimeLeft() + "s)", hudX, 95);
    }

    // 4. Dibujar Línea de Apuntado (Láser)
    gc.setStroke(Color.rgb(255, 0, 0, 0.4));
    gc.setLineWidth(2);
    gc.strokeLine(localPlayer.getCenterX(), localPlayer.getCenterY(), inputManager.getMouseX(),
        inputManager.getMouseY());

    // 5. Dibujar Barra de Carga de Artillería sobre el jugador
    if (isCharging && currentWeapon.equals("artillery")) {
      double barWidth = 50;
      double chargePercent = chargePower / MAX_POWER;

      gc.setFill(Color.rgb(0, 0, 0, 0.5)); // Fondo barra
      gc.fillRect(localPlayer.getCenterX() - barWidth / 2, localPlayer.getCenterY() - 45, barWidth, 6);

      gc.setFill(Color.rgb(231, 76, 60)); // Relleno barra roja
      gc.fillRect(localPlayer.getCenterX() - barWidth / 2, localPlayer.getCenterY() - 45, barWidth * chargePercent, 6);
    }

    // DIBUJAR CAJAS
    for (Crate c : crates) {
      c.render(gc);
    }

    for (Barrier b : activeBarriers) {
      b.render(gc);
    }

    // Dibujar preview si estamos en modo construcción
    if (isBuildingMode) {
      gc.setGlobalAlpha(0.3);
      gc.setFill(Color.LIGHTBLUE);
      gc.fillRect(inputManager.getMouseX() - 22, inputManager.getMouseY() - 22, 45, 45);
      gc.setGlobalAlpha(1.0);
    }
  }

  private void generateCrates() {
    java.util.Random rand = new java.util.Random();
    double minX = isHost ? WIDTH * 0.30 : 0;
    double maxX = isHost ? WIDTH : WIDTH * 0.70;

    int creadas = 0;
    int intentos = 0;
    // Intentamos crear 15 cajas, pero con un límite de intentos para evitar bucles
    // infinitos
    while (creadas < 15 && intentos < 300) {
      intentos++;
      double cx = minX + rand.nextDouble() * (maxX - minX - 45);
      double cy = rand.nextDouble() * (HEIGHT - 45);

      // --- EVITAR SUPERPOSICIÓN ---
      boolean superpuesta = false;
      for (Crate existente : crates) {
        // Verificamos si la distancia es menor al tamaño de la caja (con margen)
        if (Math.abs(existente.getX() - cx) < 45 && Math.abs(existente.getY() - cy) < 45) {
          superpuesta = true;
          break;
        }
      }

      if (!superpuesta) {
        double prob = rand.nextDouble();
        if (prob < 0.20)
          crates.add(new IndestructibleCrate(cx, cy));
        else if (prob < 0.30)
          crates.add(new HealthCrate(cx, cy));
        else if (prob < 0.40)
          crates.add(new AmmoCrate(cx, cy));
        else if (prob < 0.50)
          crates.add(new DoubleScoreCrate(cx, cy));
        else
          crates.add(new EmptyCrate(cx, cy));
        creadas++;
      }
    }
  }

  // --- MÉTODO PARA RECIBIR RECOMPENSAS DE LA RED ---
  public void applyNetworkReward(String type, int amount) {
    if (localPlayer == null)
      return;

    if (type.equals("AMMO")) {
      localPlayer.addAmmo(amount);
      System.out.println("[RECOMPENSA] +5 Balas");
    } else if (type.equals("LIFE")) {
      localPlayer.addLife();
      System.out.println("[RECOMPENSA] +1 Vida");
    } else if (type.equals("DOUBLE")) {
      localPlayer.activateDoubleScore(amount);
      System.out.println("[RECOMPENSA] ¡Puntos Dobles x8 Segundos!");
    } else if (type.equals("SCORE")) {
      localPlayer.addScore(amount);
      System.out.println("[RECOMPENSA] +10 Puntos");
    }
  }

  private void enviarRecompensaRed(Crate c) {
    String msg = "";
    if (c instanceof AmmoCrate)
      msg = "REWARD;AMMO;5";
    else if (c instanceof HealthCrate)
      msg = "REWARD;LIFE;1";
    else if (c instanceof DoubleScoreCrate)
      msg = "REWARD;DOUBLE;8";
    else if (c instanceof EmptyCrate)
      msg = "REWARD;SCORE;10";

    if (!msg.isEmpty()) {
      engine.sendNetworkMessage(msg);
    }
  }

  private void regenerarMapa() {
    java.util.Random rand = new java.util.Random();
    double minX = isHost ? WIDTH * 0.30 : 0;
    double maxX = isHost ? WIDTH : WIDTH * 0.70;

    // A) Mover las cajas indestructibles existentes
    for (Crate c : crates) {
      if (c instanceof IndestructibleCrate) {
        double newX = minX + rand.nextDouble() * (maxX - minX - 45);
        double newY = rand.nextDouble() * (HEIGHT - 45);
        // Necesitas crear un setPos en Crate o acceder a x/y si son protected
        c.setX(newX);
        c.setY(newY);
      }
    }

    // B) Si hay pocas cajas, crear nuevas (Solo si no excede el límite)
    if (crates.size() < MAX_CRATES) {
      int cuantasNuevas = 3; // Añadimos de a 3 por ciclo
      for (int i = 0; i < cuantasNuevas; i++) {
        if (crates.size() >= MAX_CRATES)
          break;

        double cx = minX + rand.nextDouble() * (maxX - minX - 45);
        double cy = rand.nextDouble() * (HEIGHT - 45);

        // Reutilizamos la lógica de colisión para que no nazcan una encima de otra
        boolean superpuesta = false;
        for (Crate existente : crates) {
          if (Math.abs(existente.getX() - cx) < 45 && Math.abs(existente.getY() - cy) < 45) {
            superpuesta = true;
            break;
          }
        }

        if (!superpuesta) {
          double prob = rand.nextDouble();
          // IMPORTANTE: Aquí solo creamos cajas DESTRUCTIBLES (porque las indestructibles
          // son fijas)
          if (prob < 0.15)
            crates.add(new HealthCrate(cx, cy));
          else if (prob < 0.30)
            crates.add(new AmmoCrate(cx, cy));
          else if (prob < 0.45)
            crates.add(new DoubleScoreCrate(cx, cy));
          else
            crates.add(new EmptyCrate(cx, cy));
        }
      }
    }
  }
}