package com.fishtankgame.model;

public enum FishBreed {
    GOLDFISH("Goldfish", 80, 0.8f, 30f),
    BLUE_TANG("Blue Tang", 200, 1.5f, 45f),
    ANGELFISH("Angelfish", 120, 0.9f, 60f),
    BETAFISH("Betafish", 150, 1.2f, 60f),
    CLOWNFISH("Clownfish", 180, 1.1f, 60f),
    TIGERFISH("Tigerfish", 250, 1.8f, 60f);

    private final String name;
    private final int maxFillValue;
    private final float speed;
    private final float hatchTime;

    FishBreed(String name, int maxFillValue, float speed, float hatchTime) {
        this.name = name;
        this.maxFillValue = maxFillValue;
        this.speed = speed;
        this.hatchTime = hatchTime;
    }

    public String getName() { return name; }
    public int getMaxFillValue() { return maxFillValue; }
    public float getSpeed() { return speed; }
    public float getHatchTime() { return hatchTime; }

    public static FishBreed fromName(String name) {
        for (FishBreed breed : values()) {
            if (breed.name.equalsIgnoreCase(name)) return breed;
        }
        return GOLDFISH; // Default
    }
}
