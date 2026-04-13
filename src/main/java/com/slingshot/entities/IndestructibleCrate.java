package com.slingshot.entities;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class IndestructibleCrate extends Crate {
    public IndestructibleCrate(double x, double y) {
        super(x, y);
    }

    private static Image img;
    static {
        try {
            img = new Image(EmptyCrate.class.getResourceAsStream("/assets/crate_metal.png"));
        } catch (Exception e) {
            System.err.println("No se encontro crate_metal.png");
        }
    }

    @Override
    protected Color getColor() {
        return Color.DARKGRAY;
    } // Metal

    @Override
    protected Image getImage() {
        return img;
    } // ¡Retornamos la imagen!

    @Override
    public boolean onHitByBullet(Player player, Projectile bullet) {
        if (bullet.getType().equals("sniper")) {
            bullet.bounceHorizontal();
            return false; // ¡IMPORTANTE! La bala NO se destruye, rebota.
        }
        return true; // La artillería sí explota al chocar
    }
}