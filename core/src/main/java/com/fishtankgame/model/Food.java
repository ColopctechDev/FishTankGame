package com.fishtankgame.model;

import java.util.Objects;

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
