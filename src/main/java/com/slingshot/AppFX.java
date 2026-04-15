package com.slingshot;

import com.slingshot.core.GameEngine;
import com.slingshot.core.states.SetupState;
import com.slingshot.network.NetworkObserver;
import com.slingshot.network.NetworkProtocol;
import com.slingshot.network.UDPManager;
import com.slingshot.ui.ControlsWindow;
import com.slingshot.ui.CustomAlert;
import com.slingshot.ui.GameWindow;
import com.slingshot.ui.HomeWindow;
import com.slingshot.ui.LobbyWindow;
import com.slingshot.ui.RulesWindow;
import com.slingshot.ui.SetupWindow;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class AppFX extends Application {

  // Variables para guardar la identidad visual
  private String matchMapId = "desert";
  private String myCharacterId = "sniper";

  private UDPManager udpManager;
  private GameEngine gameEngine;
  private SetupWindow currentSetupWindow;
  private LobbyWindow lobbyWindow;

  // Declaramos gameWindow a nivel global para que la red la pueda ver
  private GameWindow gameWindow;

  // ¡NUEVO! Guardamos la ventana principal para poder navegar entre escenas
  private Stage primaryStage;

  private String lastTargetIp = "127.0.0.1";
  private int lastTargetPort = 5001;
  private boolean isHost = false;
  private boolean handshakeComplete = false;

  @Override
  public void start(Stage primaryStage) {
    this.primaryStage = primaryStage; // Guardamos la referencia

    try {
      udpManager = new UDPManager();
      gameEngine = new GameEngine();

      gameEngine.setNetworkSender(message -> {
        udpManager.send(message, lastTargetIp, lastTargetPort);
      });

      udpManager.addObserver(message -> {
        Platform.runLater(() -> {

          if (message.equals("HANDSHAKE_OK") && !isHost && !handshakeComplete) {
            System.out.println("[AppFX] -> Handshake recibido del HOST. Confirmando...");
            udpManager.send("HANDSHAKE_OK", lastTargetIp, lastTargetPort);
            handshakeComplete = true;
          }

          NetworkProtocol.processMessage(message, gameEngine);

          if (message.startsWith("SETUP_PC1") && !isHost && currentSetupWindow != null) {
            String[] tokens = message.split(";");
            String mapName = tokens[1];
            String hostChar = tokens[2];
            currentSetupWindow.receiveHostData(mapName, hostChar);
          }

          // RECIBIR BALAS DEL RIVAL
          if (message.startsWith("BULLET")) {
            String[] tokens = message.split(";");
            String type = tokens[1];
            double entryY = Double.parseDouble(tokens[2].replace(',', '.'));
            double angle = Double.parseDouble(tokens[3].replace(',', '.'));
            double power = Double.parseDouble(tokens[4].replace(',', '.'));

            if (gameWindow != null) {
              gameWindow.spawnRemoteProjectile(type, entryY, angle, power);
            }
          }

          // RECIBIR RECOMPENSAS DE CAJAS ENEMIGAS
          if (message.startsWith("REWARD")) {
            String[] tokens = message.split(";");
            String type = tokens[1];
            int amount = Integer.parseInt(tokens[2]);

            Platform.runLater(() -> {
              if (gameWindow != null) {
                gameWindow.applyNetworkReward(type, amount);
              }
            });
          }

          if (message.startsWith("FIN_PARTIDA")) {
            String[] tokens = message.split(";");
            int scoreRival = Integer.parseInt(tokens[1]);

            Platform.runLater(() -> {
              if (gameWindow != null) {
                gameWindow.recibirFinPartidaEnemigo(scoreRival);
              }
            });
          }

          // RECIBIR POSICIÓN DEL RIVAL
          if (message.startsWith("POS")) {
            String[] tokens = message.split(";");
            double ox = Double.parseDouble(tokens[1].replace(',', '.'));
            double oy = Double.parseDouble(tokens[2].replace(',', '.'));

            Platform.runLater(() -> {
              if (gameWindow != null) {
                gameWindow.updateOpponentPos(ox, oy);
              }
            });
          }
          if (message.equals("PLAYER_LEFT_SETUP")) {
            if (currentSetupWindow != null) {
              currentSetupWindow.showOpponentLeftAlert(() -> {
                // Al darle click a OK en la alerta, volvemos al inicio
                showHome();
              });
            }
          }
          if (message.equals("REPLAY_REQUEST")) {
            Platform.runLater(() -> {
              CustomAlert.showConfirm("¿Revancha?", "El oponente quiere jugar de nuevo. ¿Aceptas?",
                  () -> { // SI
                    udpManager.send("REPLAY_RESPONSE;YES", lastTargetIp, lastTargetPort);
                    gameEngine.setState(new SetupState()); // Volver al setup
                  },
                  () -> { // NO
                    udpManager.send("REPLAY_RESPONSE;NO", lastTargetIp, lastTargetPort);
                    showHome();
                  });
            });
          }

          if (message.startsWith("REPLAY_RESPONSE")) {
            String resp = message.split(";")[1];
            Platform.runLater(() -> {
              if (resp.equals("YES")) {
                gameEngine.setState(new SetupState());
              } else {
                CustomAlert.show("Petición Rechazada", "El rival ha decidido no jugar más.", () -> showHome());
              }
            });
          }
          // SI EL RIVAL ABANDONA EN MEDIO DE LA PARTIDA
          if (message.equals("PLAYER_QUIT_MATCH")) {
            Platform.runLater(() -> {
                CustomAlert.show("Conexión Perdida", "El rival ha abandonado la partida en curso.", () -> {
                    showHome(); // Nos saca al inicio a nosotros también
                });
            });
          }
        });
      });

      gameEngine.setOnStateChangeListener(newState -> {
        if (newState instanceof SetupState) {
          if (lobbyWindow != null)
            lobbyWindow.stopLobby(); // Apagamos latido
          handshakeComplete = true;

          Platform.runLater(() -> {
            currentSetupWindow = new SetupWindow(udpManager, lastTargetIp, lastTargetPort, isHost);
            // Configuramos la acción del botón volver físico
            currentSetupWindow.setOnBackListener(() -> {
              showHome();
            });

            // Atrapamos los datos elegidos
            currentSetupWindow.setOnSetupCompleteListener((mapId, charId) -> {
              this.matchMapId = mapId;
              this.myCharacterId = charId;
            });

            primaryStage.setScene(currentSetupWindow.createScene());
          });
        } else if (newState instanceof com.slingshot.core.states.PlayingState) {
          Platform.runLater(() -> {
            // Usamos la variable global y le añadimos el Listener de salida de balas
            gameWindow = new GameWindow(gameEngine, isHost, matchMapId, myCharacterId);

            gameWindow.setOnProjectileExitListener((type, y, angle, power) -> {
              String msg = NetworkProtocol.formatProjectile(type, y, angle, power);
              udpManager.send(msg, lastTargetIp, lastTargetPort);
            });

            primaryStage.setScene(gameWindow.createScene());
            primaryStage.centerOnScreen();
          });
        } else if (newState instanceof com.slingshot.core.states.PlayingState) {
          Platform.runLater(() -> {
            gameWindow = new GameWindow(gameEngine, isHost, matchMapId, myCharacterId);

            // ¡NUEVO! Conectamos el botón Abandonar de GameWindow hacia AppFX
            gameWindow.setOnExitToHome(() -> {
                showHome();
            });

            gameWindow.setOnProjectileExitListener((type, y, angle, power) -> {
              String msg = NetworkProtocol.formatProjectile(type, y, angle, power);
              udpManager.send(msg, lastTargetIp, lastTargetPort);
            });

            primaryStage.setScene(gameWindow.createScene());
            primaryStage.centerOnScreen();
          });
        }
      });

      // Inicializamos el Lobby en memoria
      lobbyWindow = new LobbyWindow(udpManager, gameEngine);
      lobbyWindow.setOnConnectAction((ip, port, rolSeleccionado) -> {
        this.lastTargetIp = ip;
        this.lastTargetPort = port;
        this.isHost = rolSeleccionado;
      });

      // ¡NUEVO! Arrancamos el juego mostrando la pantalla de Inicio
      showHome();

    } catch (Exception e) {
      System.err.println("=== ERROR GRAVE AL INICIAR LA INTERFAZ ===");
      e.printStackTrace();
    }
  }

  // --- MÉTODOS DE NAVEGACIÓN ---

  public void showHome() {
    HomeWindow home = new HomeWindow(
        this::showLobby, // Va al Lobby
        this::showRules, // Va a Reglas
        this::showControls // Va a Controles
    );
    primaryStage.setScene(home.getScene());
    primaryStage.setTitle("Slingshot Battle - Inicio");
    primaryStage.centerOnScreen();
    primaryStage.show();
  }

  public void showRules() {
    RulesWindow rules = new RulesWindow(this::showHome);
    primaryStage.setScene(rules.getScene());
    primaryStage.centerOnScreen();
  }

  public void showControls() {
    ControlsWindow controls = new ControlsWindow(this::showHome);
    primaryStage.setScene(controls.getScene());
    primaryStage.centerOnScreen();
  }

  public void showLobby() {
    if (lobbyWindow != null) {
      lobbyWindow.display(primaryStage, this::showHome);
    }
  }

  @Override
  public void stop() {
    if (udpManager != null)
      udpManager.close();
    System.exit(0);
  }

  public static void main(String[] args) {
    launch(args);
  }
}