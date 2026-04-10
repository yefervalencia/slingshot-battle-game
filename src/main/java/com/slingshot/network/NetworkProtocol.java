package com.slingshot.network;

public class NetworkProtocol {
    // Definimos el separador oficial de nuestro protocolo maestro
    private static final String SEPARATOR = ";";

    /**
     * TOMA LOS DATOS DEL JUEGO Y LOS CONVIERTE EN STRING PARA RED
     */
    public static String formatSetupPC1(String mapName, String charId, String userName) {
        return "SETUP_PC1" + SEPARATOR + mapName + SEPARATOR + charId + SEPARATOR + userName;
    }

    public static String formatShoot(String type, double angle, double power) {
        return "ACTION_SHOOT" + SEPARATOR + type + SEPARATOR + angle + SEPARATOR + power;
    }

    public static String formatTurnEnd(String nextPlayer) {
        return "TURN_END" + SEPARATOR + nextPlayer;
    }

    /**
     * TOMA EL STRING DE RED Y LO TRADUCE A LOGICA DE JUEGO (Parseo)
     */
    public static void processMessage(String rawMessage) {
        if (rawMessage == null || rawMessage.trim().isEmpty()) {
            return;
        }

        // Partimos el mensaje usando el separador
        String[] tokens = rawMessage.split(SEPARATOR);
        String command = tokens[0]; // El primer elemento SIEMPRE es el comando

        try {
            switch (command) {
                case "HANDSHAKE_OK":
                    System.out.println("[Protocolo] -> ÉXITO: Handshake recibido. El rival está conectado.");
                    // TODO: Avisar al GameEngine que cambie a estado SETUP
                    break;

                case "SETUP_PC1":
                    String mapName = tokens[1];
                    String charId = tokens[2];
                    String userName = tokens[3];
                    System.out.println("[Protocolo] -> CONFIGURACIÓN: Mapa=" + mapName + ", Personaje=" + charId + ", Rival=" + userName);
                    // TODO: Actualizar la GUI y preparar el motor de juego
                    break;

                case "ACTION_SHOOT":
                    String type = tokens[1];
                    double angle = Double.parseDouble(tokens[2]);
                    double power = Double.parseDouble(tokens[3]);
                    System.out.println("[Protocolo] -> ATAQUE: Tipo=" + type + ", Ángulo=" + angle + ", Fuerza=" + power);
                    // TODO: Instanciar un proyectil en el GameEngine
                    break;

                case "TURN_END":
                    String nextPlayer = tokens[1];
                    System.out.println("[Protocolo] -> CAMBIO DE TURNO: Ahora juega " + nextPlayer);
                    // TODO: Desbloquear el input si es nuestro turno
                    break;

                default:
                    System.err.println("[Protocolo] -> ADVERTENCIA: Comando desconocido: " + command);
                    break;
            }
        } catch (Exception e) {
            // Previene que un paquete malicioso o corrupto (ej: letras donde van números) crashee el juego
            System.err.println("[Protocolo] -> ERROR GRAVE: Paquete mal formado: " + rawMessage);
        }
    }
}