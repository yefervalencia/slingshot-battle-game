package com.slingshot.ui;

import com.slingshot.network.UDPManager;
import com.slingshot.network.NetworkProtocol;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class SetupWindow {
    private UDPManager udpManager;
    private String targetIp;
    private int targetPort;
    private boolean isHost;

    public SetupWindow(UDPManager udpManager, String targetIp, int targetPort, boolean isHost) {
        this.udpManager = udpManager;
        this.targetIp = targetIp;
        this.targetPort = targetPort;
        this.isHost = isHost;
    }

    public Scene createScene() {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(40));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #2c3e50;"); // Fondo oscuro elegante

        Label lblTitle = new Label(isHost ? "SALA DE PREPARACIÓN (HOST)" : "SALA DE PREPARACIÓN (CLIENTE)");
        lblTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        // --- CAMPOS DE FORMULARIO ---
        TextField txtName = new TextField(isHost ? "Jugador_Host" : "Jugador_Cliente");
        txtName.setMaxWidth(200);

        ComboBox<String> cbCharacter = new ComboBox<>();
        cbCharacter.getItems().addAll("Sniper", "Artillery", "Tank");
        cbCharacter.setValue("Sniper");
        cbCharacter.setMaxWidth(200);

        ComboBox<String> cbMap = new ComboBox<>();
        cbMap.getItems().addAll("Desert_Map", "Forest_Map");
        cbMap.setValue("Desert_Map");
        cbMap.setMaxWidth(200);

        Button btnAction = new Button(isHost ? "ENVIAR CONFIGURACIÓN" : "ESTOY LISTO");
        btnAction.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");

        // --- LÓGICA DE ROLES ASIMÉTRICOS ---
        if (isHost) {
            // El Host puede elegir el mapa
            layout.getChildren().addAll(
                lblTitle,
                new Label("Tu Nombre:"), txtName,
                new Label("Tu Personaje:"), cbCharacter,
                new Label("Mapa de Batalla:"), cbMap,
                new Label(""), btnAction
            );

            btnAction.setOnAction(e -> {
                String msg = NetworkProtocol.formatSetupPC1(cbMap.getValue(), cbCharacter.getValue(), txtName.getText());
                udpManager.send(msg, targetIp, targetPort);
                btnAction.setDisable(true);
                btnAction.setText("ESPERANDO AL CLIENTE...");
            });

        } else {
            // El Cliente NO puede elegir mapa (por ahora lo deshabilitamos visualmente)
            cbMap.setDisable(true);
            cbMap.setPromptText("Esperando mapa del Host...");
            
            layout.getChildren().addAll(
                lblTitle,
                new Label("Tu Nombre:"), txtName,
                new Label("Tu Personaje:"), cbCharacter,
                new Label("Mapa (Elegido por Host):"), cbMap,
                new Label(""), btnAction
            );

            // TODO: Más adelante activaremos este botón solo cuando llegue el SETUP_PC1 del Host
            btnAction.setOnAction(e -> {
                // Enviamos nuestro READY_PC2 (Token inventado por ti en el Documento Maestro)
                udpManager.send("READY_PC2;" + cbCharacter.getValue() + ";" + txtName.getText(), targetIp, targetPort);
                btnAction.setDisable(true);
                btnAction.setText("ESPERANDO INICIO...");
            });
        }

        // Estilizar los Labels genéricos
        layout.getChildren().forEach(node -> {
            if (node instanceof Label && node != lblTitle) {
                ((Label) node).setStyle("-fx-text-fill: #bdc3c7;");
            }
        });

        return new Scene(layout, 1280, 720);
    }
}