package com.fishtankgame.model;

import java.util.Objects;

public record Food(String type, int growthBoost, double price, boolean isPremium) {
    public Food(String type, int growthBoost, double price) {
        this(type, growthBoost, price, false);
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
