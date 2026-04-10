package com.slingshot;

import com.slingshot.core.GameEngine;
import com.slingshot.network.NetworkObserver;
import com.slingshot.network.NetworkProtocol;
import com.slingshot.network.UDPManager;
import com.slingshot.ui.LobbyWindow;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class AppFX extends Application {

    private UDPManager udpManager;
    private GameEngine gameEngine;

    @Override
    public void start(Stage primaryStage) {
        // Inicializamos el "Backend"
        udpManager = new UDPManager();
        gameEngine = new GameEngine();

        // Conectamos el Observer
        udpManager.addObserver(new NetworkObserver() {
            @Override
            public void onMessageReceived(String message) {
                // IMPORTANTE: Como la red corre en un hilo secundario y JavaFX en su propio hilo principal,
                // debemos usar Platform.runLater para evitar que la UI colapse al recibir un paquete.
                Platform.runLater(() -> {
                    NetworkProtocol.processMessage(message, gameEngine);
                });
            }
        });

        // Mostramos la interfaz
        LobbyWindow lobby = new LobbyWindow(udpManager, gameEngine);
        lobby.display(primaryStage);
    }

    @Override
    public void stop() {
        // Apagado seguro al cerrar la ventana
        if (udpManager != null) {
            udpManager.close();
        }
        System.out.println("Juego cerrado limpiamente.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}