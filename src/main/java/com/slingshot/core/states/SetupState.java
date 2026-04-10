package com.slingshot.core.states;
import com.slingshot.core.GameEngine;

public class SetupState implements GameState {
    @Override
    public void handleNetworkMessage(String command, String[] tokens, GameEngine engine) {
        System.out.println("[State: Setup] -> Analizando opciones del rival...");
    }
}