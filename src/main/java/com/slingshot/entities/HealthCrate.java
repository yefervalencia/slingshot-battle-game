package com.slingshot.entities;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class HealthCrate extends Crate {
    public HealthCrate(double x, double y) {
        super(x, y);
    }

    private static Image img;
    static {
        try {
            img = new Image(EmptyCrate.class.getResourceAsStream("/assets/crate_health.png"));
        } catch (Exception e) {
            System.err.println("No se encontro crate_health.png");
        }
    }

    @Override
    protected Color getColor() {
        return Color.LIGHTGREEN;
    }

    @Override
    protected Image getImage() {
        return img;
    } // ¡Retornamos la imagen!

    @Override
    public boolean onHitByBullet(Player player, Projectile bullet) {
        if (player != null) {
            player.addScore(10); // Da puntos
            player.addLife();
        }
        this.destroy();
        return true; // Se destruye
    }
}