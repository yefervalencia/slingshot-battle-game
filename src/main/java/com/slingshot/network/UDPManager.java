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
    public void startListening(int localPort) {
        try {
            socket = new DatagramSocket(localPort);
            isRunning = true;
            listenThread = new Thread(this); // 'this' llama al método run()
            listenThread.start();
            System.out.println("[UDPManager] Escuchando en el puerto local: " + localPort);
        } catch (Exception e) {
            System.err.println("[UDPManager] Error al abrir el puerto: " + e.getMessage());
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
            
            // Si el socket no está inicializado (ej. somos el cliente antes de escuchar), abrimos uno temporal
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