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

import java.util.ArrayList;
import java.util.List;

public class SetupWindow {
  private UDPManager udpManager;
  private String targetIp;
  private int targetPort;
  private boolean isHost;

  private TextField txtName;
  private Button btnAction;

  // Usamos ToggleGroups en lugar de ComboBox
  private ToggleGroup charGroup;
  private ToggleGroup mapGroup;

  private HBox charsBox;
  private HBox mapsBox;

  public SetupWindow(UDPManager udpManager, String targetIp, int targetPort, boolean isHost) {
    this.udpManager = udpManager;
    this.targetIp = targetIp;
    this.targetPort = targetPort;
    this.isHost = isHost;
  }

  public Scene createScene() {
    StackPane root = new StackPane();

    // 1. Fondo de la Sala
    try {
      Image bg = new Image(getClass().getResourceAsStream("/assets/setup_bg.png"));
      root.setBackground(new Background(new BackgroundImage(bg,
          BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
          BackgroundPosition.CENTER, new BackgroundSize(100, 100, true, true, true, true))));
    } catch (Exception e) {
      root.setStyle("-fx-background-color: #2c3e50;");
    }

    // Panel Principal
    VBox layout = new VBox(20);
    layout.setPadding(new Insets(30));
    layout.setAlignment(Pos.CENTER);
    layout.setMaxWidth(650); // Un poco más ancho para que quepan las imágenes
    layout.setMaxHeight(600);
    layout.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85); -fx-background-radius: 15;");

    Label lblTitle = new Label(isHost ? "SALA DE PREPARACIÓN (HOST)" : "SALA DE PREPARACIÓN (CLIENTE)");
    lblTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f1c40f;");

    txtName = new TextField(isHost ? "Host_Player" : "Guest_Player");
    txtName.setMaxWidth(250);

    // 2. Inicializar Grupos Visuales
    charGroup = new ToggleGroup();
    preventDeselection(charGroup); // Evita que se queden sin seleccionar nada
    charsBox = new HBox(15);
    charsBox.setAlignment(Pos.CENTER);
    setupCharacters();

    mapGroup = new ToggleGroup();
    preventDeselection(mapGroup);
    mapsBox = new HBox(15);
    mapsBox.setAlignment(Pos.CENTER);
    setupMaps();

    btnAction = new Button(isHost ? "ENVIAR Y EMPEZAR" : "CONFIRMAR LISTO");
    btnAction.setStyle(
        "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 10 30;");

    // 3. Lógica Asimétrica
    if (isHost) {
      layout.getChildren().addAll(
          lblTitle, label("Tu Nombre:"), txtName,
          label("Elige tu Personaje:"), charsBox,
          label("Elige el Mapa:"), mapsBox,
          new Label(""), btnAction);

      btnAction.setOnAction(e -> {
        GameCharacter selectedChar = (GameCharacter) charGroup.getSelectedToggle().getUserData();
        MapOption selectedMap = (MapOption) mapGroup.getSelectedToggle().getUserData();

        String msg = NetworkProtocol.formatSetupPC1(selectedMap.getId(), selectedChar.getId(), txtName.getText());
        udpManager.send(msg, targetIp, targetPort);
        lockUI("Esperando al Cliente...");
      });
    } else {
      lockUI("Esperando que el Host elija mapa...");
      layout.getChildren().addAll(
          lblTitle, label("Tu Nombre:"), txtName,
          label("Elige tu Personaje:"), charsBox,
          label("Mapa Elegido por el Host:"), mapsBox,
          new Label(""), btnAction);

      btnAction.setOnAction(e -> {
        GameCharacter selectedChar = (GameCharacter) charGroup.getSelectedToggle().getUserData();
        udpManager.send("READY_PC2;" + selectedChar.getId() + ";" + txtName.getText(), targetIp, targetPort);
        lockUI("¡Listo! Esperando inicio...");
      });
    }

