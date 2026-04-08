package com.slingshot.entities;

import java.awt.Color;
import java.awt.Graphics;

public class SupplyCrate extends GameObject {
    private boolean idCollected;
    private CrateType type; // Composición con la clase de arriba
    private boolean isEnemyCrate;

    public SupplyCrate(String id, int x, int y, CrateType type, boolean isEnemy) {
        super(id, (int)x, (int)y, 40, 40); // Tamaño de la caja
        this.type = type;
        this.isEnemyCrate = isEnemy;
        this.idCollected = false;
    }

    @Override
    public void update() {
        // Lógica de colisión o efectos visuales si flota
    }

    @Override
    public void draw(Graphics g) {
        if (!idCollected) {
            g.setColor(isEnemyCrate ? Color.ORANGE : Color.DARK_GRAY);
            g.fillRect((int)x, (int)y, width, height);
            g.setColor(Color.WHITE);
            g.drawString(type.rewardType.substring(0, 1), (int)x + 15, (int)y + 25);
        }
    }

    public void applyEffect(Player player) {
        if (!idCollected) {
            type.applyEffect(player);
            idCollected = true;
            this.active = false; // Desaparece del mapa
        }
    }
}