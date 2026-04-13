package com.slingshot.entities;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class EmptyCrate extends Crate {
    public EmptyCrate(double x, double y) {
        super(x, y);
    }

    private static Image img;
    static {
        try {
            img = new Image(EmptyCrate.class.getResourceAsStream("/assets/crate_empty.png"));
        } catch (Exception e) {
            System.err.println("No se encontro crate_empty.png");
        }
    }

    @Override
    protected Color getColor() {
        return Color.SADDLEBROWN;
    } // Color madera

    @Override
    protected Image getImage() {
        return img;
    } // ¡Retornamos la imagen!

    @Override
    public boolean onHitByBullet(Player player, Projectile bullet) {
        if (player != null) {
            player.addScore(10); // Otorga los 10 puntos base

        }
        this.destroy();
        return true; // Se destruye
    }
}