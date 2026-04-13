package com.slingshot.entities;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class AmmoCrate extends Crate {
    public AmmoCrate(double x, double y) {
        super(x, y);
    }

    private static Image img;
    static {
        try {
            img = new Image(EmptyCrate.class.getResourceAsStream("/assets/crate_ammo.png"));
        } catch (Exception e) {
            System.err.println("No se encontro crate_ammo.png");
        }
    }

    @Override
    protected Image getImage() {
        return img;
    } // ¡Retornamos la imagen!

    @Override
    protected Color getColor() {
        return Color.GOLD;
    } // Llamativa

    @Override
    public boolean onHitByBullet(Player player, Projectile bullet) {
        if (player != null) {
            player.addScore(10);
            player.addAmmo(5); // Te regala 5 balas
        }
        this.destroy();
        return true;
    }
}