package com.slingshot.entities;

import com.slingshot.core.InputManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Player {
    private double x, y;
    private final double SIZE = 60.0;
    private final double SPEED = 5.0;
    private Image skin;
    private double angle = 0; // Ángulo hacia donde mira

    // Stats
    private int lives = 3;
    private int ammo = 20;

    public Player(double startX, double startY, Image skin) {
        this.x = startX;
        this.y = startY;
        this.skin = skin;
    }

    public void update(InputManager input, double minX, double maxX, double maxY) {
        // Movimiento
        if (input.isKeyPressed("W")) y -= SPEED;
        if (input.isKeyPressed("S")) y += SPEED;
        if (input.isKeyPressed("A")) x -= SPEED;
        if (input.isKeyPressed("D")) x += SPEED;

        // Colisiones con el mapa
        if (y < 0) y = 0;
        if (y > maxY - SIZE) y = maxY - SIZE;
        if (x < minX) x = minX;
        if (x > maxX - SIZE) x = maxX - SIZE;

        // Calcular ángulo hacia el mouse (Trigonometría Básica)
        double centerX = x + SIZE / 2;
        double centerY = y + SIZE / 2;
        double dx = input.getMouseX() - centerX;
        double dy = input.getMouseY() - centerY;
        this.angle = Math.toDegrees(Math.atan2(dy, dx));
    }

    public void render(GraphicsContext gc) {
        double centerX = x + SIZE / 2;
        double centerY = y + SIZE / 2;

        gc.save(); // Guardamos el estado del lienzo
        
        // Movemos el eje de rotación al centro del jugador
        gc.translate(centerX, centerY);
        gc.rotate(angle); // Rotamos el lienzo

        // Dibujamos al jugador (compensando la traslación previa)
        if (skin != null) {
            gc.drawImage(skin, -SIZE / 2, -SIZE / 2, SIZE, SIZE);
        } else {
            gc.setFill(Color.BLUE);
            gc.fillRect(-SIZE / 2, -SIZE / 2, SIZE, SIZE);
        }

        gc.restore(); // Restauramos el lienzo para que el resto del mapa no gire
    }

    public double getCenterX() { return x + SIZE / 2; }
    public double getCenterY() { return y + SIZE / 2; }
    public double getAngle() { return angle; }
    public int getLives() { return lives; }
    public int getAmmo() { return ammo; }
    public void reduceAmmo() { this.ammo--; }
}