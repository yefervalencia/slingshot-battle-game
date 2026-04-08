package com.slingshot.entities;

class HealthCrate extends CrateType {
    public HealthCrate() { super("HEALTH", 1); }
    @Override
    public void applyEffect(Player player) {
        player.takeDamage(-20); // Curar 20 de vida
        System.out.println("Salud restaurada para " + player.getUsername());
    }
}
