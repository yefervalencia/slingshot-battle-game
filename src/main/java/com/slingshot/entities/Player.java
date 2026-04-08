package com.slingshot.entities;

import java.awt.Graphics;
import java.awt.Color;

public class Player extends GameObject {

  // Enums definidos según tu diagrama
  public enum Status {
    IDLE, MOVING, AIMING, LOCKED
  }

  public enum Side {
    LEFT, RIGHT
  }

  private String username;
  private int health, score, ammo;
  private int characterId;
  private Status status;
  private Side side;

  private Projectile currentProjectile;

  private float minX, maxX, minY, maxY;

  public Player(String id, String username, float x, float y, Side side) {
    super(id, x, y, 50, 50); // Tamaño base del jugador
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
    updateHitbox();
  }

  @Override
  public void draw(Graphics g) {
    // Dibujo temporal para pruebas
    g.setColor(side == Side.LEFT ? Color.BLUE : Color.RED);
    g.fillRect((int) x, (int) y, width, height);
    g.setColor(Color.WHITE);
    g.drawString(username, (int) x, (int) y - 25);
    g.drawString("HP:" + health + " SC:" + score + " AM:" + ammo,
        (int) x - 10, (int) y - 10);
  }

  // Métodos definidos en tu diagrama
  public void move(int dx, int dy) {
    if (status != Status.MOVING)
      return;

    float newX = this.x + dx;
    float newY = this.y + dy;

    // Restricción de zona (se configura desde GameEngine según Side)
    if (newX >= minX && newX + width <= maxX)
      this.x = newX;
    if (newY >= minY && newY + height <= maxY)
      this.y = newY;
  }

  public void takeDamage(int amount) {
    this.health = Math.max(0, this.health - amount);
    if (this.health == 0)
      this.active = false;
  }

  @Override
  public void heal(int amount) {
    this.health = Math.min(100, this.health + amount);
  }

  public boolean consumeAmmo() {
    if (this.ammo <= 0)
      return false;
    this.ammo--;
    return true;
  }

  public void setMovementBounds(float minX, float maxX, float minY, float maxY) {
    this.minX = minX;
    this.maxX = maxX;
    this.minY = minY;
    this.maxY = maxY;
  }

  public void refillAmmoIfEmpty() {
    if (this.ammo <= 0)
      this.ammo = 1;
  }

  // Getters y Setters
  public String getUsername() {
    return username;
  }

  public int getHealth() {
    return health;
  }

  public int getScore() {
    return score;
  }

  public int getAmmo() {
    return ammo;
  }

  public Status getStatus() {
    return status;
  }

  public Side getSide() {
    return side;
  }

  public Projectile getCurrentProjectile() {
    return currentProjectile;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public void setScore(int score) {
    this.score = score;
  }

  public void setAmmo(int ammo) {
    this.ammo = ammo;
  }

  public void setCurrentProjectile(Projectile p) {
    this.currentProjectile = p;
  }
}