package com.slingshot.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class CustomAlert {

    public static void show(String title, String message, Runnable onConfirm) {
        Stage window = new Stage();
        
        // Bloquea eventos en otras ventanas hasta que se cierre esta
        window.initModality(Modality.APPLICATION_MODAL);
        window.initStyle(StageStyle.UNDECORATED); // Sin bordes de Windows para que sea más "gamer"
        window.setTitle(title);

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        
        // Estilo del contenedor (Fondo oscuro con borde neón)
        layout.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #e74c3c; -fx-border-width: 3; -fx-border-radius: 10; -fx-background-radius: 10;");

        Label lblTitle = new Label(title.toUpperCase());
        lblTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        lblTitle.setTextFill(Color.web("#e74c3c"));

        Label lblMsg = new Label(message);
        lblMsg.setFont(Font.font("Arial", 16));
        lblMsg.setTextFill(Color.WHITE);
        lblMsg.setWrapText(true);

        // Reutilizamos la UIFactory para el botón de aceptar
        javafx.scene.control.Button btnOk = UIFactory.createMenuButton("ACEPTAR", "#e74c3c", () -> {
            window.close();
            if (onConfirm != null) onConfirm.run();
        });
        btnOk.setPrefWidth(150);
        btnOk.setPrefHeight(45);

        layout.getChildren().addAll(lblTitle, lblMsg, btnOk);

        Scene scene = new Scene(layout);
        scene.setFill(Color.TRANSPARENT);
        window.setScene(scene);
        window.showAndWait();
    }
}