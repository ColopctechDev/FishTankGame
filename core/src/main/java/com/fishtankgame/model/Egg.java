package com.fishtankgame.model;

public record Egg(String breed, int quantity, double price, boolean isPremium) {
    public Egg(String breed, int quantity, double price) {
        this(breed, quantity, price, false);
    }

}
