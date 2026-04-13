package com.slingshot.entities;
import javafx.scene.paint.Color;

public class IndestructibleCrate extends Crate {
    public IndestructibleCrate(double x, double y) { super(x, y); }

    @Override
    protected Color getColor() { return Color.DARKGRAY; } // Metal

    @Override
    public void onHitByBullet(Player player, Projectile bullet) {
        // NO llamamos a this.destroy(). Por ende, sobrevive.
        // NO suma puntos.
        
        // Regla: La bala de francotirador rebota (invertimos su vector X). 
        // La artillería simplemente choca y explota en GameWindow.
        if (bullet.getType().equals("sniper")) {
            bullet.bounceHorizontal(); 
        }
    }
}