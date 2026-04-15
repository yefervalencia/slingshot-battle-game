package com.slingshot.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class UDPManager implements Runnable {
    private DatagramSocket socket;
    private boolean isRunning;
    private Thread listenThread;

    private volatile boolean isListening = false;
    // Lista de observadores (Patrón Observer)
    private List<NetworkObserver> observers = new ArrayList<>();

    // 1. Métodos del Patrón Observer
    public void addObserver(NetworkObserver observer) {
        observers.add(observer);
    }

    private void notifyObservers(String message) {
        for (NetworkObserver obs : observers) {
            obs.onMessageReceived(message);
        }
    }

    // 2. Iniciar el servidor UDP en un Hilo separado
    public void startListening(int port) {
        // 1. Nos aseguramos de cerrar cualquier conexión previa limpia antes de abrir
        // otra
        stopListening();

        try {
            socket = new DatagramSocket(port);
            isListening = true;

            Thread listenThread = new Thread(() -> {
                byte[] buffer = new byte[1024];
                // El hilo solo corre si isListening es true y el socket no está cerrado
                while (isListening && socket != null && !socket.isClosed()) {
                    try {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet);
                        String msg = new String(packet.getData(), 0, packet.getLength());
                        notifyObservers(msg);
                    } catch (Exception e) {
                        // Es normal que lance excepción aquí cuando forzamos el socket.close()
                        if (isListening) {
                            System.err.println("Error en la escucha UDP: " + e.getMessage());
                        }
                    }
                }
            });
            listenThread.setDaemon(true);
            listenThread.start();
            System.out.println("[UDPManager] Escuchando limpiamente en el puerto " + port);

        } catch (Exception e) {
            System.err.println("Error al abrir el puerto " + port + ": " + e.getMessage());
        }
    }

    // --- EL NUEVO MÉTODO PARA LIBERAR EL PUERTO ---
    public void stopListening() {
        isListening = false; // Detiene el bucle del hilo
        if (socket != null && !socket.isClosed()) {
            socket.close(); // LIBERA EL PUERTO EN EL SISTEMA OPERATIVO
            System.out.println("[UDPManager] Puerto liberado y cerrado correctamente.");
        }
    }

    // 3. El Bucle Infinito de Escucha (Se ejecuta en segundo plano)
    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        while (isRunning) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet); // El hilo se pausa aquí hasta que llegue un paquete

                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("[UDPManager] Paquete recibido: " + message);

                // Le pasamos el string al resto del sistema
                notifyObservers(message);
            } catch (Exception e) {
                if (isRunning) {
                    System.err.println("[UDPManager] Error recibiendo paquete: " + e.getMessage());
                }
            }
        }
    }

    // 4. Método para enviar datos
    public void send(String message, String targetIp, int targetPort) {
        try {
            byte[] buffer = message.getBytes();
            InetAddress address = InetAddress.getByName(targetIp);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, targetPort);

            // Si el socket no está inicializado (ej. somos el cliente antes de escuchar),
            // abrimos uno temporal
            if (socket == null) {
                try (DatagramSocket tempSocket = new DatagramSocket()) {
                    tempSocket.send(packet);
                }
            } else {
                socket.send(packet);
            }
            System.out.println("[UDPManager] Enviado -> " + message);
        } catch (Exception e) {
            System.err.println("[UDPManager] Error enviando paquete: " + e.getMessage());
        }
    }

    // 5. Apagado seguro
    public void close() {
        isRunning = false;
        if (socket != null && !socket.isClosed()) {
            socket.close(); // Esto destrabará el socket.receive() lanzando una excepción controlada
        }
        System.out.println("[UDPManager] Socket cerrado.");
    }
}