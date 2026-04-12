package com.slingshot.core;

import javafx.scene.Scene;
import java.util.HashSet;
import java.util.Set;

public class InputManager {
    private Set<String> activeKeys = new HashSet<>();
    private double mouseX = 0;
    private double mouseY = 0;
    private boolean mousePressed = false;

    public void attachToScene(Scene scene) {
        // Teclado
        scene.setOnKeyPressed(e -> activeKeys.add(e.getCode().toString()));
        scene.setOnKeyReleased(e -> activeKeys.remove(e.getCode().toString()));
        
        // Mouse (Para apuntar más adelante)
        scene.setOnMouseMoved(e -> { mouseX = e.getX(); mouseY = e.getY(); });
        scene.setOnMouseDragged(e -> { mouseX = e.getX(); mouseY = e.getY(); });
        scene.setOnMousePressed(e -> mousePressed = true);
        scene.setOnMouseReleased(e -> mousePressed = false);
    }

    public boolean isKeyPressed(String keyCode) {
        return activeKeys.contains(keyCode);
    }

    public boolean isMousePressed() { return mousePressed; }
    public double getMouseX() { return mouseX; }
    public double getMouseY() { return mouseY; }
}