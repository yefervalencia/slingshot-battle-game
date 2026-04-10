package com.slingshot;

import com.slingshot.network.UDPManager;
import com.slingshot.network.NetworkObserver;
import com.slingshot.network.NetworkProtocol;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== INICIANDO PRUEBA DE PROTOCOLO DE RED ===");

        UDPManager pc1Manager = new UDPManager();
        UDPManager pc2Manager = new UDPManager();

        // CONEXIÓN CLAVE: El Observer ahora le pasa el trabajo pesado al Protocolo
        pc2Manager.addObserver(new NetworkObserver() {
            @Override
            public void onMessageReceived(String message) {
                // Pasamos el string crudo al "traductor"
                NetworkProtocol.processMessage(message);
            }
        });

        pc1Manager.startListening(5000);
        pc2Manager.startListening(5001);

        try { Thread.sleep(500); } catch (InterruptedException e) {}

        // --- BATERÍA DE PRUEBAS ---
        System.out.println("\n[Main-Thread] 1. Simulando Handshake...");
        pc1Manager.send("HANDSHAKE_OK", "127.0.0.1", 5001);
        try { Thread.sleep(200); } catch (InterruptedException e) {}

        System.out.println("\n[Main-Thread] 2. Simulando Setup inicial de PC1...");
        // Usamos nuestro propio formateador para no equivocarnos con los punto y coma
        String setupMsg = NetworkProtocol.formatSetupPC1("Desert_Map", "Sniper", "IngenieroJefe");
        pc1Manager.send(setupMsg, "127.0.0.1", 5001);
        try { Thread.sleep(200); } catch (InterruptedException e) {}

        System.out.println("\n[Main-Thread] 3. Simulando Disparo de Artillería...");
        String shootMsg = NetworkProtocol.formatShoot("Artillery", 65.5, 90.0);
        pc1Manager.send(shootMsg, "127.0.0.1", 5001);
        try { Thread.sleep(200); } catch (InterruptedException e) {}

        System.out.println("\n[Main-Thread] 4. Simulando un paquete corrupto (Hacker/Lag)...");
        // Mandamos texto en lugar de números para probar la robustez del catch
        pc1Manager.send("ACTION_SHOOT;Artillery;TextoRaro;90.0", "127.0.0.1", 5001);
        try { Thread.sleep(500); } catch (InterruptedException e) {}


        System.out.println("\n=== CERRANDO PRUEBA ===");
        pc1Manager.close();
        pc2Manager.close();
        
        try { Thread.sleep(500); System.exit(0); } catch (InterruptedException e) {}
    }
}