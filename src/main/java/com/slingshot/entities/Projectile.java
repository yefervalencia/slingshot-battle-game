package com.slingshot.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Projectile {
    private double x, y;
    private double velX, velY;
    private String type; // "sniper" o "artillery"
    private boolean isAlive = true;

    // Constantes físicas
    private final double SPEED = 15.0;

    public Projectile(double startX, double startY, double angleDeg, String type) {
        this.x = startX;
        this.y = startY;
        this.type = type;

        // Convertimos el ángulo a radianes para calcular la velocidad en X e Y
        double rad = Math.toRadians(angleDeg);
        this.velX = Math.cos(rad) * SPEED;
        this.velY = Math.sin(rad) * SPEED;
    }

    public void update() {
        x += velX;
        y += velY;

        // Si la bala sale de la pantalla de 1280x720, la marcamos para borrarla
        if (x < 0 || x > 1280 || y < 0 || y > 720) {
            isAlive = false;
        }
    }

    public void render(GraphicsContext gc) {
        gc.setFill(type.equals("sniper") ? Color.CYAN : Color.ORANGE);
        // Dibujamos un círculo para la bala (las de artillería son más grandes)
        double size = type.equals("sniper") ? 6 : 12;
        gc.fillOval(x - size/2, y - size/2, size, size);
    }

    public boolean isAlive() { return isAlive; }
}