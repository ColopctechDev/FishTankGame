package com.fishtankgame.model;

public class Egg {
    private String breed;
    private int hatchTime;
    private double price;

    public Egg(String breed, int hatchTime, double price) {
        this.breed = breed;
        this.hatchTime = hatchTime;
        this.price = price;
    }

    public String getBreed() {
        return breed;
    }

    public int getHatchTime() {
        return hatchTime;
    }

    public double getPrice() {
        return price;
    }
}
