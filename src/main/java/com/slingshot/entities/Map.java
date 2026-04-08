package com.slingshot.entities;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

public class Map {
    private String name;
    private Image image;
    private String currentPlayerTurn; // El username del jugador que tiene el turno
    
    // Lista de todos los objetos en el mapa (Cajas, Barreras, etc.)
    private ArrayList<GameObject> entities;

    public Map(String name) {
        this.name = name;
        this.entities = new ArrayList<>();
        this.currentPlayerTurn = ""; 
    }

    public void addEntity(GameObject entity) {
        entities.add(entity);
    }

    public ArrayList<GameObject> getEntities() {
        return entities;
    }

    public String getCurrentPlayerTurn() {
        return currentPlayerTurn;
    }

    public void setCurrentPlayerTurn(String currentPlayerTurn) {
        this.currentPlayerTurn = currentPlayerTurn;
    }
}