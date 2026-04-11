package com.slingshot.core;

import com.slingshot.core.states.GameState;
import com.slingshot.core.states.HandshakeState;
import java.util.function.Consumer;

public class GameEngine {
    private GameState currentState;
    private Consumer<GameState> onStateChangeListener;
    
    // --- NUEVO: Puente para enviar datos a la red ---
    private Consumer<String> networkSender;

    public GameEngine() {
        System.out.println("[GameEngine] -> Motor iniciado.");
        this.setState(new HandshakeState());
    }

    // --- NUEVO: Setter y método para enviar ---
    public void setNetworkSender(Consumer<String> networkSender) {
        this.networkSender = networkSender;
    }

    public void sendNetworkMessage(String message) {
        if (networkSender != null) {
            networkSender.accept(message);
        }
    }

    public void setState(GameState newState) {
        this.currentState = newState;
        System.out.println("[GameEngine] -> *** CAMBIO DE ESTADO A: " + newState.getClass().getSimpleName() + " ***");
        if (onStateChangeListener != null) {
            onStateChangeListener.accept(newState);
        }
    }

    public void setOnStateChangeListener(Consumer<GameState> listener) {
        this.onStateChangeListener = listener;
    }
    
    public void processNetworkMessage(String command, String[] tokens) {
        currentState.handleNetworkMessage(command, tokens, this);
    }
}