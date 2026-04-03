package com.slingshot.entities;

import java.awt.Graphics;
import java.awt.Color;

public class Player extends GameObject {
    
    // Enums definidos según tu diagrama
    public enum Status { IDLE, MOVING, AIMING, LOCKED }
    public enum Side { LEFT, RIGHT }

    private String username;
    private int health, score, ammo;
    private int characterId;
    private Status status;
    private Side side;
    
    // En tu diagrama dijiste que currentStrategy es de tipo Projectile
    private Projectile currentStrategy;

    public Player(String username, int x, int y, Side side) {
        super(x, y, 50, 50); // Tamaño base del jugador
        this.username = username;
        this.health = 100;
        this.score = 0;
        this.ammo = 10;
        this.status = Status.IDLE;
        this.side = side;
    }

    @Override
    public void update() {
        // Actualizar la hitbox si el jugador se mueve
        this.hitbox.setLocation(x, y);
    }

    @Override
    public void draw(Graphics g) {
        // Dibujo temporal para pruebas
        g.setColor(side == Side.LEFT ? Color.BLUE : Color.RED);
        g.fillRect(x, y, width, height);
        g.setColor(Color.BLACK);
        g.drawString(username + " (" + health + "%)", x, y - 10);
    }

    // Métodos definidos en tu diagrama
    public void move(int dx, int dy) {
        if (status == Status.MOVING) {
            this.x += dx;
            this.y += dy;
        }
    }

    public void takeDamage(int amount) {
        this.health -= amount;
        if (this.health <= 0) {
            this.health = 0;
            this.active = false;
        }
    }

    // Getters y Setters específicos
    public void setStatus(Status status) { this.status = status; }
    public Status getStatus() { return status; }
}