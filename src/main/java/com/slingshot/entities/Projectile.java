package com.slingshot.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Projectile {
    private double x, y;
    private double velX, velY;
    private String type;
    private boolean isAlive = true;
    private boolean isHost;
    private boolean isEnemy;
    private double angle; // Guardamos el ángulo original para el relevo de red
    private double power;

    // Físicas
    private final double GRAVITY = 0.4;
    private final double WIDTH = 1280;
    private final double HEIGHT = 720;
    private final double RADIUS;

    // Recibe la potencia (power) y el rol (isHost)
    public Projectile(double startX, double startY, double angleDeg, String type, double power, boolean isHost,
            boolean isEnemy) {
        this.x = startX;
        this.y = startY;
        this.type = type;
        this.angle = angleDeg;
        this.power = power;
        this.isHost = isHost;
        this.isEnemy = isEnemy;

        double rad = Math.toRadians(angleDeg);

        if (type.equals("sniper")) {
            double speed = 25.0;
            this.velX = Math.cos(rad) * speed;
            this.velY = Math.sin(rad) * speed;
            this.RADIUS = 4;
        } else {
            this.velX = Math.cos(rad) * power;
            this.velY = Math.sin(rad) * power;
            this.RADIUS = 8;
        }
    }

    public void update() {
        if (!isAlive)
            return;

        // Si es artillería, la gravedad tira de la bala hacia abajo (Y aumenta)
        if (type.equals("artillery")) {
            velY += GRAVITY;
        }

        x += velX;
        y += velY;

        // --- SISTEMA DE COLISIONES Y REBOTES ---
        if (type.equals("sniper")) {
            // 1. Rebote en Techo (Y=0) y Piso (Y=720)
            if (y - RADIUS <= 0) {
                y = RADIUS;
                velY = -velY;
            }
            if (y + RADIUS >= HEIGHT) {
                y = HEIGHT - RADIUS;
                velY = -velY;
            }

            // 2. Rebote en la espalda y cruce de red
            if (isHost) {
                if (x - RADIUS <= 0) {
                    x = RADIUS;
                    velX = -velX;
                } // Rebota en la pared izquierda
                if (x > WIDTH)
                    isAlive = false; // Cruza la línea derecha (Se va por red)
            } else {
                if (x + RADIUS >= WIDTH) {
                    x = WIDTH - RADIUS;
                    velX = -velX;
                } // Rebota en la pared derecha
                if (x < 0)
                    isAlive = false; // Cruza la línea izquierda (Se va por red)
            }
        } else if (type.equals("artillery")) {
            // La artillería explota si toca piso o techo
            if (y + RADIUS >= HEIGHT || y - RADIUS <= 0)
                isAlive = false;

            if (isHost) {
                if (x - RADIUS <= 0)
                    isAlive = false; // Explota en pared izquierda
                if (x > WIDTH)
                    isAlive = false; // Se va por la red
            } else {
                if (x + RADIUS >= WIDTH)
                    isAlive = false; // Explota en pared derecha
                if (x < 0)
                    isAlive = false; // Se va por la red
            }
        }
    }

    public void render(GraphicsContext gc) {
        if (type.equals("sniper")) {
            gc.setFill(Color.CYAN);
            // Efecto visual de bala alargada para el francotirador
            gc.fillOval(x - RADIUS, y - RADIUS, RADIUS * 2, RADIUS * 2);
        } else {
            gc.setFill(Color.ORANGE);
            gc.fillOval(x - RADIUS, y - RADIUS, RADIUS * 2, RADIUS * 2);
        }
    }

    public void bounceHorizontal() {
        this.velX = -this.velX;
    }

    // --- LOS MÉTODOS GET (INDISPENSABLES PARA LA RED) ---
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getAngle() {
        return angle;
    }

    public double getPower() {
        return power;
    }

    public String getType() {
        return type;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public boolean isEnemy() {
        return isEnemy;
    }
}