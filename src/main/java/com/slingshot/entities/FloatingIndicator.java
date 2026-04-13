package com.slingshot.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class FloatingIndicator {
    private double x, y;
    private String text;
    private Image icon;
    private int lifeTime = 60; // 60 frames = 1 segundo flotando
    private int maxLife = 60;

    public FloatingIndicator(double x, double y, String text, Image icon) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.icon = icon;
    }

    public void update() {
        y -= 1.0; // Flota hacia arriba lentamente
        lifeTime--;
    }

    public void render(GraphicsContext gc) {
        double opacity = (double) lifeTime / maxLife;
        if (opacity < 0) opacity = 0;
        
        gc.setGlobalAlpha(opacity); // Efecto de desvanecimiento
        
        double textOffsetX = x;
        
        // Si hay ícono, lo dibujamos y corremos el texto a la derecha
        if (icon != null) {
            gc.drawImage(icon, x, y - 20, 24, 24); 
            textOffsetX += 30;
        }
        
        if (text != null && !text.isEmpty()) {
            gc.setFill(Color.LIMEGREEN); // Color verde para recompensas
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 22));
            gc.fillText(text, textOffsetX, y);
        }
        
        gc.setGlobalAlpha(1.0); // Restauramos la opacidad general
    }

    public boolean isAlive() { return lifeTime > 0; }
}