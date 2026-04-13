package com.slingshot.entities;
import javafx.scene.paint.Color;

public class DoubleScoreCrate extends Crate {
    public DoubleScoreCrate(double x, double y) { super(x, y); }

    @Override
    protected Color getColor() { return Color.PURPLE; } // Color morado/épico

    @Override
    public void onHitByBullet(Player player, Projectile bullet) {
        player.addScore(20); // Otorga el doble de puntos de una vez
        this.destroy();
    }
}