package com.slingshot.entities;

public class IndestructibleCrate extends CrateType {
    public IndestructibleCrate() { 
        // 999 de vida o simplemente ignoramos el daño en el método
        super("INDESTRUCTIBLE", 999); 
    }

    @Override
    public void applyEffect(Player player) {
        // No hace nada al jugador, su función es física
        System.out.println("¡Impacto en muro indestructible! La bala debería rebotar.");
    }
}