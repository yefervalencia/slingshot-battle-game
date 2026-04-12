package com.slingshot;

import com.slingshot.core.GameEngine;
import com.slingshot.core.states.SetupState;
import com.slingshot.network.NetworkObserver;
import com.slingshot.network.NetworkProtocol;
import com.slingshot.network.UDPManager;
import com.slingshot.ui.GameWindow;
import com.slingshot.ui.LobbyWindow;
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
  
  // ¡CORRECCIÓN 1! Declaramos gameWindow a nivel global para que la red la pueda ver
  private GameWindow gameWindow; 

  private String lastTargetIp = "127.0.0.1";
  private int lastTargetPort = 5001;
  private boolean isHost = false;
  private boolean handshakeComplete = false;

  @Override
  public void start(Stage primaryStage) {
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
            double entryY = Double.parseDouble(tokens[2]);
            double angle = Double.parseDouble(tokens[3]);
            double power = Double.parseDouble(tokens[4]);

            if (gameWindow != null) {
              gameWindow.spawnRemoteProjectile(type, entryY, angle, power);
            }
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

            // Atrapamos los datos elegidos
            currentSetupWindow.setOnSetupCompleteListener((mapId, charId) -> {
              this.matchMapId = mapId;
              this.myCharacterId = charId;
            });

            primaryStage.setScene(currentSetupWindow.createScene());
          });
        } else if (newState instanceof com.slingshot.core.states.PlayingState) {
          Platform.runLater(() -> {
            
            // ¡CORRECCIÓN 2! Usamos la variable global y le añadimos el Listener de salida de balas
            gameWindow = new GameWindow(gameEngine, isHost, matchMapId, myCharacterId);
            
            gameWindow.setOnProjectileExitListener((type, y, angle, power) -> {
                String msg = NetworkProtocol.formatProjectile(type, y, angle, power);
                udpManager.send(msg, lastTargetIp, lastTargetPort);
            });

            primaryStage.setScene(gameWindow.createScene());
            primaryStage.centerOnScreen();
          });
        }
      });

      lobbyWindow = new LobbyWindow(udpManager, gameEngine);
      lobbyWindow.setOnConnectAction((ip, port, rolSeleccionado) -> {
        this.lastTargetIp = ip;
        this.lastTargetPort = port;
        this.isHost = rolSeleccionado;
      });

      lobbyWindow.display(primaryStage);

    } catch (Exception e) {
      System.err.println("=== ERROR GRAVE AL INICIAR LA INTERFAZ ===");
      e.printStackTrace();
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