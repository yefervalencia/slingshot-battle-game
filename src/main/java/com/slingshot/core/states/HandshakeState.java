package com.slingshot.core.states;

import com.slingshot.core.GameEngine;

public class HandshakeState implements GameState {

    @Override
    public void handleNetworkMessage(String command, String[] tokens, GameEngine engine) {
        if (command.equals("HANDSHAKE_OK")) {
            System.out.println("[State: Handshake] -> ¡Éxito! El otro jugador está listo.");
            System.out.println("[State: Handshake] -> Preparando la pantalla de selección de personajes...");
            
            // ¡TRANSICIÓN DE ESTADO! Pasamos al siguiente nivel del juego
            engine.setState(new SetupState()); 
        } else {
            // Regla estricta: Si nos mandan un disparo o movimiento ahora, lo ignoramos.
            System.out.println("[State: Handshake] -> BLOQUEADO: Se ignoró el comando '" + command + "' porque aún no estamos conectados.");
        }
    }
}