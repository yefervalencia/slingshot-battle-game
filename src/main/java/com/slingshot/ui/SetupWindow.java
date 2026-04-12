package com.slingshot.ui;

import com.slingshot.entities.GameCharacter;
import com.slingshot.entities.MapOption;
import com.slingshot.network.NetworkProtocol;
import com.slingshot.network.UDPManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;

public class SetupWindow {
    private UDPManager udpManager;
    private String targetIp;
    private int targetPort;
    private boolean isHost;

    private ComboBox<GameCharacter> cbCharacter;
    private ComboBox<MapOption> cbMap;
    private Button btnAction;
    private TextField txtName;

    public SetupWindow(UDPManager udpManager, String targetIp, int targetPort, boolean isHost) {
        this.udpManager = udpManager;
        this.targetIp = targetIp;
        this.targetPort = targetPort;
        this.isHost = isHost;
    }

    public Scene createScene() {
        StackPane root = new StackPane();

        // 1. Fondo del Setup
        try {
            Image bg = new Image(getClass().getResourceAsStream("/assets/setup_bg.png"));
            root.setBackground(new Background(new BackgroundImage(bg, 
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, 
                BackgroundPosition.CENTER, new BackgroundSize(100, 100, true, true, true, true))));
        } catch (Exception e) {
            root.setStyle("-fx-background-color: #34495e;");
        }

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(40));
        layout.setAlignment(Pos.CENTER);
        layout.setMaxWidth(500);
        layout.setMaxHeight(550);
        layout.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-background-radius: 15;");

        Label lblTitle = new Label(isHost ? "CONFIGURAR PARTIDA (HOST)" : "UNIRSE A PARTIDA (CLIENTE)");
        lblTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #ecf0f1;");

        txtName = new TextField(isHost ? "Host_Player" : "Guest_Player");

        // 2. Inicializar Datos
        cbCharacter = new ComboBox<>();
        setupCharacterComboBox();

        cbMap = new ComboBox<>();
        setupMapComboBox();

        btnAction = new Button(isHost ? "ENVIAR Y EMPEZAR" : "CONFIRMAR LISTO");
        btnAction.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");

        // Lógica de interacción
        if (isHost) {
            layout.getChildren().addAll(lblTitle, label("Tu Nombre:"), txtName, label("Tu Personaje:"), cbCharacter, label("Mapa:"), cbMap, new Label(""), btnAction);
            btnAction.setOnAction(e -> {
                String msg = NetworkProtocol.formatSetupPC1(cbMap.getValue().getId(), cbCharacter.getValue().getId(), txtName.getText());
                udpManager.send(msg, targetIp, targetPort);
                lockUI("Esperando al Cliente...");
            });
        } else {
            lockUI("Esperando configuración del Host...");
            layout.getChildren().addAll(lblTitle, label("Tu Nombre:"), txtName, label("Tu Personaje:"), cbCharacter, label("Mapa Elegido:"), cbMap, new Label(""), btnAction);
            btnAction.setOnAction(e -> {
                udpManager.send("READY_PC2;" + cbCharacter.getValue().getId() + ";" + txtName.getText(), targetIp, targetPort);
                lockUI("¡Listo! Esperando inicio...");
            });
        }

        root.getChildren().add(layout);
        return new Scene(root, 1280, 720);
    }

    private void setupCharacterComboBox() {
        List<GameCharacter> chars = new ArrayList<>();
        chars.add(new GameCharacter("sniper", "Sniper", "/assets/sniper_icon.png", "Alta precisión, poca vida."));
        chars.add(new GameCharacter("artillery", "Artillery", "/assets/artillery_icon.png", "Daño de área parabólico."));
        chars.add(new GameCharacter("tank", "Tank", "/assets/tank_icon.png", "Mucha resistencia, corto alcance."));
        
        cbCharacter.getItems().addAll(chars);
        cbCharacter.setCellFactory(createCharacterCellFactory());
        cbCharacter.setButtonCell(createCharacterCellFactory().call(null)); // Para el item seleccionado
        cbCharacter.getSelectionModel().selectFirst();
    }

    private void setupMapComboBox() {
        cbMap.getItems().add(new MapOption("desert", "Desierto Árido", "/assets/desert_thumb.png"));
        cbMap.getItems().add(new MapOption("forest", "Bosque Oscuro", "/assets/forest_thumb.png"));
        cbMap.getSelectionModel().selectFirst();
    }

    private void lockUI(String message) {
        btnAction.setDisable(true);
        btnAction.setText(message);
        cbCharacter.setDisable(true);
        cbMap.setDisable(true);
        txtName.setDisable(true);
    }

    public void receiveHostData(String mapId, String hostCharId) {
        // Buscar el mapa por ID
        cbMap.getItems().stream()
              .filter(m -> m.getId().equals(mapId))
              .findFirst().ifPresent(cbMap::setValue);

        // Remover personaje del host
        cbCharacter.getItems().removeIf(c -> c.getId().equals(hostCharId));
        cbCharacter.getSelectionModel().selectFirst();

        // Desbloquear para el cliente
        btnAction.setDisable(false);
        btnAction.setText("CONFIRMAR LISTO");
        cbCharacter.setDisable(false);
        txtName.setDisable(false);
    }

    private Label label(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #bdc3c7;");
        return l;
    }

    private Callback<ListView<GameCharacter>, ListCell<GameCharacter>> createCharacterCellFactory() {
        return lv -> new ListCell<>() {
            @Override
            protected void updateItem(GameCharacter item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    HBox box = new HBox(10);
                    box.setAlignment(Pos.CENTER_LEFT);
                    try {
                        ImageView iv = new ImageView(new Image(getClass().getResourceAsStream(item.getImagePath())));
                        iv.setFitHeight(30); iv.setPreserveRatio(true);
                        box.getChildren().add(iv);
                    } catch (Exception e) {}
                    box.getChildren().add(new Label(item.getName()));
                    setGraphic(box);
                }
            }
        };
    }
}