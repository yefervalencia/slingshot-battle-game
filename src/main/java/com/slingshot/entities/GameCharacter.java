package com.slingshot.entities;

public class GameCharacter {
    private String id;
    private String name;
    private String imagePath;
    private String description;

    public GameCharacter(String id, String name, String imagePath, String description) {
        this.id = id;
        this.name = name;
        this.imagePath = imagePath;
        this.description = description;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getImagePath() { return imagePath; }
    public String getDescription() { return description; }

    @Override
    public String toString() { return name; } // Para que el ComboBox no explote
}