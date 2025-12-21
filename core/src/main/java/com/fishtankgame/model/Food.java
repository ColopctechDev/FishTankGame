package com.fishtankgame.model;

import java.util.Objects;

public class Food {
    private final String type;
    private final int growthBoost;
    private final double price;
    private final boolean isPremium;

    public Food(String type, int growthBoost, double price) {
        this(type, growthBoost, price, false);
    }

    public Food(String type, int growthBoost, double price, boolean isPremium) {
        this.type = type;
        this.growthBoost = growthBoost;
        this.price = price;
        this.isPremium = isPremium;
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

    public boolean isPremium() {
        return isPremium;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Food food = (Food) o;
        return Objects.equals(type, food.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
