package com.slingshot.entities;

import javafx.scene.paint.Color;

public class EmptyCrate extends Crate {
    public EmptyCrate(double x, double y) {
        super(x, y);
    }

    @Override
    protected Color getColor() {
        return Color.SADDLEBROWN;
    } // Color madera

    @Override
    public boolean onHitByBullet(Player player, Projectile bullet) {
        if (player != null) {
            player.addScore(10); // Otorga los 10 puntos base
            this.destroy();
        }
        return true; // Se destruye
    }
}