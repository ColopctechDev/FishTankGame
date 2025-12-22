package com.fishtankgame.model;

public enum FishBreed {
    GOLDFISH("Goldfish", 25, 0.8f, 30f, false, 5, 15),
    BLUE_TANG("Blue Tang", 80, 1.5f, 45f, false, 15, 45),
    ANGELFISH("Angelfish", 100, 0.9f, 60f, false, 20, 60),
    BETAFISH("Betafish", 200, 1.2f, 60f, false, 25, 75),
    CLOWNFISH("Clownfish", 225, 1.1f, 60f, false, 30, 90),
    TIGERFISH("Tigerfish", 400, 1.8f, 60f, false, 50, 150),
    KOI("Koi", 150, 1.0f, 120f, true, 100, 20000),
    RAINBOWFISH("Rainbowfish", 225, 2.0f, 180f, true, 250, 50000),
    TETRA("Tetra", 40, 1.2f, 20f, false, 10, 30),
    SNAIL("Snail", 30, 0.16f, 40f, false, 35, 105),
    BLUE_GUPPY("Blue Guppy", 60, 1.0f, 40f, false, 40, 120),
    GEMFISH("Gemfish", 300, 4.0f, 240f, true, 350, 70000),
    PLATINUM_AROWANA("Platinum Arowana", 1000, 0.6f, 600f, false, 1000000, 3000000),
    STARFISH("Starfish", 100, 0.05f, 45f, true, 500, 100000);

    private final String name;
    private final int maxFillValue;
    private final float speed;
    private final float hatchTime;
    private final boolean isPremium;
    private final double buyPrice;
    private final double sellPrice;

    FishBreed(String name, int maxFillValue, float speed, float hatchTime, boolean isPremium, double buyPrice, double sellPrice) {
        this.name = name;
        this.maxFillValue = maxFillValue;
        this.speed = speed;
        this.hatchTime = hatchTime;
        this.isPremium = isPremium;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
    }

    public String getName() { return name; }
    public int getMaxFillValue() { return maxFillValue; }
    public float getSpeed() { return speed; }
    public float getHatchTime() { return hatchTime; }
    public boolean isPremium() { return isPremium; }
    public double getBuyPrice() { return buyPrice; }
    public double getSellPrice() { return sellPrice; }

    public static FishBreed fromName(String name) {
        for (FishBreed breed : values()) {
            if (breed.name.equalsIgnoreCase(name)) return breed;
        }
        return GOLDFISH; // Default
    }
}
