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

  // Variables temporales para saber a dónde disparar el siguiente paquete
  private String lastTargetIp = "127.0.0.1";
  private int lastTargetPort = 5001;
  private boolean isHost = false;

  @Override
  public void start(Stage primaryStage) {
    udpManager = new UDPManager();
    gameEngine = new GameEngine();

    // Puente para que el Engine pueda enviar mensajes sin conocer el UDPManager
    gameEngine.setNetworkSender(message -> {
      udpManager.send(message, lastTargetIp, lastTargetPort);
    });

   // 1. Observer de Red
     udpManager.addObserver(message -> {
         Platform.runLater(() -> {
             NetworkProtocol.processMessage(message, gameEngine);

             if (message.equals("HANDSHAKE_OK") && !isHost) {
                    System.out.println("[AppFX] -> Handshake recibido del HOST. Confirmando conexión...");
                    // El Cliente DEBE responder el Handshake para que el Host sepa que llegó
                    udpManager.send("HANDSHAKE_OK", lastTargetIp, lastTargetPort);
                }

             // NUEVO: Interceptar el paquete del Host para actualizar la GUI del Cliente
             if (message.startsWith("SETUP_PC1") && !isHost && currentSetupWindow != null) {
                 String[] tokens = message.split(";");
                 String mapName = tokens[1];
                 String hostChar = tokens[2];
                 currentSetupWindow.receiveHostData(mapName, hostChar);
             }
         });
     });

     // 2. Observer de Estado de Juego
     gameEngine.setOnStateChangeListener(newState -> {
         if (newState instanceof SetupState) {
             Platform.runLater(() -> {
                 // Guardamos la referencia en nuestra variable global
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

    // 3. Modificamos ligeramente la creación del Lobby para interceptar los datos
    // de conexión
    LobbyWindow lobby = new LobbyWindow(udpManager, gameEngine);
    // Le pasamos un callback para atrapar la IP/Puerto cuando el usuario da click
    // en Conectar    
        lobby.setOnConnectAction((ip, port, rolSeleccionado) -> {
            this.lastTargetIp = ip;
            this.lastTargetPort = port;
            this.isHost = rolSeleccionado; // Ahora el rol lo define el ComboBox, no el clic
        });

        lobby.display(primaryStage);
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