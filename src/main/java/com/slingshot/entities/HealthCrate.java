package com.slingshot.entities;

import javafx.scene.paint.Color;

public class HealthCrate extends Crate {
    public HealthCrate(double x, double y) {
        super(x, y);
    }

    @Override
    protected Color getColor() {
        return Color.LIGHTGREEN;
    }

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