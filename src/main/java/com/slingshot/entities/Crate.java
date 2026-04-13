package com.slingshot.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public abstract class Crate {
  protected double x, y;
  protected double size = 40.0;
  protected boolean isAlive = true;

  public Crate(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public void render(GraphicsContext gc) {
    if (!isAlive)
      return;
    gc.setFill(getColor());
    gc.fillRect(x, y, size, size);
    gc.setStroke(Color.BLACK);
    gc.strokeRect(x, y, size, size);
  }

  // MÉTODOS QUE CADA HIJA DEBE DEFINIR
  protected abstract Color getColor();

  // El método polimórfico: Cada caja decide qué hacer cuando le disparan
  public abstract boolean onHitByBullet(Player player, Projectile bullet);

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public double getSize() {
    return size;
  }

  public boolean isAlive() {
    return isAlive;
  }

  public void destroy() {
    this.isAlive = false;
  }
}