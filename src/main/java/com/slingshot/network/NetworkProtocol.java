package com.slingshot.network;

import com.slingshot.core.GameEngine;

public class NetworkProtocol {
    private static final String SEPARATOR = ";";

    public static String formatSetupPC1(String mapName, String charId, String userName) {
        return "SETUP_PC1" + SEPARATOR + mapName + SEPARATOR + charId + SEPARATOR + userName;
    }

    public static String formatShoot(String type, double angle, double power) {
        return "ACTION_SHOOT" + SEPARATOR + type + SEPARATOR + angle + SEPARATOR + power;
    }

    public static String formatTurnEnd(String nextPlayer) {
        return "TURN_END" + SEPARATOR + nextPlayer;
    }

    public static String formatProjectile(String type, double y, double angle, double power) {
        // Formato: BULLET;tipo;y_entrada;angulo;potencia
        return String.format("BULLET;%s;%.2f;%.2f;%.2f", type, y, angle, power);
    }

    public static void processMessage(String rawMessage, GameEngine engine) {
        if (rawMessage == null || rawMessage.trim().isEmpty()) {
            return;
        }

        // 1. Partimos el mensaje
        String[] tokens = rawMessage.split(SEPARATOR);
        String command = tokens[0];

        try {
            // 2. MAGIA DE SOLID: El protocolo ya NO hace un switch.
            // Solo le avisa al motor que llegó algo, y el Motor (a través de su Estado)
            // decide qué hacer.
            engine.processNetworkMessage(command, tokens);

        } catch (Exception e) {
            System.err.println("[Protocolo] -> ERROR GRAVE: Paquete mal formado: " + rawMessage);
        }
    }
}