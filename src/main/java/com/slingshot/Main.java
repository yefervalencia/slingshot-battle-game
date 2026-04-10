package com.slingshot;

import com.slingshot.network.UDPManager;
import com.slingshot.network.NetworkObserver;
import com.slingshot.network.NetworkProtocol;
import com.slingshot.core.GameEngine;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== INICIANDO PRUEBA DE PATRON STATE ===");

        // 1. Iniciamos el motor del juego para la máquina local (Arranca en HandshakeState)
        GameEngine localEngine = new GameEngine();

        UDPManager pc1Manager = new UDPManager();
        UDPManager pc2Manager = new UDPManager();

        // 2. Conectamos la red con el Motor usando el Observer
        pc2Manager.addObserver(new NetworkObserver() {
            @Override
            public void onMessageReceived(String message) {
                // Pasamos el mensaje al protocolo y el protocolo avisa al motor
                NetworkProtocol.processMessage(message, localEngine);
            }
        });

        pc1Manager.startListening(5000);
        pc2Manager.startListening(5001);
        try { Thread.sleep(500); } catch (InterruptedException e) {}

        // --- PRUEBAS ---
        System.out.println("\n[Main-Thread] 1. Un hacker intenta disparar ANTES de conectar...");
        String hackMsg = NetworkProtocol.formatShoot("Artillery", 45, 90);
        pc1Manager.send(hackMsg, "127.0.0.1", 5001);
        try { Thread.sleep(200); } catch (InterruptedException e) {}

        System.out.println("\n[Main-Thread] 2. Ahora sí, hacemos el Handshake legal...");
        pc1Manager.send("HANDSHAKE_OK", "127.0.0.1", 5001);
        try { Thread.sleep(200); } catch (InterruptedException e) {}

        System.out.println("\n[Main-Thread] 3. PC1 envía su configuración de Setup...");
        String setupMsg = NetworkProtocol.formatSetupPC1("Desert", "Sniper", "Yeferson");
        pc1Manager.send(setupMsg, "127.0.0.1", 5001);
        try { Thread.sleep(500); } catch (InterruptedException e) {}

        pc1Manager.close(); pc2Manager.close();
        System.exit(0);
    }
}