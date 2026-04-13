package com.slingshot.entities;

import javafx.scene.paint.Color;

public class DoubleScoreCrate extends Crate {
    public DoubleScoreCrate(double x, double y) {
        super(x, y);
    }

    @Override
    protected Color getColor() {
        return Color.PURPLE;
    } // Color morado/épico

    @Override
    public boolean onHitByBullet(Player player, Projectile bullet) {
        player.activateDoubleScore(8); // Activa por 8 segundos
        player.addScore(10);
        this.destroy();
        return true; // La bala se destruye
    }
}