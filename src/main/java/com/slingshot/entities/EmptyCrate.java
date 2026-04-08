package com.slingshot.entities;

public class EmptyCrate extends CrateType {
    public EmptyCrate() { 
        super("EMPTY", 1); // 1 de vida, se destruye fácil
    }

    @Override
    public void applyEffect(Player player) {
        // Según tu regla: Da 10 puntos de puntaje
        player.setScore(player.getScore() + 10);
        System.out.println("Caja vacía: +10 puntos para " + player.getUsername());
    }
}