package com.slingshot.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Barrier {
  private double x, y;
  private final double size = 45.0; // Un poco más grande que las cajas
  private int health = 4; // Sniper quita 1, Artillería quita 2

  public Barrier(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public void takeDamage(String projectileType) {
    if (projectileType.equals("sniper"))
      health -= 1;
    else if (projectileType.equals("artillery"))
      health -= 2;
  }

  public boolean isAlive() {
    return health > 0;
  }

  public void render(GraphicsContext gc) {
    // Visualmente se vuelve más oscura/transparente al dañarse
    double opacity = health / 4.0;
    gc.setFill(Color.rgb(52, 152, 219, opacity)); // Un azul translúcido
    gc.fillRect(x, y, size, size);

    // Borde reforzado
    gc.setStroke(Color.WHITE);
    gc.setLineWidth(health); // El borde se adelgaza al dañarse
    gc.strokeRect(x, y, size, size);
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public double getSize() {
    return size;
  }
}