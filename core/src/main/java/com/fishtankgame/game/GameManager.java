package com.fishtankgame.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.fishtankgame.model.Egg;
import com.fishtankgame.model.Fish;
import com.fishtankgame.model.Food;
import com.fishtankgame.model.FoodPellet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameManager {
    private List<Fish> fishList;
    private List<Egg> eggList;
    private Map<Food, Integer> foodInventory;
    private List<FoodPellet> foodPellets;
    private double money;
    private Map<String, Texture> fishTextures;
    private Texture foodPelletTexture;

    public GameManager(Texture foodPelletTexture) {
        fishList = new ArrayList<>();
        eggList = new ArrayList<>();
        foodInventory = new HashMap<>();
        foodPellets = new ArrayList<>();
        money = 50.0; // Starting money adjusted
        fishTextures = new HashMap<>();
        this.foodPelletTexture = foodPelletTexture;

        // Add starting food
        foodInventory.put(new Food("Sunflower", 10, 1.0), 20);
    }

    public void addFishTexture(String breed, Texture texture) {
        fishTextures.put(breed, texture);
    }

    public void update(float delta) {
        // Egg hatching
        List<Egg> hatchedEggs = new ArrayList<>();
        for (Egg egg : eggList) {
            hatchedEggs.add(egg);
        }

        for (Egg egg : hatchedEggs) {
            eggList.remove(egg);
            if (fishTextures.containsKey(egg.getBreed())) {
                Texture fishTexture = fishTextures.get(egg.getBreed());
                int maxFillValue = egg.getBreed().equals("Goldfish") ? 80 : 100;
                float speed = egg.getBreed().equals("Goldfish") ? 0.8f : 1.0f;
                fishList.add(new Fish("New Fish", egg.getBreed(), egg.getPrice() * 2, speed, fishTexture, maxFillValue));
            }
        }

        // Update fish
        for (Fish fish : fishList) {
            fish.update(delta);
        }

        // Update and check collisions for food pellets
        for (FoodPellet pellet : new ArrayList<>(foodPellets)) {
            pellet.update(delta);
            for (Fish fish : fishList) {
                if (!fish.isAdult() && fish.getBounds().overlaps(pellet.getBounds())) {
                    fish.feed(pellet.getFoodType().getGrowthBoost());
                    foodPellets.remove(pellet);
                    for (Fish otherFish : fishList) {
                        if (otherFish.getTargetFood() == pellet) {
                            otherFish.clearTargetFood();
                        }
                    }
                    break;
                }
            }
        }

        // Assign schooling leader and targets
        assignSchoolingLeader("Goldfish");

        for (Fish fish : fishList) {
            if (fish.getTargetFood() == null && fish.getTargetFish() == null) {
                if (!fish.isAdult()) {
                    FoodPellet closestPellet = findClosestPellet(fish);
                    if (closestPellet != null) {
                        fish.setTargetFood(closestPellet);
                        fish.setSchoolingLeader(null);
                    }
                } else if (fish.getBreed().equals("Blue Tang") && fish.canChase()) {
                    Fish closestPrey = findClosestPrey(fish, "Goldfish");
                    if (closestPrey != null) {
                        fish.setTargetFish(closestPrey);
                    }
                }
            }
        }
    }

    public void handleSoldFish(Fish soldFish) {
        for (Fish fish : fishList) {
            if (fish.getSchoolingLeader() == soldFish) {
                fish.setSchoolingLeader(null);
            }
        }
    }

    private void assignSchoolingLeader(String breed) {
        Fish leader = null;
        for (Fish fish : fishList) {
            if (fish.getBreed().equals(breed)) {
                fish.clearAsLeader(); // Clear previous leaders
                if (leader == null) {
                    leader = fish;
                    fish.setAsLeader();
                    fish.setSchoolingLeader(null);
                } else {
                    if (fish.getTargetFood() == null) {
                        fish.setSchoolingLeader(leader);
                    }
                }
            }
        }
    }

    private FoodPellet findClosestPellet(Fish fish) {
        FoodPellet closest = null;
        float minDistance = Float.MAX_VALUE;

        Vector2 fishPosition = new Vector2(fish.getBounds().x, fish.getBounds().y);

        for (FoodPellet pellet : foodPellets) {
            boolean isTargeted = false;
            for (Fish otherFish : fishList) {
                if (otherFish.getTargetFood() == pellet) {
                    isTargeted = true;
                    break;
                }
            }

            if (!isTargeted) {
                Vector2 pelletPosition = new Vector2(pellet.getBounds().x, pellet.getBounds().y);
                float distance = fishPosition.dst2(pelletPosition);
                if (distance < minDistance) {
                    minDistance = distance;
                    closest = pellet;
                }
            }
        }
        return closest;
    }

    private Fish findClosestPrey(Fish predator, String preyBreed) {
        Fish closest = null;
        float minDistance = Float.MAX_VALUE;

        Vector2 predatorPosition = new Vector2(predator.getBounds().x, predator.getBounds().y);

        for (Fish prey : fishList) {
            if (prey.getBreed().equals(preyBreed)) {
                Vector2 preyPosition = new Vector2(prey.getBounds().x, prey.getBounds().y);
                float distance = predatorPosition.dst2(preyPosition);

                if (distance < minDistance) {
                    minDistance = distance;
                    closest = prey;
                }
            }
        }
        return closest;
    }

    public void addFood(Food food, int quantity) {
        foodInventory.put(food, foodInventory.getOrDefault(food, 0) + quantity);
    }

    public void dropFood() {
        if (!foodInventory.isEmpty()) {
            Food foodToDrop = foodInventory.keySet().iterator().next();
            int quantity = foodInventory.get(foodToDrop);

            float x = MathUtils.random(100, 1280 - 100);
            float y = MathUtils.random(480, 720 - 16);

            foodPellets.add(new FoodPellet(foodToDrop, foodPelletTexture, x, y));

            if (quantity > 1) {
                foodInventory.put(foodToDrop, quantity - 1);
            } else {
                foodInventory.remove(foodToDrop);
            }
        }
    }

    // Getters and setters

    public List<Fish> getFishList() {
        return fishList;
    }

    public List<Egg> getEggList() {
        return eggList;
    }

    public List<FoodPellet> getFoodPellets() {
        return foodPellets;
    }

    public Map<Food, Integer> getFoodInventory() {
        return foodInventory;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }
}
