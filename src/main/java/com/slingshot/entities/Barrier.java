package com.slingshot.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Barrier {
  private double x, y;
  // Ahora es más delgada en X y alargada en Y
  private final double width = 20.0;
  private final double height = 80.0;
  private int health = 4;

  // Aquí cargaremos la imagen cuando la tengamos
  public static Image barrierImage = null;

  private static Image img;
  static {
    try {
      img = new Image(Barrier.class.getResourceAsStream("/assets/barrier.png"));
    } catch (Exception e) {
    }
  }

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
    double opacity = health / 4.0;
    gc.setGlobalAlpha(opacity);

    if (img != null) {
      gc.drawImage(img, x, y, width, height); // La estiramos en Y
    } else {
      gc.setFill(Color.rgb(52, 152, 219));
      gc.fillRect(x, y, width, height);
    }
    gc.setGlobalAlpha(1.0);
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public double getWidth() {
    return width;
  } // ¡NUEVO!

  public double getHeight() {
    return height;
  } // ¡NUEVO!
}