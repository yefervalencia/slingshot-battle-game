package com.slingshot.ui;

import com.slingshot.core.GameEngine;
import com.slingshot.core.InputManager;
import com.slingshot.entities.AmmoCrate;
import com.slingshot.entities.Barrier;
import com.slingshot.entities.Crate;
import com.slingshot.entities.DoubleScoreCrate;
import com.slingshot.entities.EmptyCrate;
import com.slingshot.entities.FloatingIndicator;
import com.slingshot.entities.HealthCrate;
import com.slingshot.entities.IndestructibleCrate;
import com.slingshot.entities.Player;
import com.slingshot.entities.Projectile;
import com.slingshot.network.NetworkProtocol;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameWindow {

  // --- Callback para volver al inicio ---
  private Runnable onExitToHome;

  public void setOnExitToHome(Runnable onExitToHome) {
    this.onExitToHome = onExitToHome;
  }

  private List<FloatingIndicator> floatingIndicators = new ArrayList<>();
  private Image healthIcon, ammoIcon, doubleIcon;

  // ¡CORRECCIÓN! Usamos esta variable global para la ventana completa
  private StackPane rootLayout;
  private VBox pauseMenuContainer;

  private double oppX = 0, oppY = 0; // Posición cruda del rival
  private long lastPosSend = 0; // Control de frecuencia de envío

  private boolean isGameOver = false;
  private boolean endMenuShown = false; // Evita que el menú final se dibuje 60 veces por segundo
  private String finalStatus = "";
  private int opponentScore = -1; // -1 significa que aún no recibimos el dato

  private long lastAmmoRegen = System.currentTimeMillis();
  private final long AMMO_COOLDOWN = 5000; // 5 segundos para regenerar 1 bala

  private List<Barrier> activeBarriers = new ArrayList<>();
  private boolean isBuildingMode = false;
  private final int MAX_BARRIERS = 4; // Límite de construcción
  private boolean canPlaceBarrier = true;

  public interface OnProjectileExitListener {
    void onExit(String type, double y, double angle, double power);
  }

  private OnProjectileExitListener exitListener;

  public void setOnProjectileExitListener(OnProjectileExitListener listener) {
    this.exitListener = listener;
  }

  private List<Crate> crates = new ArrayList<>();

  private AnimationTimer gameLoop;

  private int gameTimeSeconds = 600; // 10 minutos
  private long lastTimerUpdate = System.currentTimeMillis();
  private boolean isPaused = false;
  private double opponentScoreDisplay = 0; // Para mostrar puntaje rival en tiempo real

  // --- VARIABLES DE ESCENARIO DINÁMICO ---
  private long lastRegenTime = System.currentTimeMillis();
  private final long REGEN_COOLDOWN = 60000; // Cada 60 segundos intenta regenerar
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
    try {
      healthIcon = new Image(getClass().getResourceAsStream("/assets/icon_health.png"));
    } catch (Exception e) {
    }
    try {
      ammoIcon = new Image(getClass().getResourceAsStream("/assets/icon_ammo.png"));
    } catch (Exception e) {
    }
    try {
      doubleIcon = new Image(getClass().getResourceAsStream("/assets/icon_double.png"));
    } catch (Exception e) {
    }
  }

  public Scene createScene() {
    // ¡CORRECCIÓN! Instanciamos la variable global
    rootLayout = new StackPane();
    canvas = new Canvas(WIDTH, HEIGHT);

    // Botón Pausa
    Button btnPause = new Button("II");
    btnPause.setStyle(
        "-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20px; -fx-background-radius: 50em; -fx-min-width: 50px; -fx-min-height: 50px; -fx-cursor: hand; -fx-padding: 0;");
    btnPause.setOnMouseEntered(e -> btnPause.setStyle(btnPause.getStyle().replace("#f39c12", "#e67e22")));
    btnPause.setOnMouseExited(e -> btnPause.setStyle(btnPause.getStyle().replace("#e67e22", "#f39c12")));
    btnPause.setOnAction(e -> showPauseMenu());

    // ¡CORRECCIÓN! Esquina opuesta: Si es Host (Izq), botón a la Derecha. Si es
    // Cliente (Der), botón a la Izq.
    StackPane.setAlignment(btnPause, isHost ? Pos.TOP_RIGHT : Pos.TOP_LEFT);
    StackPane.setMargin(btnPause, new Insets(20));

    rootLayout.getChildren().addAll(canvas, btnPause);

    Scene scene = new Scene(rootLayout, WIDTH, HEIGHT);
    inputManager.attachToScene(scene);

    gameLoop = new AnimationTimer() {
      @Override
      public void handle(long now) {
        update();
        render();
      }
    };
    gameLoop.start();

    return scene;
  }

  private void showPauseMenu() {
    if (isPaused || isGameOver)
      return;
    isPaused = true;

    pauseMenuContainer = new VBox(20);
    pauseMenuContainer.setAlignment(Pos.CENTER);
    pauseMenuContainer.setMaxSize(400, 500);
    pauseMenuContainer.setStyle(
        "-fx-background-color: rgba(0, 0, 0, 0.85); -fx-background-radius: 20; -fx-border-color: #f39c12; -fx-border-width: 3; -fx-border-radius: 20;");
    pauseMenuContainer.setPadding(new Insets(30));

    Label lblTitle = new Label("JUEGO EN PAUSA");
    lblTitle.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #f39c12;");

    Label lblWarning = new Label("¡El rival sigue jugando, date prisa!");
    lblWarning.setStyle("-fx-font-size: 16px; -fx-text-fill: #e74c3c; -fx-font-style: italic;");

    Button btnResume = UIFactory.createMenuButton("CONTINUAR", "#2ecc71", () -> {
      isPaused = false;
      rootLayout.getChildren().remove(pauseMenuContainer);
    });

    Button btnControls = UIFactory.createMenuButton("CONTROLES", "#3498db", () -> {
      CustomAlert.show("Controles de Combate",
          "Z: Arma Sniper\nX: Arma Artillería\nC: Colocar Barrera\nMouse: Apuntar y Disparar", null);
    });

    Button btnPoints = UIFactory.createMenuButton("PUNTUACIÓN", "#9b59b6", () -> {
      CustomAlert.show("Estado de la Partida",
          "Tus Puntos: " + localPlayer.getScore() + "\nPuntos del Rival: " + opponentScoreDisplay, null);
    });

    Button btnQuit = UIFactory.createMenuButton("ABANDONAR", "#e74c3c", () -> {
      // 1. Avisar a la red
      engine.sendNetworkMessage("PLAYER_QUIT_MATCH");

      // 2. Detener el bucle del juego para que no consuma memoria en el menú
      if (gameLoop != null)
        gameLoop.stop();

      // 3. Volver al Home usando el callback
      if (onExitToHome != null)
        onExitToHome.run();
    });

    pauseMenuContainer.getChildren().addAll(lblTitle, lblWarning, new Label(""), btnResume, btnControls, btnPoints,
        btnQuit);
    rootLayout.getChildren().add(pauseMenuContainer);
  }

  private void update() {
    if (isGameOver) {
      if (!endMenuShown)
        showEndGameMenu();
      return;
    }

    // ¡ELIMINADO: if (isPaused) return;!
    // Ahora el tiempo y los enemigos siguen actualizándose de fondo.

    // 1. EL TIEMPO NUNCA SE DETIENE
    if (System.currentTimeMillis() - lastTimerUpdate > 1000) {
      gameTimeSeconds--;
      lastTimerUpdate = System.currentTimeMillis();
      if (gameTimeSeconds <= 0)
        finalizarPartida();
    }

    // 2. REGENERACIÓN DE MUNICIÓN NUNCA SE DETIENE
    if (localPlayer.getAmmo() < 1 && System.currentTimeMillis() - lastAmmoRegen > AMMO_COOLDOWN) {
      localPlayer.addAmmo(1);
      lastAmmoRegen = System.currentTimeMillis();
    }

    // ==========================================
    // 3. ESTO SÍ SE PAUSA (CONTROLES DEL JUGADOR LOCAL)
    // ==========================================
    if (!isPaused) {
      // Selector de Armas
      if (inputManager.isKeyPressed("Z")) {
        currentWeapon = "sniper";
        isBuildingMode = false;
      }
      if (inputManager.isKeyPressed("X")) {
        currentWeapon = "artillery";
        isBuildingMode = false;
      }
      if (inputManager.isKeyPressed("C")) {
        isBuildingMode = true;
      }

      double minX = isHost ? 0 : WIDTH * 0.70;
      double maxX = isHost ? WIDTH * 0.30 : WIDTH;
      localPlayer.update(inputManager, minX, maxX, HEIGHT);

      if (!isBuildingMode) {
        if (inputManager.isMousePressed() && localPlayer.getAmmo() > 0) {
          isCharging = true;
          if (currentWeapon.equals("artillery")) {
            chargePower += 0.4;
            if (chargePower > MAX_POWER)
              chargePower = MAX_POWER;
          }
        } else if (isCharging) {
          shoot();
          isCharging = false;
          chargePower = 5.0;
        }
      } else {
        if (inputManager.isMousePressed()) {
          if (canPlaceBarrier && activeBarriers.size() < MAX_BARRIERS) {
            double mx = inputManager.getMouseX();
            double my = inputManager.getMouseY();
            double limit30 = WIDTH * 0.30;
            double limit70 = WIDTH * 0.70;
            boolean inValidZone = isHost ? (mx > limit30) : (mx < limit70);

            if (inValidZone) {
              activeBarriers.add(new com.slingshot.entities.Barrier(mx - 22, my - 22));
              canPlaceBarrier = false;
            }
          }
        } else {
          canPlaceBarrier = true;
        }
      }
    } // FIN DEL BLOQUE DE PAUSA

    // ==========================================
    // 4. ESTO NO SE PAUSA (MUNDO Y ENEMIGOS)
    // ==========================================

    Iterator<FloatingIndicator> fIt = floatingIndicators.iterator();
    while (fIt.hasNext()) {
      FloatingIndicator fi = fIt.next();
      fi.update();
      if (!fi.isAlive())
        fIt.remove();
    }

    // Actualizar Balas y Colisiones EN TIEMPO REAL
    Iterator<Projectile> it = activeProjectiles.iterator();
    while (it.hasNext()) {
      Projectile p = it.next();
      p.update();
      boolean projectileDestroyed = false;

      // COLISIÓN CON BARRERAS
      if (p.isEnemy()) {
        Iterator<Barrier> bIt = activeBarriers.iterator();
        while (bIt.hasNext()) {
          Barrier b = bIt.next();
          if (p.getX() > b.getX() && p.getX() < b.getX() + b.getWidth() &&
              p.getY() > b.getY() && p.getY() < b.getY() + b.getHeight()) {
            b.takeDamage(p.getType());
            projectileDestroyed = true;
            if (!b.isAlive())
              bIt.remove();
            break;
          }
        }
      }

      // Colisión con Jugador Local
      if (p.isEnemy() && localPlayer.checkHit(p.getX(), p.getY())) {
        localPlayer.takeDamage();
        projectileDestroyed = true;
        engine.sendNetworkMessage("REWARD;SCORE;50");
      }

      // Colisión con Cajas
      if (p.isEnemy() && !p.getType().equals("artillery")) {
        for (Crate c : crates) {
          if (c.isAlive() && p.getX() > c.getX() && p.getX() < c.getX() + c.getSize() &&
              p.getY() > c.getY() && p.getY() < c.getY() + c.getSize()) {
            boolean destruyeBala = c.onHitByBullet(null, p);
            if (destruyeBala)
              projectileDestroyed = true;
            enviarRecompensaRed(c);
            break;
          }
        }
      }

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
        it.remove();
      }
    }

    if (System.currentTimeMillis() - lastRegenTime > REGEN_COOLDOWN) {
      lastRegenTime = System.currentTimeMillis();
      regenerarMapa();
    }

    if (localPlayer.getLives() <= 0) {
      finalizarPartida();
    }

    // Seguimos enviando nuestra posición aunque estemos pausados
    // para que el rival no nos vea desaparecer de su radar
    if (System.currentTimeMillis() - lastPosSend > 50) {
      engine.sendNetworkMessage(NetworkProtocol.formatPosition(localPlayer.getCenterX(), localPlayer.getCenterY()));
      lastPosSend = System.currentTimeMillis();
    }
  }

  public void updateOpponentPos(double x, double y) {
    this.oppX = x;
    this.oppY = y;
  }

  public void spawnRemoteProjectile(String type, double y, double angle, double power) {
    double startX = isHost ? WIDTH : 0;
    Projectile p = new Projectile(startX, y, angle, type, power, isHost, true);
    activeProjectiles.add(p);
  }

  private void shoot() {
    localPlayer.reduceAmmo();
    Projectile p = new Projectile(localPlayer.getCenterX(), localPlayer.getCenterY(), localPlayer.getAngle(),
        currentWeapon, chargePower, isHost, false);
    activeProjectiles.add(p);
  }

  private void render() {
    double hudX = isHost ? 20 : (WIDTH * 0.70) + 20;
    GraphicsContext gc = canvas.getGraphicsContext2D();

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

    localPlayer.render(gc);
    for (Projectile p : activeProjectiles)
      p.render(gc);

    gc.setFill(Color.WHITE);
    gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 20));

    gc.fillText("Vidas: " + localPlayer.getLives(), hudX, 30);
    gc.fillText("Munición: " + localPlayer.getAmmo(), hudX, 60);
    gc.fillText("Puntos: " + localPlayer.getScore(), hudX, 90);

    String modoActivo = isBuildingMode ? "CONSTRUCCIÓN (C)" : "ARMA: " + currentWeapon.toUpperCase();
    gc.fillText(modoActivo, hudX, 120);
    gc.fillText("Barreras: " + (MAX_BARRIERS - activeBarriers.size()), hudX, 140);

    if (localPlayer.isDoubleScoreActive()) {
      gc.setFill(Color.PURPLE);
      gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 22));
      gc.fillText("¡DOBLE PUNTUACIÓN! (" + localPlayer.getDoubleScoreTimeLeft() + "s)", hudX, 165);
    }

    gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.NORMAL, 12));
    gc.setStroke(Color.rgb(255, 0, 0, 0.4));
    gc.setLineWidth(2);
    gc.strokeLine(localPlayer.getCenterX(), localPlayer.getCenterY(), inputManager.getMouseX(),
        inputManager.getMouseY());

    if (isCharging && currentWeapon.equals("artillery")) {
      double barWidth = 50;
      double chargePercent = chargePower / MAX_POWER;
      gc.setFill(Color.rgb(0, 0, 0, 0.5));
      gc.fillRect(localPlayer.getCenterX() - barWidth / 2, localPlayer.getCenterY() - 45, barWidth, 6);
      gc.setFill(Color.rgb(231, 76, 60));
      gc.fillRect(localPlayer.getCenterX() - barWidth / 2, localPlayer.getCenterY() - 45, barWidth * chargePercent, 6);
    }

    for (Crate c : crates)
      c.render(gc);
    for (FloatingIndicator fi : floatingIndicators)
      fi.render(gc);
    for (Barrier b : activeBarriers)
      b.render(gc);

    if (isBuildingMode) {
      gc.setGlobalAlpha(0.3);
      gc.setFill(Color.LIGHTBLUE);
      gc.fillRect(inputManager.getMouseX() - 22, inputManager.getMouseY() - 22, 45, 45);
      gc.setGlobalAlpha(1.0);
    }

    // --- RADAR DEL OPONENTE ---
    double radarX = isHost ? WIDTH - 40 : 10;
    gc.setFill(Color.rgb(255, 0, 0, 0.3));
    gc.fillRect(radarX + 10, 0, 10, HEIGHT);
    gc.setFill(Color.LIME);
    gc.setEffect(new javafx.scene.effect.Bloom());
    gc.fillOval(radarX + 5, oppY - 10, 20, 20);
    gc.setEffect(null);

    double areaMovimiento = WIDTH * 0.30;
    double xRelativa = isHost ? (WIDTH - oppX) : oppX;
    int xSegmento = (int) ((xRelativa / areaMovimiento) * 29) + 1;
    xSegmento = Math.max(1, Math.min(30, xSegmento));

    gc.setFill(Color.WHITE);
    gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 24));
    gc.fillText("D:" + xSegmento, radarX - 20, oppY - 20);

    // CRONÓMETRO SUPERIOR
    String timeStr = String.format("%02d:%02d", gameTimeSeconds / 60, gameTimeSeconds % 60);
    gc.setFill(gameTimeSeconds <= 60 ? Color.RED : Color.WHITE);
    gc.setFont(Font.font("Arial", FontWeight.BOLD, 36));
    gc.setStroke(Color.BLACK);
    gc.setLineWidth(1.5);
    gc.fillText(timeStr, WIDTH / 2 - 45, 40);
    gc.strokeText(timeStr, WIDTH / 2 - 45, 40);
  }

  private void generateCrates() {
    java.util.Random rand = new java.util.Random();
    double minX = isHost ? WIDTH * 0.30 : 0;
    double maxX = isHost ? WIDTH : WIDTH * 0.70;

    int creadas = 0;
    int intentos = 0;
    while (creadas < 15 && intentos < 300) {
      intentos++;
      double cx = minX + rand.nextDouble() * (maxX - minX - 45);
      double cy = rand.nextDouble() * (HEIGHT - 45);
      boolean superpuesta = false;
      for (Crate existente : crates) {
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

  public void applyNetworkReward(String type, int amount) {
    if (localPlayer == null)
      return;
    double floatX = localPlayer.getCenterX() - 15;
    double floatY = localPlayer.getCenterY() - 40;

    if (type.equals("AMMO")) {
      localPlayer.addAmmo(amount);
      floatingIndicators.add(new FloatingIndicator(floatX, floatY, "+" + amount, ammoIcon));
    } else if (type.equals("LIFE")) {
      localPlayer.addLife();
      floatingIndicators.add(new FloatingIndicator(floatX, floatY, "+1", healthIcon));
    } else if (type.equals("DOUBLE")) {
      localPlayer.activateDoubleScore(amount);
      floatingIndicators.add(new FloatingIndicator(floatX - 20, floatY, "x2 PTS", doubleIcon));
    } else if (type.equals("SCORE")) {
      localPlayer.addScore(amount);
      this.opponentScoreDisplay = amount; // Sincroniza visualmente
      floatingIndicators.add(new FloatingIndicator(floatX, floatY, "+" + amount, null));
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

    if (!msg.isEmpty())
      engine.sendNetworkMessage(msg);
  }

  private void regenerarMapa() {
    java.util.Random rand = new java.util.Random();
    double minX = isHost ? WIDTH * 0.30 : 0;
    double maxX = isHost ? WIDTH : WIDTH * 0.70;

    for (Crate c : crates) {
      if (c instanceof IndestructibleCrate) {
        double newX = minX + rand.nextDouble() * (maxX - minX - 45);
        double newY = rand.nextDouble() * (HEIGHT - 45);
        c.setX(newX);
        c.setY(newY);
      }
    }

    if (crates.size() < MAX_CRATES) {
      int cuantasNuevas = 3;
      for (int i = 0; i < cuantasNuevas; i++) {
        if (crates.size() >= MAX_CRATES)
          break;
        double cx = minX + rand.nextDouble() * (maxX - minX - 45);
        double cy = rand.nextDouble() * (HEIGHT - 45);

        boolean superpuesta = false;
        for (Crate existente : crates) {
          if (Math.abs(existente.getX() - cx) < 45 && Math.abs(existente.getY() - cy) < 45) {
            superpuesta = true;
            break;
          }
        }

        if (!superpuesta) {
          double prob = rand.nextDouble();
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

  private void finalizarPartida() {
    this.isGameOver = true;
    engine.sendNetworkMessage("FIN_PARTIDA;" + localPlayer.getScore());
    verificarGanador();
  }

  public void recibirFinPartidaEnemigo(int scoreEnemigo) {
    this.opponentScore = scoreEnemigo;
    this.opponentScoreDisplay = scoreEnemigo;
    this.isGameOver = true;
    verificarGanador();
  }

  private void verificarGanador() {
    // Definimos el estado
    if (localPlayer.getScore() > opponentScore) {
      finalStatus = "¡VICTORIA!";
    } else if (localPlayer.getScore() < opponentScore) {
      finalStatus = "DERROTA";
    } else {
      finalStatus = "EMPATE TÁCTICO";
    }
  }

  // --- NUEVO MÉTODO PARA MOSTRAR LA PANTALLA FINAL ---
  private void showEndGameMenu() {
    endMenuShown = true;
    isPaused = true;

    Platform.runLater(() -> {
      VBox finContainer = new VBox(25);
      finContainer.setAlignment(Pos.CENTER);
      finContainer.setMaxSize(600, 500);

      Color colorBorde = finalStatus.equals("¡VICTORIA!") ? Color.web("#2ecc71") : Color.web("#e74c3c");
      String hexColor = finalStatus.equals("¡VICTORIA!") ? "#2ecc71" : "#e74c3c";

      finContainer.setStyle("-fx-background-color: rgba(0, 0, 0, 0.9); -fx-border-color: " + hexColor
          + "; -fx-border-width: 4; -fx-background-radius: 15; -fx-border-radius: 15;");

      Label lblResultado = new Label(finalStatus);
      lblResultado.setFont(Font.font("Arial", FontWeight.BOLD, 50));
      lblResultado.setTextFill(colorBorde);

      Label lblScore = new Label(
          "TUS PUNTOS: " + localPlayer.getScore() + "  |  RIVAL: " + (opponentScore == -1 ? "..." : opponentScore));
      lblScore.setFont(Font.font("Monospaced", FontWeight.BOLD, 22));
      lblScore.setTextFill(Color.WHITE);

      Button btnReplay = UIFactory.createMenuButton("VOLVER A JUGAR", "#3498db", () -> {
      });
      btnReplay.setOnAction(e -> {
        engine.sendNetworkMessage("REPLAY_REQUEST");
        btnReplay.setText("ESPERANDO RIVAL...");
        btnReplay.setDisable(true);
      });

      Button btnExit = UIFactory.createMenuButton("SALIR", "#e74c3c", () -> {
        engine.sendNetworkMessage("REPLAY_RESPONSE;NO");
        if (gameLoop != null)
          gameLoop.stop();
        if (onExitToHome != null)
          onExitToHome.run();
      });

      finContainer.getChildren().addAll(lblResultado, lblScore, new Label(""), btnReplay, btnExit);
      rootLayout.getChildren().add(finContainer);
    });
  }
}