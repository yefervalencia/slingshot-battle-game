package com.slingshot.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
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

    Image crateImage = getImage(); // Obtenemos la imagen de la clase hija

    if (crateImage != null) {
      gc.drawImage(crateImage, x, y, size, size);
    } else {
      // Fallback por si la imagen no carga
      gc.setFill(getColor());
      gc.fillRect(x, y, size, size);
      gc.setStroke(Color.BLACK);
      gc.strokeRect(x, y, size, size);
    }
  }

  // MÉTODOS ABSTRACTOS PARA LAS HIJAS
  protected abstract Color getColor();

  protected abstract Image getImage(); // ¡NUEVO!

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

  public void setX(double x) {
    this.x = x;
  }

  public void setY(double y) {
    this.y = y;
  }
}