package com.slingshot.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class CustomAlert {

  public static void show(String title, String message, Runnable onConfirm) {
    createBaseAlert(title, message, true, onConfirm, null);
  }

  public static void showConfirm(String title, String message, Runnable onYes, Runnable onNo) {
    createBaseAlert(title, message, false, onYes, onNo);
  }

  private static void createBaseAlert(String title, String message, boolean isSimple, Runnable action1,
      Runnable action2) {
    Stage window = new Stage();
    window.initModality(Modality.APPLICATION_MODAL);
    window.initStyle(StageStyle.UNDECORATED);

    VBox layout = new VBox(20);
    layout.setPadding(new Insets(30));
    layout.setAlignment(Pos.CENTER);
    layout.setStyle(
        "-fx-background-color: #1a1a1a; -fx-border-color: #f39c12; -fx-border-width: 3; -fx-border-radius: 10; -fx-background-radius: 10;");

    Label lblTitle = new Label(title.toUpperCase());
    lblTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));
    lblTitle.setTextFill(Color.web("#f39c12"));

    Label lblMsg = new Label(message);
    lblMsg.setFont(Font.font("Arial", 16));
    lblMsg.setTextFill(Color.WHITE);
    lblMsg.setWrapText(true);
    lblMsg.setAlignment(Pos.CENTER);

    HBox buttonBox = new HBox(20);
    buttonBox.setAlignment(Pos.CENTER);

    if (isSimple) {
      Button btnOk = UIFactory.createMenuButton("ACEPTAR", "#2ecc71", () -> {
        window.close();
        if (action1 != null)
          action1.run();
      });
      btnOk.setPrefSize(140, 45);
      buttonBox.getChildren().add(btnOk);
    } else {
      Button btnYes = UIFactory.createMenuButton("SÍ", "#2ecc71", () -> {
        window.close();
        if (action1 != null)
          action1.run();
      });
      Button btnNo = UIFactory.createMenuButton("NO", "#e74c3c", () -> {
        window.close();
        if (action2 != null)
          action2.run();
      });
      btnYes.setPrefSize(100, 45);
      btnNo.setPrefSize(100, 45);
      buttonBox.getChildren().addAll(btnYes, btnNo);
    }

    layout.getChildren().addAll(lblTitle, lblMsg, buttonBox);
    Scene scene = new Scene(layout);
    scene.setFill(Color.TRANSPARENT);
    window.setScene(scene);
    window.showAndWait();
  }
}