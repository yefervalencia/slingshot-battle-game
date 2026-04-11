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

  // Variables de interfaz accesibles
  private ComboBox<String> cbCharacter;
  private ComboBox<String> cbMap;
  private Button btnAction;

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
    layout.setStyle("-fx-background-color: #2c3e50;");

    Label lblTitle = new Label(isHost ? "SALA DE PREPARACIÓN (HOST)" : "SALA DE PREPARACIÓN (CLIENTE)");
    lblTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

    TextField txtName = new TextField(isHost ? "Jugador_Host" : "Jugador_Cliente");
    txtName.setMaxWidth(200);

    cbCharacter = new ComboBox<>();
    // Por ahora usamos Strings. Más adelante lo conectaremos a tus entidades reales
    cbCharacter.getItems().addAll("Sniper", "Artillery", "Tank");
    cbCharacter.setValue("Sniper");
    cbCharacter.setMaxWidth(200);

    cbMap = new ComboBox<>();
    cbMap.getItems().addAll("Desert_Map", "Forest_Map");
    cbMap.setValue("Desert_Map");
    cbMap.setMaxWidth(200);

    btnAction = new Button(isHost ? "ENVIAR CONFIGURACIÓN" : "ESTOY LISTO");
    btnAction.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");

    if (isHost) {
      layout.getChildren().addAll(
          lblTitle, new Label("Tu Nombre:"), txtName, new Label("Tu Personaje:"), cbCharacter,
          new Label("Mapa de Batalla:"), cbMap, new Label(""), btnAction);

      btnAction.setOnAction(e -> {
        String msg = NetworkProtocol.formatSetupPC1(cbMap.getValue(), cbCharacter.getValue(), txtName.getText());
        udpManager.send(msg, targetIp, targetPort);
        btnAction.setDisable(true);
        btnAction.setText("ESPERANDO AL CLIENTE...");
        cbCharacter.setDisable(true);
        cbMap.setDisable(true);
      });
    } else {
      // ESTADO INICIAL DEL CLIENTE: TOTALMENTE BLOQUEADO
      cbMap.setDisable(true);
      cbCharacter.setDisable(true);
      btnAction.setDisable(true);
      btnAction.setText("ESPERANDO QUE EL HOST ELIJA...");

      layout.getChildren().addAll(
          lblTitle, new Label("Tu Nombre:"), txtName, new Label("Tu Personaje:"), cbCharacter,
          new Label("Mapa (Elegido por Host):"), cbMap, new Label(""), btnAction);

      btnAction.setOnAction(e -> {
        udpManager.send("READY_PC2;" + cbCharacter.getValue() + ";" + txtName.getText(), targetIp, targetPort);
        btnAction.setDisable(true);
        btnAction.setText("ESPERANDO INICIO...");
        cbCharacter.setDisable(true);
      });
    }

    layout.getChildren().forEach(node -> {
      if (node instanceof Label && node != lblTitle) {
        ((Label) node).setStyle("-fx-text-fill: #bdc3c7;");
      }
    });

    return new Scene(layout, 1280, 720);
  }

  // --- EL MÉTODO MÁGICO PARA ACTUALIZAR AL CLIENTE ---
  public void receiveHostData(String mapName, String hostCharacter) {
    if (!isHost) {
      cbMap.setValue(mapName); // Mostramos el mapa que eligió el Host

      cbCharacter.getItems().remove(hostCharacter); // BLOQUEAMOS EL PERSONAJE DEL HOST
      cbCharacter.getSelectionModel().selectFirst(); // Seleccionamos el siguiente disponible
      cbCharacter.setDisable(false); // Desbloqueamos para que el Cliente elija

      btnAction.setDisable(false); // Desbloqueamos el botón
      btnAction.setText("ESTOY LISTO");
    }
  }
}