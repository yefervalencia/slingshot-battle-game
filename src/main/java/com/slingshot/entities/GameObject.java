package com.slingshot.entities;

import java.awt.Graphics;
import java.awt.Rectangle;

public abstract class GameObject {
    protected String id;
    protected float x, y;
    protected int width, height;
    protected Rectangle hitbox;
    protected boolean active;
    protected boolean isCollidable;

    public GameObject(String id, float x, float y, int width, int height) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.hitbox = new Rectangle((int) x, (int) y, width, height);
        this.active = true;
        this.isCollidable = true;
    }

    protected void updateHitbox() {
        this.hitbox.setLocation((int) x, (int) y);
    }

    public void heal(int amount) {
        // Lo implementa Player, aquí es base
    }

    // Métodos abstractos que obligan a las hijas a definirlos
    public abstract void update();

    public abstract void draw(Graphics g);

    // Getters
    public String getId() {
        return id;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isCollidable() {
        return isCollidable;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    // Setters
    public void setActive(boolean active) {
        this.active = active;
    }

    public void setCollidable(boolean collidable) {
        this.isCollidable = collidable;
    }

    public void setX(float x) {
        this.x = x;
        updateHitbox();
    }

    public void setY(float y) {
        this.y = y;
        updateHitbox();
    }
}