    root.getChildren().add(layout);
    return new Scene(root, 1000, 700);
  }

  // --- MÉTODOS CREADORES DE UI ---

  private void setupCharacters() {
    List<GameCharacter> chars = new ArrayList<>();
    chars.add(new GameCharacter("sniper", "Sniper", "/assets/sniper_icon.png", ""));
    chars.add(new GameCharacter("artillery", "Artillery", "/assets/artillery_icon.png", ""));
    chars.add(new GameCharacter("tank", "Tank", "/assets/tank_icon.png", ""));

    for (GameCharacter gc : chars) {
      ToggleButton btn = createVisualButton(gc.getName(), gc.getImagePath(), 80);
      btn.setUserData(gc); // Guardamos la entidad dentro del botón
      btn.setToggleGroup(charGroup);
      charsBox.getChildren().add(btn);
    }
    charGroup.getToggles().get(0).setSelected(true); // Selecciona el primero por defecto
  }

  private void setupMaps() {
    List<MapOption> maps = new ArrayList<>();
    maps.add(new MapOption("desert", "Desierto Árido", "/assets/desert_thumb.png"));
    maps.add(new MapOption("forest", "Bosque Oscuro", "/assets/forest_thumb.png"));

    for (MapOption map : maps) {
      ToggleButton btn = createVisualButton(map.getName(), map.getImagePath(), 120);
      btn.setUserData(map);
      btn.setToggleGroup(mapGroup);
      mapsBox.getChildren().add(btn);
    }
    mapGroup.getToggles().get(0).setSelected(true);
  }

  private ToggleButton createVisualButton(String text, String imagePath, int imgWidth) {
    ToggleButton btn = new ToggleButton();

    VBox box = new VBox(10);
    box.setAlignment(Pos.CENTER);

    try {
      ImageView iv = new ImageView(new Image(getClass().getResourceAsStream(imagePath)));
      iv.setFitWidth(imgWidth);
      iv.setPreserveRatio(true);
      box.getChildren().add(iv);
    } catch (Exception e) {
      System.err.println("Falta imagen: " + imagePath);
    }

    Label lbl = new Label(text);
    lbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
    box.getChildren().add(lbl);

    btn.setGraphic(box);

    // Estilo CSS dinámico: cambia si está seleccionado o no
    String normalStyle = "-fx-background-color: transparent; -fx-border-color: #7f8c8d; -fx-border-radius: 5; -fx-cursor: hand;";
    String selectedStyle = "-fx-background-color: rgba(39, 174, 96, 0.3); -fx-border-color: #2ecc71; -fx-border-width: 2; -fx-border-radius: 5; -fx-cursor: hand;";

    btn.setStyle(normalStyle);
    btn.selectedProperty().addListener((obs, oldVal, isSelected) -> {
      btn.setStyle(isSelected ? selectedStyle : normalStyle);
    });

    return btn;
  }

  // --- LÓGICA DE CONTROL ---

  public void receiveHostData(String mapId, String hostCharId) {
    if (isHost)
      return;

    // 1. Desbloquear Interfaz General
    btnAction.setDisable(false);
    btnAction.setText("CONFIRMAR LISTO");
    txtName.setDisable(false);
    charsBox.setDisable(false);

    // 2. Seleccionar y Bloquear el Mapa del Host
    for (Toggle t : mapGroup.getToggles()) {
      MapOption m = (MapOption) t.getUserData();
      if (m.getId().equals(mapId)) {
        t.setSelected(true);
      }
      ((ToggleButton) t).setDisable(true); // El cliente no puede cambiar el mapa
    }

    // 3. Bloquear el Personaje que eligió el Host
    for (Toggle t : charGroup.getToggles()) {
      GameCharacter c = (GameCharacter) t.getUserData();
      ToggleButton btn = (ToggleButton) t;

      if (c.getId().equals(hostCharId)) {
        btn.setDisable(true);
        btn.setOpacity(0.3); // Lo hacemos visualmente "fantasma" para que sepa que está ocupado

        // Si justo estábamos parados en el que el host eligió, saltamos a otro
        if (btn.isSelected()) {
          for (Toggle fallback : charGroup.getToggles()) {
            if (fallback != t) {
              fallback.setSelected(true);
              break;
            }
          }
        }
      }
    }
  }

  private void lockUI(String message) {
    btnAction.setDisable(true);
    btnAction.setText(message);
    charsBox.setDisable(true);
    mapsBox.setDisable(true);
    txtName.setDisable(true);
  }

  private void preventDeselection(ToggleGroup group) {
    group.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
      if (newToggle == null && oldToggle != null) {
        group.selectToggle(oldToggle); // Si intentas deseleccionar dando click de nuevo, te lo impide
      }
    });
  }

  private Label label(String text) {
    Label l = new Label(text);
    l.setStyle("-fx-text-fill: #bdc3c7; -fx-font-weight: bold;");
    return l;
  }
}