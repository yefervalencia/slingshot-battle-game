package com.slingshot;

import com.slingshot.network.UDPManager;
import com.slingshot.network.NetworkObserver;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== INICIANDO PRUEBA AISLADA DE RED (UDP P2P) ===");

        // 1. Instanciamos dos managers (simulando PC1 y PC2 en la misma máquina)
        UDPManager pc1Manager = new UDPManager();
        UDPManager pc2Manager = new UDPManager();

        // 2. Aplicamos el Patrón Observer a PC2
        // Simulamos que el "GameEngine" o la "GUI" del PC2 está escuchando los eventos de red
        pc2Manager.addObserver(new NetworkObserver() {
            @Override
            public void onMessageReceived(String message) {
                // Si esto se imprime, significa que el hilo secundario recibió el paquete
                // y logró pasarlo correctamente a la capa superior (nuestro sistema).
                System.out.println("[PC2-Observer] EXITO: Mensaje capturado de la red -> '" + message + "'");
            }
        });

        // 3. Iniciamos los puertos de escucha (arrancan los hilos while-running)
        pc1Manager.startListening(5000); // PC1 escucha en puerto 5000
        pc2Manager.startListening(5001); // PC2 escucha en puerto 5001

        // Pausa táctica de 500ms. 
        // Como los Sockets levantan en hilos paralelos (Threads), les damos tiempo para 
        // abrir los puertos en el Sistema Operativo antes de disparar paquetes.
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 4. PC1 envía un mensaje de prueba a PC2
        System.out.println("\n[Main-Thread] Ordenando a PC1 que envíe un paquete a PC2...");
        // Como ambos están en la misma máquina, usamos localhost (127.0.0.1)
        pc1Manager.send("HANDSHAKE_OK;Soy_El_Master", "127.0.0.1", 5001);

        // 5. Mantenemos el programa vivo unos segundos para observar el asincronismo y luego limpiamos
        try {
            Thread.sleep(2000);
            System.out.println("\n=== CERRANDO CONEXIONES Y APAGANDO ===");
            pc1Manager.close();
            pc2Manager.close();
            
            // Pausa breve para ver los mensajes de cierre en consola
            Thread.sleep(500); 
            System.exit(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}