package com.slingshot.entities;

public abstract class CrateType {
    protected String rewardType;
    protected int health;

    public CrateType(String rewardType, int health) {
        this.rewardType = rewardType;
        this.health = health;
    }

    // El método que cada subclase definirá
    public abstract void applyEffect(Player player);
}