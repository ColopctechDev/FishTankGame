package com.fishtankgame.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.fishtankgame.FishTankGame;
import com.fishtankgame.model.Egg;
import com.fishtankgame.model.Fish;
import com.fishtankgame.model.Food;
import com.fishtankgame.model.FoodPellet;
import com.fishtankgame.model.EggObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameManager {
    private List<Fish> fishList;
    private List<EggObject> eggObjects;
    private Map<Food, Integer> foodInventory;
    private List<FoodPellet> foodPellets;
    private double money;
    private Map<String, Texture> fishTextures;
    private Texture bubbleTexture;

    // Dynamic tank bounds
    private float tankWidth = FishTankGame.VIRTUAL_WIDTH;
    private float tankHeight = FishTankGame.VIRTUAL_HEIGHT;

    public GameManager(Texture bubbleTexture) {
        fishList = new ArrayList<>();
        eggObjects = new ArrayList<>();
        foodInventory = new HashMap<>();
        foodPellets = new ArrayList<>();
        money = 50.0;
        fishTextures = new HashMap<>();
        this.bubbleTexture = bubbleTexture;

        foodInventory.put(new Food("Flax", 30, 5.0), 50);
    }

    public void updateTankSize(float width, float height) {
        this.tankWidth = width;
        this.tankHeight = height;
    }

    public float getTankWidth() { return tankWidth; }
    public float getTankHeight() { return tankHeight; }

    public void addFishTexture(String breed, Texture texture) {
        fishTextures.put(breed, texture);
    }

    public void update(float delta) {
        // Update eggs and hatch them
        List<EggObject> hatchedEggs = new ArrayList<>();
        for (EggObject eggObject : new ArrayList<>(eggObjects)) {
            eggObject.update(delta);
            if (eggObject.isReadyToHatch()) {
                hatchedEggs.add(eggObject);
            }
        }

        for (EggObject eggObject : hatchedEggs) {
            eggObjects.remove(eggObject);
            for (Fish fish : fishList) {
                if (fish.getTargetEgg() == eggObject) {
                    fish.clearTargetEgg();
                }
            }
            Egg eggData = eggObject.getEggData();
            if (fishTextures.containsKey(eggData.getBreed())) {
                Texture fishTexture = fishTextures.get(eggData.getBreed());
                int maxFillValue;
                float speed;
                switch (eggData.getBreed()) {
                    case "Goldfish": maxFillValue = 80; speed = 0.8f; break;
                    case "Angelfish": maxFillValue = 120; speed = 0.9f; break;
                    case "Betafish": maxFillValue = 150; speed = 1.2f; break;
                    case "Clownfish": maxFillValue = 180; speed = 1.1f; break;
                    case "Tigerfish": maxFillValue = 250; speed = 1.8f; break;
                    case "Blue Tang": maxFillValue = 200; speed = 1.5f; break;
                    default: maxFillValue = 100; speed = 1.0f; break;
                }
                fishList.add(new Fish("New Fish", eggData.getBreed(), eggData.getPrice() * 2, speed, fishTexture, maxFillValue, this));
            }
        }

        // Update fish
        for (Fish fish : fishList) {
            fish.update(delta);
        }

        // Bettafish fight resolution
        List<Fish> losers = new ArrayList<>();
        for (Fish fish : new ArrayList<>(fishList)) {
            if (fish.isFighting() && fish.getBounds().overlaps(fish.getTargetFish().getBounds())) {
                Fish attacker = fish;
                Fish defender = fish.getTargetFish();
                if (losers.contains(attacker) || losers.contains(defender)) continue;

                if (attacker.getFightTimer() <= 0) {
                    attacker.startFightTimer();
                    defender.startFightTimer();
                }
                else if (attacker.getFightTimer() <= 0.1f) {
                    if (attacker.getFillValue() >= defender.getFillValue()) {
                        losers.add(defender);
                    } else {
                        losers.add(attacker);
                    }
                }
            }
        }
        for (Fish loser : losers) {
            fishList.remove(loser);
            for (Fish fish : fishList) {
                if (fish.getTargetFish() == loser) {
                    fish.clearTargetFish();
                }
            }
        }

        // Food pellet collision
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

        // AI Target Assignment
        assignSchoolingLeader("Goldfish");
        assignSchoolingLeader("Clownfish");
        assignSchoolingLeader("Tigerfish");
        assignSchoolingLeader("Blue Tang");
        assignSchoolingLeader("Angelfish");
        assignSchoolingLeader("Betafish");

        for (Fish fish : fishList) {
            if (fish.getTargetFood() == null && fish.getTargetFish() == null && fish.getTargetEgg() == null) {
                if (!fish.isAdult()) {
                    FoodPellet closestPellet = findClosestPellet(fish);
                    if (closestPellet != null) {
                        fish.setTargetFood(closestPellet);
                        fish.setSchoolingLeader(null);
                    }
                } else if (fish.getBreed().equals("Betafish")) {
                    Fish otherBetta = findClosestBetta(fish);
                    if (otherBetta != null) {
                        fish.setTargetFish(otherBetta);
                        otherBetta.setTargetFish(fish);
                    }
                } else if (fish.getBreed().equals("Blue Tang") && fish.canChase()) {
                    Fish closestPrey = findClosestPrey(fish, "Goldfish");
                    if (closestPrey != null) {
                        fish.setTargetFish(closestPrey);
                    }
                } else if (fish.getBreed().equals("Angelfish")) {
                    EggObject closestEgg = findClosestEgg(fish);
                    if (closestEgg != null) {
                        fish.setTargetEgg(closestEgg);
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
                fish.clearAsLeader();
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

    private Fish findClosestBetta(Fish fighter) {
        Fish closest = null;
        float minDistance = Float.MAX_VALUE;
        Vector2 fighterPosition = new Vector2(fighter.getBounds().x, fighter.getBounds().y);
        for (Fish other : fishList) {
            if (other.getBreed().equals("Betafish") && other != fighter && other.getTargetFish() == null) {
                Vector2 otherPosition = new Vector2(other.getBounds().x, other.getBounds().y);
                float distance = fighterPosition.dst2(otherPosition);
                if (distance < minDistance) {
                    minDistance = distance;
                    closest = other;
                }
            }
        }
        return closest;
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

    private EggObject findClosestEgg(Fish fish) {
        EggObject closest = null;
        float minDistance = Float.MAX_VALUE;
        Vector2 fishPosition = new Vector2(fish.getBounds().x, fish.getBounds().y);
        for (EggObject egg : eggObjects) {
            boolean isTargeted = false;
            for (Fish otherFish : fishList) {
                if (otherFish.getTargetEgg() == egg) {
                    isTargeted = true;
                    break;
                }
            }
            if (!isTargeted) {
                Vector2 eggPosition = new Vector2(egg.getPosition().x, egg.getPosition().y);
                float distance = fishPosition.dst2(eggPosition);
                if (distance < minDistance) {
                    minDistance = distance;
                    closest = egg;
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
            float x = MathUtils.random(100, tankWidth - 100);
            float y = MathUtils.random(480, tankHeight - 16);
            foodPellets.add(new FoodPellet(foodToDrop, bubbleTexture, x, y));
            if (quantity > 1) {
                foodInventory.put(foodToDrop, quantity - 1);
            } else {
                foodInventory.remove(foodToDrop);
            }
        }
    }

    public void dropEgg(Egg egg) {
        float x = MathUtils.random(100, tankWidth - 100);
        float y = tankHeight - 16;
        eggObjects.add(new EggObject(egg, bubbleTexture, x, y));
    }

    public List<Fish> getFishList() {
        return fishList;
    }

    public List<EggObject> getEggObjects() {
        return eggObjects;
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
