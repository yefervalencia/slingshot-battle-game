package com.slingshot.entities;

class AmmoCrate extends CrateType {
    public AmmoCrate() { super("AMMO", 1); }
    @Override
    public void applyEffect(Player player) {
        // // Asumiendo que añadiremos un método addAmmo en Player
        // player.setAmmo(player.getAmmo() + 5); 
        // System.out.println("Munición extra recolectada!");
    }
}