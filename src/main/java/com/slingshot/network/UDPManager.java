package com.slingshot.network;

import java.io.IOException;
import java.net.*;

public class UDPManager {
    private static UDPManager instance;
    private DatagramSocket socket;
    private NetworkObserver observer;
    private int remotePort;
    private InetAddress remoteAddress;

    private UDPManager() {}

    public static UDPManager getInstance() {
        if (instance == null) instance = new UDPManager();
        return instance;
    }

    // Inicializa el socket y el hilo de escucha
    public void initialize(int localPort, String remoteIP, int remotePort) throws SocketException, UnknownHostException {
        this.socket = new DatagramSocket(localPort);
        this.remoteAddress = InetAddress.getByName(remoteIP);
        this.remotePort = remotePort;
        this.startListening();
    }

    public void setObserver(NetworkObserver observer) {
        this.observer = observer;
    }

    public void sendMessage(String message) {
        new Thread(() -> {
            try {
                byte[] buffer = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, remoteAddress, remotePort);
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void startListening() {
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            while (true) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength());
                    
                    if (observer != null) {
                        observer.onMessageReceived(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}