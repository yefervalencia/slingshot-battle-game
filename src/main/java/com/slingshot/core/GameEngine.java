package com.slingshot.core;

import com.slingshot.core.states.GameState;
import com.slingshot.core.states.HandshakeState;

public class GameEngine {
    private GameState currentState;

    public GameEngine() {
        // Al encender el motor, el estado INICIAL obligatorio es el Handshake
        System.out.println("[GameEngine] -> Motor iniciado.");
        this.setState(new HandshakeState());
    }

    // Método para cambiar de estado
    public void setState(GameState newState) {
        this.currentState = newState;
        System.out.println("[GameEngine] -> *** CAMBIO DE ESTADO A: " + newState.getClass().getSimpleName() + " ***");
    }

    // El motor NO procesa la red, delega el trabajo al Estado Actual
    public void processNetworkMessage(String command, String[] tokens) {
        currentState.handleNetworkMessage(command, tokens, this);
    }
}