package com.slingshot.entities;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class DoubleScoreCrate extends Crate {
    public DoubleScoreCrate(double x, double y) {
        super(x, y);
    }

    private static Image img;
    static {
        try {
            img = new Image(EmptyCrate.class.getResourceAsStream("/assets/crate_double.png"));
        } catch (Exception e) {
            System.err.println("No se encontro crate_double.png");
        }
    }

    @Override
    protected Color getColor() {
        return Color.PURPLE;
    } // Color morado/épico

    @Override
    protected Image getImage() {
        return img;
    } // ¡Retornamos la imagen!

    @Override
    public boolean onHitByBullet(Player player, Projectile bullet) {
        if (player != null) {
            player.activateDoubleScore(8); // Activa por 8 segundos
            player.addScore(10);
        }
        this.destroy();
        return true; // La bala se destruye
    }
}