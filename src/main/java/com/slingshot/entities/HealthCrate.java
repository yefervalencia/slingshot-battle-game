package com.slingshot.entities;
import javafx.scene.paint.Color;

public class HealthCrate extends Crate {
    public HealthCrate(double x, double y) { super(x, y); }

    @Override
    protected Color getColor() { return Color.LIGHTGREEN; }

    @Override
    public void onHitByBullet(Player player, Projectile bullet) {
        player.addScore(10);     // Da puntos
        player.addLife();     
        this.destroy();          // Se destruye
    }
}