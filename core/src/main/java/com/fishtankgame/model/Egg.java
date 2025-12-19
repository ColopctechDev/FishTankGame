package com.fishtankgame.model;

public class Egg {
    private final String breed;
    private final int quantity;
    private final double price;
    private final boolean isPremium;

    public Egg(String breed, int quantity, double price) {
        this(breed, quantity, price, false);
    }

    public Egg(String breed, int quantity, double price, boolean isPremium) {
        this.breed = breed;
        this.quantity = quantity;
        this.price = price;
        this.isPremium = isPremium;
    }

    public String getBreed() { return breed; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public boolean isPremium() { return isPremium; }
}
