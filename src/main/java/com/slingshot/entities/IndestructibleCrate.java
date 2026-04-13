package com.slingshot.entities;

import javafx.scene.paint.Color;

public class IndestructibleCrate extends Crate {
    public IndestructibleCrate(double x, double y) {
        super(x, y);
    }

    @Override
    protected Color getColor() {
        return Color.DARKGRAY;
    } // Metal

    @Override
    public boolean onHitByBullet(Player player, Projectile bullet) {
        if (bullet.getType().equals("sniper")) {
            bullet.bounceHorizontal();
            return false; // ¡IMPORTANTE! La bala NO se destruye, rebota.
        }
        return true; // La artillería sí explota al chocar
    }
}