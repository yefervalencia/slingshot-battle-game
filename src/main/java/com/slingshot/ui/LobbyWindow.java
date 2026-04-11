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

import java.net.InetAddress;

public class LobbyWindow {

    private UDPManager udpManager;
    private GameEngine gameEngine;
    private OnConnectListener connectListener;
    private volatile boolean isLobbyActive = true;

    public void stopLobby() {
        this.isLobbyActive = false;
    }

    public LobbyWindow(UDPManager udpManager, GameEngine gameEngine) {
        this.udpManager = udpManager;
        this.gameEngine = gameEngine;
    }

    public void setOnConnectAction(OnConnectListener listener) {
        this.connectListener = listener;
    }

    public void display(Stage primaryStage) {
        primaryStage.setTitle("Sling-Shot Battle v2.0 - Conexión P2P");

        StackPane root = new StackPane();

        // Carga de la imagen
        try {
            Image bgImage = new Image(getClass().getResourceAsStream("/assets/lobby_bg.png"));
            BackgroundImage background = new BackgroundImage(
                    bgImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true));
            root.setBackground(new Background(background));
        } catch (Exception e) {
            System.err.println(
                    "Advertencia: No se encontró la imagen de fondo en /assets/lobby_bg.png. Usando fondo sólido.");
            root.setStyle("-fx-background-color: #2b2b2b;");
        }

        // Títulos e inputs
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

        // Panel semitransparente
        VBox controls = new VBox(10);
        controls.setPadding(new Insets(30));
        controls.setAlignment(Pos.CENTER);
        controls.setMaxWidth(300);
        controls.setMaxHeight(480);
        controls.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-background-radius: 10;");

        Label lblDatosRival = new Label("--- DATOS DEL RIVAL ---");
        lblDatosRival.setStyle("-fx-text-fill: #f1c40f; -fx-font-weight: bold;");

        Label lbl1 = new Label("Puerto de Escucha Local:");
        lbl1.setStyle("-fx-text-fill: white;");
        Label lbl2 = new Label("IP Remota:");
        lbl2.setStyle("-fx-text-fill: white;");
        Label lbl3 = new Label("Puerto Remoto:");
        lbl3.setStyle("-fx-text-fill: white;");

        // --- NUEVO SELECTOR DE ROL ---
        ComboBox<String> cbRol = new ComboBox<>();
        cbRol.getItems().addAll("Soy el HOST (Creo la partida)", "Soy el CLIENTE (Me uno)");
        cbRol.setValue("Soy el HOST (Creo la partida)");
        cbRol.setStyle("-fx-font-weight: bold;");

        Label lblRol = new Label("Selecciona tu Rol:");
        lblRol.setStyle("-fx-text-fill: #f1c40f; -fx-font-weight: bold;");

        // Ensamblaje seguro sin duplicados
        controls.getChildren().addAll(
                lblTitle, lblMiIP,
                lbl1, txtLocalPort,
                lblDatosRival,
                lbl2, txtRemoteIp,
                lbl3, txtRemotePort,
                new Label(""), // Espaciador
                lblRol, cbRol,
                new Label(""), // Espaciador
                btnConnect);

        btnConnect.setOnAction(e -> {
            try {
                int localPort = Integer.parseInt(txtLocalPort.getText());
                int remotePort = Integer.parseInt(txtRemotePort.getText());
                String remoteIp = txtRemoteIp.getText();

                // Leemos el rol
                boolean soyHost = cbRol.getValue().contains("HOST");

                udpManager.startListening(localPort);

                // Solo el Host dispara el Handshake, el cliente espera
                if (soyHost) {
                    // MÁQUINA DE PULSOS: El Host ruega por conexión hasta que el Cliente despierte
                    Thread handshakeThread = new Thread(() -> {
                        while (isLobbyActive) {
                            udpManager.send("HANDSHAKE_OK", remoteIp, remotePort);
                            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                        }
                    });
                    handshakeThread.setDaemon(true); // Se cierra si cerramos el juego
                    handshakeThread.start();
                }

                if (connectListener != null) {
                    connectListener.onConnect(remoteIp, remotePort, soyHost);
                }

                btnConnect.setDisable(true);
                cbRol.setDisable(true);
                btnConnect.setText(soyHost ? "ESPERANDO AL CLIENTE..." : "ESPERANDO HANDSHAKE...");
            } catch (NumberFormatException ex) {
                System.err.println("Error: Los puertos deben ser números.");
            }
        });

        root.getChildren().add(controls);

        Scene scene = new Scene(root, 1080, 720);
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