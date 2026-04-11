package com.slingshot.core.states;

import com.slingshot.core.GameEngine;

public class SetupState implements GameState {

    @Override
    public void handleNetworkMessage(String command, String[] tokens, GameEngine engine) {
        switch (command) {
            case "SETUP_PC1":
                // Escenario: Somos PC2 y recibimos las opciones del PC1
                String mapName = tokens[1];
                String pc1Char = tokens[2];
                String pc1Name = tokens[3];
                System.out.println("[State: Setup] -> Opciones del HOST recibidas. Mapa: " + mapName + " | Rival: " + pc1Name + " (" + pc1Char + ")");
                System.out.println("[State: Setup] -> INFO: Desbloqueando GUI local para elegir nuestro personaje...");
                break;

            case "READY_PC2":
                // Escenario: Somos PC1 (Host) y el PC2 nos confirma que ya eligió
                String pc2Char = tokens[1];
                String pc2Name = tokens[2];
                System.out.println("[State: Setup] -> El CLIENTE (" + pc2Name + ") eligió a " + pc2Char + " y está listo.");
                System.out.println("[State: Setup] -> INFO: Dando la orden de inicio global...");
                
                // 1. Enviamos el token oficial a la red para que el Cliente avance
                engine.sendNetworkMessage("GAME_START");
                
                // 2. Nosotros también avanzamos al juego
                engine.setState(new PlayingState());
                break;

            case "GAME_START":
                // Escenario: Somos PC2 y PC1 nos da la orden oficial de empezar
                System.out.println("[State: Setup] -> ¡Orden de inicio recibida del HOST! Entrando al campo de batalla.");
                engine.setState(new PlayingState());
                break;

            case "ACTION_SHOOT":
            case "ACTION_MOVE":
            case "ACTION_BUILD":
                // Regla estricta: Nadie dispara ni construye mientras estamos en la sala de espera
                System.out.println("[State: Setup] -> BLOQUEADO: Intento de acción ('" + command + "') ignorado. El juego no ha empezado.");
                break;

            default:
                System.err.println("[State: Setup] -> ADVERTENCIA: Comando desconocido en fase Setup: " + command);
                break;
        }
    }
}