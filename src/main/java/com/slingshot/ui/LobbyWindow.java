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

    // Variable para controlar el latido de red
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

    public void display(Stage primaryStage) {
        primaryStage.setTitle("Sling-Shot Battle v2.0 - Conexión P2P");
        StackPane root = new StackPane();

        // 1. Carga de Imagen A Prueba de Fallos
        try {
            InputStream is = getClass().getResourceAsStream("/assets/lobby_bg.png");
            if (is != null) {
                Image bgImage = new Image(is);
                BackgroundImage background = new BackgroundImage(
                        bgImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true));
                root.setBackground(new Background(background));
            } else {
                root.setStyle("-fx-background-color: #2b2b2b;");
            }
        } catch (Exception e) {
            root.setStyle("-fx-background-color: #2b2b2b;");
        }

        // 2. Elementos Gráficos
        Label lblTitle = new Label("SALA DE CONEXIÓN");
        lblTitle.setStyle(
                "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white; -fx-effect: dropshadow( gaussian , rgba(0,0,0,0.8) , 5,0,0,2 );");

        Label lblMiIP = new Label("Tu IP Local: " + getLocalIP());
        lblMiIP.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        TextField txtLocalPort = new TextField("5000");
        txtLocalPort.setMaxWidth(200);
        TextField txtRemoteIp = new TextField("127.0.0.1");
        txtRemoteIp.setMaxWidth(200);
        TextField txtRemotePort = new TextField("5001");
        txtRemotePort.setMaxWidth(200);

        Button btnConnect = new Button("CONECTAR E INICIAR");
        btnConnect.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20 10 20;");

        VBox controls = new VBox(10);
        controls.setPadding(new Insets(30));
        controls.setAlignment(Pos.CENTER);
        controls.setMaxWidth(300);
        controls.setMaxHeight(480);
        controls.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-background-radius: 10;");

        Label lblDatosRival = new Label("--- DATOS DEL RIVAL ---");
        lblDatosRival.setStyle("-fx-text-fill: #f1c40f; -fx-font-weight: bold;");
        Label lbl1 = new Label("Puerto Local:");
        lbl1.setStyle("-fx-text-fill: white;");
        Label lbl2 = new Label("IP Remota:");
        lbl2.setStyle("-fx-text-fill: white;");
        Label lbl3 = new Label("Puerto Remoto:");
        lbl3.setStyle("-fx-text-fill: white;");

        ComboBox<String> cbRol = new ComboBox<>();
        cbRol.getItems().addAll("Soy el HOST (Creo la partida)", "Soy el CLIENTE (Me uno)");
        cbRol.setValue("Soy el HOST (Creo la partida)");
        cbRol.setStyle("-fx-font-weight: bold;");

        Label lblRol = new Label("Selecciona tu Rol:");
        lblRol.setStyle("-fx-text-fill: #f1c40f; -fx-font-weight: bold;");

        controls.getChildren().addAll(
                lblTitle, lblMiIP, lbl1, txtLocalPort, lblDatosRival, lbl2, txtRemoteIp, lbl3, txtRemotePort,
                new Label(""), lblRol, cbRol, new Label(""), btnConnect);

        // 3. Acción del Botón y Latido
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

                if (connectListener != null) {
                    connectListener.onConnect(remoteIp, remotePort, soyHost);
                }

                btnConnect.setDisable(true);
                cbRol.setDisable(true);
                btnConnect.setText(soyHost ? "ESPERANDO AL CLIENTE..." : "ESPERANDO HANDSHAKE...");
            } catch (NumberFormatException ex) {
                System.err.println("Error: Puertos inválidos.");
            }
        });

        root.getChildren().add(controls);
        Scene scene = new Scene(root, 1280, 720);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private String getLocalIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "Desconocida";
        }
    }
}