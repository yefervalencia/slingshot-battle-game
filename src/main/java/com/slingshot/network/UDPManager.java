package com.slingshot.network;

import java.io.IOException;
import java.net.*;

public class UDPManager {
    private static UDPManager instance;
    private DatagramSocket socket;
    private NetworkObserver observer;
    private int remotePort;
    private InetAddress remoteAddress;

    private UDPManager() {
    }

    public static UDPManager getInstance() {
        if (instance == null)
            instance = new UDPManager();
        return instance;
    }

    public void initialize(int localPort, String remoteIP, int remotePort)
            throws SocketException, UnknownHostException {
        if (this.socket != null && !this.socket.isClosed()) {
            this.socket.close();
        }

        // Cambiamos el bind complejo por uno directo que Windows entiende mejor
        this.socket = new DatagramSocket(localPort); // Bind simple
        this.socket.setBroadcast(true);

        this.remoteAddress = InetAddress.getByName(remoteIP);
        this.remotePort = remotePort;

        System.out.println("[NET] Local Port: " + localPort);
        System.out.println("[NET] Remote Destination: " + remoteIP + ":" + remotePort);
        this.startListening();
    }

    public void setObserver(NetworkObserver observer) {
        this.observer = observer;
    }

    public void sendMessage(String message) {
        // Usamos el MISMO socket que ya está abierto para recibir (esto es clave en
        // P2P)
        new Thread(() -> {
            try {
                if (socket == null || socket.isClosed())
                    return;

                byte[] buffer = message.getBytes();
                DatagramPacket packet = new DatagramPacket(
                        buffer,
                        buffer.length,
                        remoteAddress,
                        remotePort);

                socket.send(packet);
                // Al usar el socket que ya hizo 'bind', Java se ve obligado a usar la misma red
                System.out.println("[SENT] -> " + message + " to " + remoteAddress + ":" + remotePort);

            } catch (IOException e) {
                System.err.println("[ERROR] Send failed: " + e.getMessage());
            }
        }).start();
    }

    private void startListening() {
        Thread listenThread = new Thread(() -> {
            try {
                byte[] buffer = new byte[1024];
                System.out.println("[DEBUG] Listener thread started on port: " + socket.getLocalPort());

                while (!socket.isClosed()) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet); // Este es el punto donde el programa espera

                    String message = new String(packet.getData(), 0, packet.getLength());
                    System.out.println("\n[DEBUG] PACKET ARRIVED: " + message);

                    if (observer != null) {
                        observer.onMessageReceived(message);
                    }
                }
            } catch (IOException e) {
                if (!socket.isClosed()) {
                    System.err.println("[ERROR] Listener error: " + e.getMessage());
                }
            }
        });
        listenThread.setDaemon(true); // Para que el hilo muera si cierras el programa
        listenThread.start();
    }
}