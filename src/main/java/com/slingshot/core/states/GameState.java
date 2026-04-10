package com.slingshot.core.states;

import com.slingshot.core.GameEngine;

public interface GameState {
    /**
     * @param command El comando principal (ej: "HANDSHAKE_OK", "ACTION_SHOOT")
     * @param tokens  El arreglo completo con los parámetros
     * @param engine  La referencia al motor para poder cambiar de estado
     */
    void handleNetworkMessage(String command, String[] tokens, GameEngine engine);
}