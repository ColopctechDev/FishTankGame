package com.fishtankgame.game;

import com.badlogic.gdx.graphics.Texture;
import com.fishtankgame.model.Egg;
import com.fishtankgame.model.Fish;
import com.fishtankgame.model.Food;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameManager {
    private List<Fish> fishList;
    private List<Egg> eggList;
    private List<Food> foodList;
    private double money;
    private Map<String, Texture> fishTextures;

    public GameManager() {
        fishList = new ArrayList<>();
        eggList = new ArrayList<>();
        foodList = new ArrayList<>();
        money = 100.0; // Starting money
        fishTextures = new HashMap<>();
    }

    public void addFishTexture(String breed, Texture texture) {
        fishTextures.put(breed, texture);
    }

    public void update() {
        // Egg hatching
        List<Egg> hatchedEggs = new ArrayList<>();
        for (Egg egg : eggList) {
            // In a real game, you would use a timer. For now, we'll just hatch them instantly.
            hatchedEggs.add(egg);
        }

        for (Egg egg : hatchedEggs) {
            eggList.remove(egg);
            if (fishTextures.containsKey(egg.getBreed())) {
                Texture fishTexture = fishTextures.get(egg.getBreed());
                fishList.add(new Fish("New Fish", egg.getBreed(), egg.getPrice() * 2, 1.5f, fishTexture));
            }
        }

        // Fish growth
        for (Fish fish : fishList) {
            fish.grow();
        }
    }

    // Getters and setters

    public List<Fish> getFishList() {
        return fishList;
    }

    public List<Egg> getEggList() {
        return eggList;
    }

    public List<Food> getFoodList() {
        return foodList;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }
}
