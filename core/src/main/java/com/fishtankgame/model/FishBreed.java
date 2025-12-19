package com.fishtankgame.model;

public enum FishBreed {
    GOLDFISH("Goldfish", 25, 0.8f, 30f, false),
    BLUE_TANG("Blue Tang", 80, 1.5f, 45f, false),
    ANGELFISH("Angelfish", 100, 0.9f, 60f, false),
    BETAFISH("Betafish", 200, 1.2f, 60f, false),
    CLOWNFISH("Clownfish", 225, 1.1f, 60f, false),
    TIGERFISH("Tigerfish", 400, 1.8f, 60f, false),
    KOI("Koi", 150, 1.0f, 120f, true),
    RAINBOWFISH("Rainbowfish", 225, 2.0f, 180f, true);

    private final String name;
    private final int maxFillValue;
    private final float speed;
    private final float hatchTime;
    private final boolean isPremium;

    FishBreed(String name, int maxFillValue, float speed, float hatchTime, boolean isPremium) {
        this.name = name;
        this.maxFillValue = maxFillValue;
        this.speed = speed;
        this.hatchTime = hatchTime;
        this.isPremium = isPremium;
    }

    public String getName() { return name; }
    public int getMaxFillValue() { return maxFillValue; }
    public float getSpeed() { return speed; }
    public float getHatchTime() { return hatchTime; }
    public boolean isPremium() { return isPremium; }

    public static FishBreed fromName(String name) {
        for (FishBreed breed : values()) {
            if (breed.name.equalsIgnoreCase(name)) return breed;
        }
        return GOLDFISH; // Default
    }
}
