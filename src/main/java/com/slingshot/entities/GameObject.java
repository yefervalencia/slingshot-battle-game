package com.slingshot.entities;

import java.awt.Graphics;
import java.awt.Rectangle;

public abstract class GameObject {
    protected int x, y, width, height;
    protected Rectangle hitbox;
    protected boolean active;
    protected boolean isCollidable;

    public GameObject(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.hitbox = new Rectangle(x, y, width, height);
        this.active = true;
        this.isCollidable = true;
    }

    // Métodos abstractos que obligan a las hijas a definirlos
    public abstract void update();
    public abstract void draw(Graphics g);

    // Getters y Setters básicos
    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Rectangle getHitbox() { return hitbox; }
}