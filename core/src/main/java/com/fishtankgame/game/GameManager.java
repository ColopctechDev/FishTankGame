package com.fishtankgame.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.fishtankgame.FishTankGame;
import com.fishtankgame.effects.Bubble;
import com.fishtankgame.model.Egg;
import com.fishtankgame.model.Fish;
import com.fishtankgame.model.Food;
import com.fishtankgame.model.FoodPellet;
import com.fishtankgame.model.EggObject;
import com.fishtankgame.model.Decor;
import com.fishtankgame.model.FishBreed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GameManager {
    private final List<Fish> fishList;
    private final List<EggObject> eggObjects;
    private final Map<Food, Integer> foodInventory;
    private final List<FoodPellet> foodPellets;
    private final List<Decor> decorItems;
    private final List<Bubble> decorBubbles;
    private double money;
    private final Map<String, Texture> fishTextures;
    private final Map<String, Texture> decorTextures;
    private final Texture bubbleTexture;

    private float bubbleSpawnTimer = 0;

    // Dynamic tank bounds
    private float tankWidth = FishTankGame.VIRTUAL_WIDTH;
    private float tankHeight = FishTankGame.VIRTUAL_HEIGHT;

    public GameManager(Texture bubbleTexture) {
        fishList = new ArrayList<>();
        eggObjects = new ArrayList<>();
        foodInventory = new HashMap<>();
        foodPellets = new ArrayList<>();
        decorItems = new ArrayList<>();
        decorBubbles = new ArrayList<>();
        money = 50.0;
        fishTextures = new HashMap<>();
        decorTextures = new HashMap<>();
        this.bubbleTexture = bubbleTexture;

        foodInventory.put(new Food("Flax", 30, 5.0), 50);
    }

    public void initDecor(Texture chest, Texture bubbler, Texture plant1, Texture plant2, Texture plant3, Texture plant4) {
        decorTextures.put("Treasure Chest", chest);
        decorTextures.put("Bubbler", bubbler);
        decorTextures.put("Green Fern", plant1);
        decorTextures.put("Red Kelp", plant2);
        decorTextures.put("Purple Coral", plant3);
        decorTextures.put("Amazon Sword", plant4);

        decorItems.clear();
        fishList.clear();
    }

    public void addDecor(String type, double price, int slotIndex) {
        if (!decorTextures.containsKey(type)) return;
        Texture tex = decorTextures.get(type);

        float scale = 1.0f;
        float y = 0;

        // Check for specific plant names instead of startsWith("Plant")
        if (type.equals("Green Fern") || type.equals("Red Kelp") ||
            type.equals("Purple Coral") || type.equals("Amazon Sword")) {
            scale = 1.2f;
        } else if (type.equals("Bubbler")) {
            scale = 0.3f;
            y = 0;
        } else if (type.equals("Treasure Chest")) {
            scale = 0.32f;
            y = 0;
        }

        if (slotIndex == -1) {
            // Check if bubbler or chest already exists and remove (Backward compatible removeIf)
            Iterator<Decor> it = decorItems.iterator();
            while (it.hasNext()) {
                if (it.next().getType().equals(type)) {
                    it.remove();
                }
            }
            float percent = type.equals("Bubbler") ? 0.25f : 0.70f;
            addDecorCentrally(type, tex, percent, scale, y, -1, price);
        } else {
            addPlantToSlot(type, tex, slotIndex, scale, price);
        }
    }

    public void addPlantToSlot(String name, Texture tex, int slotIndex, float scale, double price) {
        if (slotIndex < 0 || slotIndex >= 15) return;
        // Backward compatible removeIf
        Iterator<Decor> it = decorItems.iterator();
        while (it.hasNext()) {
            if (it.next().getSlotIndex() == slotIndex) {
                it.remove();
            }
        }

        float wallMargin = 40f;
        float activeWidth = tankWidth - (2 * wallMargin);
        float slotWidth = activeWidth / 14f;
        float xPercent = (wallMargin + (slotIndex * slotWidth)) / tankWidth;
        float xCenter = tankWidth * xPercent;
        float scaledWidth = tex.getWidth() * scale;
        float x = xCenter - (scaledWidth / 2f);

        decorItems.add(new Decor(name, tex, x, 0, scale, slotIndex, xPercent, price));
    }

    public boolean isBubblerActive() {
        for (Decor d : decorItems) {
            if (d.getType().equals("Bubbler")) return true;
        }
        return false;
    }

    public boolean isSlotOccupied(int slotIndex) {
        for (Decor d : decorItems) {
            if (d.getSlotIndex() == slotIndex) return true;
        }
        return false;
    }

    private void addDecorCentrally(String name, Texture tex, float percent, float scale, float y, int slot, double price) {
        float wallMargin = 40f;
        float activeWidth = tankWidth - (2 * wallMargin);
        float xPercent = (wallMargin + (activeWidth * percent)) / tankWidth;
        float xCenter = tankWidth * xPercent;
        float scaledWidth = tex.getWidth() * scale;
        float x = xCenter - (scaledWidth / 2f);
        decorItems.add(new Decor(name, tex, x, y, scale, slot, xPercent, price));
    }

    public void updateTankSize(float width, float height) {
        this.tankWidth = width;
        this.tankHeight = height;
        for (Decor decor : decorItems) {
            float scaledWidth = decor.getBounds().width;
            float xCenter = tankWidth * decor.getXPercent();
            float newX = xCenter - (scaledWidth / 2f);
            newX = MathUtils.clamp(newX, 10, tankWidth - scaledWidth - 10);
            decor.updatePosition(newX);
        }
    }

    public float getTankWidth() { return tankWidth; }
    public float getTankHeight() { return tankHeight; }

    public void addFishTexture(String breed, Texture texture) {
        fishTextures.put(breed, texture);
    }

    public void update(float delta) {
        updateDecorBubbles(delta);
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
                FishBreed breedInfo = FishBreed.fromName(eggData.getBreed());
                fishList.add(new Fish("New Fish", breedInfo.getName(), eggData.getPrice() * 2, breedInfo.getSpeed(), fishTexture, breedInfo.getMaxFillValue(), this, eggObject.getPosition()));
            }
        }
        for (Fish fish : fishList) {
            fish.update(delta);
        }
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
                    if (attacker.getFillValue() >= defender.getFillValue()) losers.add(defender);
                    else losers.add(attacker);
                }
            }
        }
        for (Fish loser : losers) {
            fishList.remove(loser);
            for (Fish fish : fishList) {
                if (fish.getTargetFish() == loser) fish.clearTargetFish();
            }
        }
        for (FoodPellet pellet : new ArrayList<>(foodPellets)) {
            pellet.update(delta);
            for (Fish fish : fishList) {
                if (!fish.isAdult() && fish.getBounds().overlaps(pellet.getBounds())) {
                    fish.feed(pellet.getFoodType().getGrowthBoost());
                    foodPellets.remove(pellet);
                    for (Fish otherFish : fishList) {
                        if (otherFish.getTargetFood() == pellet) otherFish.clearTargetFood();
                    }
                    break;
                }
            }
        }

        // Backward compatible grouping
        Map<String, List<Fish>> breedGroups = new HashMap<>();
        for (Fish fish : fishList) {
            List<Fish> group = breedGroups.get(fish.getBreed());
            if (group == null) {
                group = new ArrayList<>();
                breedGroups.put(fish.getBreed(), group);
            }
            group.add(fish);
        }
        for (Map.Entry<String, List<Fish>> entry : breedGroups.entrySet()) {
            assignSchoolingLeaderInGroup(entry.getValue());
        }

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
                    if (closestPrey != null) fish.setTargetFish(closestPrey);
                } else if (fish.getBreed().equals("Angelfish")) {
                    EggObject closestEgg = findClosestEgg(fish);
                    if (closestEgg != null) fish.setTargetEgg(closestEgg);
                }
            }
        }
    }

    private void updateDecorBubbles(float delta) {
        Decor bubbler = null;
        for (Decor d : decorItems) {
            if (d.getType().equals("Bubbler")) {
                bubbler = d;
                break;
            }
        }
        if (bubbler != null) {
            bubbleSpawnTimer += delta;
            if (bubbleSpawnTimer > 0.15f) {
                bubbleSpawnTimer = 0;
                float baseX = bubbler.getPosition().x + (bubbler.getBounds().width / 2f);
                float spawnY = bubbler.getPosition().y + bubbler.getBounds().height - 10;
                decorBubbles.add(new Bubble(bubbleTexture, baseX, spawnY, tankHeight));
                float roll = MathUtils.random();
                if (roll < 0.20f) {
                    decorBubbles.add(new Bubble(bubbleTexture, baseX - 12, spawnY, tankHeight));
                    decorBubbles.add(new Bubble(bubbleTexture, baseX + 12, spawnY, tankHeight));
                } else if (roll < 0.80f) {
                    if (MathUtils.randomBoolean()) decorBubbles.add(new Bubble(bubbleTexture, baseX - 12, spawnY, tankHeight));
                    else decorBubbles.add(new Bubble(bubbleTexture, baseX + 12, spawnY, tankHeight));
                }
            }
        }
        for (int i = decorBubbles.size() - 1; i >= 0; i--) {
            Bubble b = decorBubbles.get(i);
            b.update(delta);
            if (b.isOffScreen()) decorBubbles.remove(i);
        }
    }

    public List<Bubble> getDecorBubbles() { return decorBubbles; }
    public List<Decor> getDecorItems() { return decorItems; }

    public void handleSoldFish(Fish soldFish) {
        for (Fish fish : fishList) {
            if (fish.getSchoolingLeader() == soldFish) fish.setSchoolingLeader(null);
        }
    }

    private void assignSchoolingLeaderInGroup(List<Fish> group) {
        Fish leader = null;
        for (Fish fish : group) {
            fish.clearAsLeader();
            if (leader == null) {
                leader = fish;
                fish.setAsLeader();
                fish.setSchoolingLeader(null);
            } else {
                if (fish.getTargetFood() == null) fish.setSchoolingLeader(leader);
                else fish.setSchoolingLeader(null);
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
        ClosestEggResult result = findClosestAvailableEgg(fish);
        return result != null ? result.egg : null;
    }

    private ClosestEggResult findClosestAvailableEgg(Fish fish) {
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
        return closest != null ? new ClosestEggResult(closest, minDistance) : null;
    }

    private static class ClosestEggResult {
        EggObject egg;
        float distanceSq;
        ClosestEggResult(EggObject egg, float distanceSq) {
            this.egg = egg;
            this.distanceSq = distanceSq;
        }
    }

    public void addFood(Food food, int quantity) {
        Integer currentQuantity = foodInventory.get(food);
        if (currentQuantity == null) currentQuantity = 0;
        foodInventory.put(food, currentQuantity + quantity);
    }

    public void dropFood() {
        if (!foodInventory.isEmpty()) {
            Food foodToDrop = foodInventory.keySet().iterator().next();
            Integer quantity = foodInventory.get(foodToDrop);
            if (quantity == null) quantity = 0;

            float x = MathUtils.random(tankWidth * 0.2f, tankWidth * 0.8f);
            float y = MathUtils.random(480, tankHeight - 16);
            foodPellets.add(new FoodPellet(foodToDrop, bubbleTexture, x, y));
            if (quantity > 1) foodInventory.put(foodToDrop, quantity - 1);
            else foodInventory.remove(foodToDrop);
        }
    }

    public void dropEgg(Egg egg) {
        float x = MathUtils.random(tankWidth * 0.2f, tankWidth * 0.8f);
        float y = tankHeight - 16;
        eggObjects.add(new EggObject(egg, bubbleTexture, x, y));
    }

    public List<Fish> getFishList() { return fishList; }
    public List<EggObject> getEggObjects() { return eggObjects; }
    public List<FoodPellet> getFoodPellets() { return foodPellets; }
    public Map<Food, Integer> getFoodInventory() { return foodInventory; }
    public double getMoney() { return money; }
    public void setMoney(double money) { this.money = money; }
}
