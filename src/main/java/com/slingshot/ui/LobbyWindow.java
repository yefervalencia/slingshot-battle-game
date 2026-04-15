package com.slingshot.ui;

import com.slingshot.core.GameEngine;
import com.slingshot.network.UDPManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.InputStream;
import java.net.InetAddress;

public class LobbyWindow {

    public interface OnConnectListener {
        void onConnect(String targetIp, int targetPort, boolean isHost);
    }

    private UDPManager udpManager;
    private GameEngine gameEngine;
    private OnConnectListener connectListener;
    private volatile boolean isLobbyActive = true;

    public LobbyWindow(UDPManager udpManager, GameEngine gameEngine) {
        this.udpManager = udpManager;
        this.gameEngine = gameEngine;
    }

    public void setOnConnectAction(OnConnectListener listener) {
        this.connectListener = listener;
    }

    public void stopLobby() {
        this.isLobbyActive = false;
    }

    public void display(Stage stage, Runnable onBack) {
        stage.setTitle("Sling-Shot Battle v2.0 - Conexión P2P");
        StackPane layoutPrincipal = new StackPane();

        // 1. Fondo
        try {
            InputStream is = getClass().getResourceAsStream("/assets/lobby_bg.png");
            if (is != null) {
                Image bgImage = new Image(is);
                BackgroundImage background = new BackgroundImage(
                        bgImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true));
                layoutPrincipal.setBackground(new Background(background));
            } else {
                layoutPrincipal.setStyle("-fx-background-color: #2b2b2b;");
            }
        } catch (Exception e) {
            layoutPrincipal.setStyle("-fx-background-color: #2b2b2b;");
        }

        // 2. Panel Central de Controles
        VBox controls = new VBox(10);
        controls.setPadding(new Insets(30));
        controls.setAlignment(Pos.CENTER);
        controls.setMaxWidth(350);
        controls.setMaxHeight(500);
        controls.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.85); -fx-background-radius: 15; -fx-border-color: #f39c12; -fx-border-width: 2; -fx-border-radius: 15;");

        Label lblTitle = new Label("SALA DE CONEXIÓN");
        lblTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label lblMiIP = new Label("Tu IP Local: " + getLocalIP());
        lblMiIP.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");

        TextField txtLocalPort = new TextField("5000");
        txtLocalPort.setMaxWidth(200);
        TextField txtRemoteIp = new TextField("127.0.0.1");
        txtRemoteIp.setMaxWidth(200);
        TextField txtRemotePort = new TextField("5001");
        txtRemotePort.setMaxWidth(200);

        Button btnConnect = new Button("CONECTAR E INICIAR");
        btnConnect.setStyle(
                "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20 10 20; -fx-background-radius: 5;");

        Label lblDatosRival = new Label("--- DATOS DEL RIVAL ---");
        lblDatosRival.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");

        ComboBox<String> cbRol = new ComboBox<>();
        cbRol.getItems().addAll("Soy el HOST (Creo la partida)", "Soy el CLIENTE (Me uno)");
        cbRol.setValue("Soy el HOST (Creo la partida)");
        cbRol.setStyle("-fx-font-weight: bold;");

        Label lblRol = new Label("Selecciona tu Rol:");
        lblRol.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");

        // --- ETIQUETAS BLANCAS CORREGIDAS ---
        Label lblPuertoLocal = new Label("Puerto Local:");
        lblPuertoLocal.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Label lblIpRemota = new Label("IP Remota:");
        lblIpRemota.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Label lblPuertoRemoto = new Label("Puerto Remoto:");
        lblPuertoRemoto.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        controls.getChildren().addAll(
                lblTitle, lblMiIP, new Label(""),
                lblPuertoLocal, txtLocalPort, new Label(""),
                lblDatosRival,
                lblIpRemota, txtRemoteIp,
                lblPuertoRemoto, txtRemotePort,
                new Label(""), lblRol, cbRol, new Label(""), btnConnect);

        btnConnect.setOnAction(e -> {
            try {
                int localPort = Integer.parseInt(txtLocalPort.getText());
                int remotePort = Integer.parseInt(txtRemotePort.getText());
                String remoteIp = txtRemoteIp.getText();
                boolean soyHost = cbRol.getValue().contains("HOST");

                udpManager.startListening(localPort);

                if (soyHost) {
                    Thread handshakeThread = new Thread(() -> {
                        while (isLobbyActive) {
                            udpManager.send("HANDSHAKE_OK", remoteIp, remotePort);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ignored) {
                            }
                        }
                    });
                    handshakeThread.setDaemon(true);
                    handshakeThread.start();
                }

                if (connectListener != null)
                    connectListener.onConnect(remoteIp, remotePort, soyHost);

                btnConnect.setDisable(true);
                cbRol.setDisable(true);
                btnConnect.setText(soyHost ? "ESPERANDO AL CLIENTE..." : "ESPERANDO HANDSHAKE...");
            } catch (NumberFormatException ex) {
                System.err.println("Error: Puertos inválidos.");
            }
        });

        // 3. Botón Circular Unificado
        Button btnBack = UIFactory.createBackButton(onBack);
        StackPane.setAlignment(btnBack, Pos.TOP_LEFT);
        StackPane.setMargin(btnBack, new Insets(20));

        // IMPORTANTE: controls primero, btnBack de último para que quede encima de todo
        layoutPrincipal.getChildren().addAll(controls, btnBack);

        Scene scene = new Scene(layoutPrincipal, 1280, 720);
        stage.setScene(scene);
        stage.show();
    }

    private String getLocalIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "Desconocida";
        }
    }
}