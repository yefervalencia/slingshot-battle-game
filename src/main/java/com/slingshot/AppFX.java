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

  private UDPManager udpManager;
  private GameEngine gameEngine;
  private SetupWindow currentSetupWindow;

  private String lastTargetIp = "127.0.0.1";
  private int lastTargetPort = 5001;
  private boolean isHost = false;

  private LobbyWindow lobbyWindow; // Para poder apagar su latido
  private boolean handshakeComplete = false; // Para evitar responder ecos infinitos

  @Override
  public void start(Stage primaryStage) {
    udpManager = new UDPManager();
    gameEngine = new GameEngine();

    gameEngine.setNetworkSender(message -> {
      udpManager.send(message, lastTargetIp, lastTargetPort);
    });

    udpManager.addObserver(message -> {
      Platform.runLater(() -> {
        NetworkProtocol.processMessage(message, gameEngine);

        // El cliente confirma la conexión devolviendo el Handshake (Solo 1 vez)
                if (message.equals("HANDSHAKE_OK") && !isHost && !handshakeComplete) {
                    System.out.println("[AppFX] -> Handshake recibido del HOST. Confirmando conexión...");
                    udpManager.send("HANDSHAKE_OK", lastTargetIp, lastTargetPort);
                    handshakeComplete = true; // Bloqueamos ecos futuros del Host
                }

        // El cliente procesa los datos del Host
        if (message.startsWith("SETUP_PC1") && !isHost && currentSetupWindow != null) {
          String[] tokens = message.split(";");
          String mapName = tokens[1];
          String hostChar = tokens[2];
          currentSetupWindow.receiveHostData(mapName, hostChar);
        }
      });
    });

    gameEngine.setOnStateChangeListener(newState -> {
      if (newState instanceof SetupState) {
                // APAGAMOS EL LATIDO DEL HOST
                if (lobbyWindow != null) lobbyWindow.stopLobby(); 
                handshakeComplete = true; 

                Platform.runLater(() -> {
                    currentSetupWindow = new SetupWindow(udpManager, lastTargetIp, lastTargetPort, isHost);
                    primaryStage.setScene(currentSetupWindow.createScene());
                });
      } else if (newState instanceof com.slingshot.core.states.PlayingState) {
        Platform.runLater(() -> {
          GameWindow gameWindow = new GameWindow(gameEngine);
          primaryStage.setScene(gameWindow.createScene());
          primaryStage.centerOnScreen();
        });
      }
    });

    LobbyWindow lobby = new LobbyWindow(udpManager, gameEngine);

    lobbyWindow.setOnConnectAction((ip, port, rolSeleccionado) -> {
      this.lastTargetIp = ip;
      this.lastTargetPort = port;
      this.isHost = rolSeleccionado;
    });

    lobby.display(primaryStage);
  }

  @Override
  public void stop() {
    if (udpManager != null) {
      udpManager.close();
    }
    System.exit(0);
  }

  public static void main(String[] args) {
    launch(args);
  }
}