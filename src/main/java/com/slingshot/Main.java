package com.slingshot;

import com.slingshot.network.UDPManager;
import com.slingshot.network.NetworkObserver;
import com.slingshot.network.NetworkProtocol;
import com.slingshot.core.GameEngine;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== SIMULACIÓN COMPLETA: HANDSHAKE -> SETUP -> PLAYING ===");

        GameEngine localEngine = new GameEngine(); // Arranca en Handshake
        UDPManager pc1Manager = new UDPManager();
        UDPManager pc2Manager = new UDPManager();

        pc2Manager.addObserver(message -> NetworkProtocol.processMessage(message, localEngine));

        pc1Manager.startListening(5000);
        pc2Manager.startListening(5001);
        try { Thread.sleep(500); } catch (InterruptedException e) {}

        // 1. Conexión
        System.out.println("\n[Main] -> 1. PC1 envía Handshake");
        pc1Manager.send("HANDSHAKE_OK", "127.0.0.1", 5001);
        try { Thread.sleep(200); } catch (InterruptedException e) {}

        // 2. Setup PC1
        System.out.println("\n[Main] -> 2. PC1 elige Mapa (Desert) y Personaje (Sniper)");
        pc1Manager.send("SETUP_PC1;Desert_Map;Sniper;Yeferson_Host", "127.0.0.1", 5001);
        try { Thread.sleep(200); } catch (InterruptedException e) {}

        // 3. Intento de trampa
        System.out.println("\n[Main] -> 3. PC1 intenta disparar por error/hack ANTES de que PC2 esté listo");
        pc1Manager.send("ACTION_SHOOT;Artillery;45;100", "127.0.0.1", 5001);
        try { Thread.sleep(200); } catch (InterruptedException e) {}

        // 4. Orden final
        System.out.println("\n[Main] -> 4. PC1 envía GAME_START");
        pc1Manager.send("GAME_START", "127.0.0.1", 5001);
        try { Thread.sleep(200); } catch (InterruptedException e) {}

        // 5. ¡A jugar!
        System.out.println("\n[Main] -> 5. AHORA SÍ, PC1 dispara");
        pc1Manager.send("ACTION_SHOOT;Artillery;45;100", "127.0.0.1", 5001);
        try { Thread.sleep(500); } catch (InterruptedException e) {}

        pc1Manager.close(); pc2Manager.close();
        System.exit(0);
    }
}