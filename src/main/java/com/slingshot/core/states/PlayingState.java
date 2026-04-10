package com.slingshot.core.states;

import com.slingshot.core.GameEngine;

public class PlayingState implements GameState {

    public PlayingState() {
        System.out.println("\n===========================================");
        System.out.println(" 🎮 ¡PARTIDA INICIADA! FÍSICAS ACTIVADAS 🎮");
        System.out.println("===========================================\n");
    }

    @Override
    public void handleNetworkMessage(String command, String[] tokens, GameEngine engine) {
        // Aquí SÍ aceptamos disparos y movimientos
        if (command.equals("ACTION_SHOOT") || command.equals("TURN_END") || command.equals("ACTION_MOVE")) {
            System.out.println("[State: Playing] -> Acción de batalla procesada: " + command);
        } else if (command.equals("HANDSHAKE_OK") || command.equals("SETUP_PC1")) {
            System.out.println("[State: Playing] -> IGNORADO: Ya estamos jugando, no se aceptan comandos de configuración.");
        }
    }
}