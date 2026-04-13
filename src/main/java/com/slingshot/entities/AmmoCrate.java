package com.slingshot.entities;
import javafx.scene.paint.Color;

public class AmmoCrate extends Crate {
    public AmmoCrate(double x, double y) { super(x, y); }

    @Override
    protected Color getColor() { return Color.GOLD; } // Llamativa

    @Override
    public boolean onHitByBullet(Player player, Projectile bullet) {
        player.addScore(10);
        player.addAmmo(5);   // Te regala 5 balas
        return true;
    }
}