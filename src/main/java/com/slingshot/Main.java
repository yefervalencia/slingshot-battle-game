package com.slingshot;

import com.slingshot.network.UDPManager;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("--- SLING-SHOT BATTLE: NETWORK TEST ---");
        
        System.out.print("Enter YOUR Local Port (e.g., 9000): ");
        int localPort = scanner.nextInt();
        
        System.out.print("Enter REMOTE IP (e.g., 192.168.1.15): ");
        String remoteIp = scanner.next();
        
        System.out.print("Enter REMOTE Port (e.g., 9001): ");
        int remotePort = scanner.nextInt();

        try {
            // 1. Initialize UDP
            UDPManager.getInstance().initialize(localPort, remoteIp, remotePort);
            
            // 2. Set a simple observer to print what we receive
            UDPManager.getInstance().setObserver(payload -> {
                System.out.println("\n[RECEIVED FROM REMOTE]: " + payload);
                System.out.print("Type a message to send: ");
            });

            System.out.println("\nConnection initialized. Listening on port " + localPort);
            
            // 3. Loop to send messages manually
            while (true) {
                System.out.print("Type a message to send: ");
                String msg = scanner.next();
                UDPManager.getInstance().sendMessage(msg);
                if (msg.equalsIgnoreCase("exit")) break;
            }

        } catch (Exception e) {
            System.err.println("Error initializing network: " + e.getMessage());
        }
    }
}