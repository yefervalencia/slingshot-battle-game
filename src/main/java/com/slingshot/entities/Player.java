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
    private int score = 0;
    private long doubleScoreEndTime = 0;

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
        if (input.isKeyPressed("W"))
            y -= SPEED;
        if (input.isKeyPressed("S"))
            y += SPEED;
        if (input.isKeyPressed("A"))
            x -= SPEED;
        if (input.isKeyPressed("D"))
            x += SPEED;

        // Colisiones con el mapa
        if (y < 0)
            y = 0;
        if (y > maxY - SIZE)
            y = maxY - SIZE;
        if (x < minX)
            x = minX;
        if (x > maxX - SIZE)
            x = maxX - SIZE;

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

        gc.save();
        gc.translate(centerX, centerY);
        gc.rotate(angle); // 1. Rotamos hacia el mouse

        // 2. MAGIA 2D: Si apuntamos a la izquierda, invertimos el eje Y para que no
        // quede de cabeza
        if (Math.abs(angle) > 90) {
            gc.scale(1, -1);
        }

        if (skin != null) {
            gc.drawImage(skin, -SIZE / 2, -SIZE / 2, SIZE, SIZE);
        } else {
            gc.setFill(Color.BLUE);
            gc.fillRect(-SIZE / 2, -SIZE / 2, SIZE, SIZE);
        }

        gc.restore();
    }

    // --- MÉTODOS DE DAÑO Y PUNTUACIÓN ---
    public void takeDamage() {
        this.lives--;
    }

    public void addScore(int points) {
        if (isDoubleScoreActive()) {
            this.score += (points * 2);
        } else {
            this.score += points;
        }
    }

    public int getScore() {
        return score;
    }

    public void addLife() {
        this.lives++;
    }

    public void addAmmo(int amount) {
        this.ammo += amount;
    }

    // --- SISTEMA DE COLISIÓN (Hitbox) ---
    // Verifica si la coordenada de la bala (px, py) está dentro del cuadrado del
    // jugador
    public boolean checkHit(double px, double py) {
        return px > x && px < x + SIZE && py > y && py < y + SIZE;
    }

    public void activateDoubleScore(int seconds) {
        this.doubleScoreEndTime = System.currentTimeMillis() + (seconds * 1000);
    }

    public boolean isDoubleScoreActive() {
        return System.currentTimeMillis() < doubleScoreEndTime;
    }

    public long getDoubleScoreTimeLeft() {
        return Math.max(0, (doubleScoreEndTime - System.currentTimeMillis()) / 1000);
    }

    public double getCenterX() {
        return x + SIZE / 2;
    }

    public double getCenterY() {
        return y + SIZE / 2;
    }

    public double getAngle() {
        return angle;
    }

    public int getLives() {
        return lives;
    }

    public int getAmmo() {
        return ammo;
    }

    public void reduceAmmo() {
        this.ammo--;
    }
}