package com.fishtankgame.model;

public class Food {
    private String type;
    private int growthBoost;
    private double price;

    public Food(String type, int growthBoost, double price) {
        this.type = type;
        this.growthBoost = growthBoost;
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public int getGrowthBoost() {
        return growthBoost;
    }

    public double getPrice() {
        return price;
    }
}
