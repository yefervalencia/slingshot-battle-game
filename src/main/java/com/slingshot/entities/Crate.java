package com.slingshot.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.Random;

public class Crate {
    private double x, y;
    private double size = 40.0;
    private boolean isAlive = true;
    private String type; 

    public Crate(double x, double y) {
        this.x = x;
        this.y = y;
        
        // 20% de probabilidad de ser una caja indestructible (gris)
        this.type = new Random().nextDouble() > 0.8 ? "indestructible" : "normal";
    }

    public void render(GraphicsContext gc) {
        if (!isAlive) return;

        // Color dependiendo del tipo
        if (type.equals("indestructible")) {
            gc.setFill(Color.DARKGRAY);
        } else {
            gc.setFill(Color.SADDLEBROWN); // Color madera
        }
        
        gc.fillRect(x, y, size, size);
        
        // Borde negro para que resalte
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRect(x, y, size, size);
    }

    // --- GETTERS & METODOS ---
    public double getX() { return x; }
    public double getY() { return y; }
    public double getSize() { return size; }
    public boolean isAlive() { return isAlive; }
    public String getType() { return type; }
    public void destroy() { isAlive = false; }
}