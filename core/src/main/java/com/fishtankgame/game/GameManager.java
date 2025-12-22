package com.fishtankgame.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
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
import com.google.gson.Gson;

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
    private int pearls;
    private final Map<String, Texture> fishTextures;
    private final Map<String, Texture> decorTextures;
    private final Texture bubbleTexture;
    private Texture eggTexture;
    private PurchaseHandler purchaseHandler;

    private float bubbleSpawnTimer = 0;
    private float eggPusSpawnTimer = 0;
    private float eggPusTargetTime = 0;

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
        pearls = 9999; // Testing pearls
        fishTextures = new HashMap<>();
        decorTextures = new HashMap<>();
        this.bubbleTexture = bubbleTexture;
        this.eggTexture = bubbleTexture; // Default to bubble until set

        // Add starting inventory
        foodInventory.put(new Food("Sunflower", 3, 10.0, false), 10);

        resetEggPusTimer();
    }

    private void resetEggPusTimer() {
        eggPusSpawnTimer = 0;
        eggPusTargetTime = MathUtils.random(30f, 120f);
    }

    public void setPurchaseHandler(PurchaseHandler purchaseHandler) {
        this.purchaseHandler = purchaseHandler;
    }

    public void setEggTexture(Texture eggTexture) {
        this.eggTexture = eggTexture;
    }

    public void purchasePearls(final int amount) {
        if (purchaseHandler != null) {
            purchaseHandler.purchasePremiumCurrency(amount, new PurchaseHandler.PurchaseCallback() {
                @Override
                public void onSuccess() {
                    pearls += amount;
                }

                @Override
                public void onFailure(String message) {
                    // Handle failure (maybe log or show a message)
                }
            });
        }
    }

    public void initDecor(Texture chest, Texture bubbler, Texture plant1, Texture plant2, Texture plant3, Texture plant4, Texture yardRock, Texture eggPus) {
        decorTextures.put("Treasure Chest", chest);
        decorTextures.put("Bubbler", bubbler);
        decorTextures.put("Green Fern", plant1);
        decorTextures.put("Red Kelp", plant2);
        decorTextures.put("Purple Coral", plant3);
        decorTextures.put("Amazon Sword", plant4);
        decorTextures.put("YardRock", yardRock);
        decorTextures.put("EggPus", eggPus);

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
        } else if (type.equals("YardRock")) {
            scale = 0.5f;
            y = 0;
        } else if (type.equals("Bubbler")) {
            scale = 0.3f;
            y = 0;
        } else if (type.equals("Treasure Chest")) {
            scale = 0.32f;
            y = 0;
        } else if (type.equals("EggPus")) {
            scale = 0.4f;
            y = 0;
        }

        if (slotIndex == -1) {
            // Check if fixed items already exist and remove
            Iterator<Decor> it = decorItems.iterator();
            while (it.hasNext()) {
                if (it.next().getType().equals(type)) {
                    it.remove();
                }
            }
            float percent = 0.5f;
            if (type.equals("Bubbler")) percent = 0.25f;
            else if (type.equals("Treasure Chest")) percent = 0.70f;
            else if (type.equals("EggPus")) percent = 0.45f;

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

    public Texture getFishTexture(String breed) {
        return fishTextures.get(breed);
    }

    public void update(float delta) {
        updateDecorBubbles(delta);
        updateEggPus(delta);
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
            if (fishTextures.containsKey(eggData.breed())) {
                Texture fishTexture = fishTextures.get(eggData.breed());
                FishBreed breedInfo = FishBreed.fromName(eggData.breed());
                fishList.add(new Fish("New Fish", breedInfo.getName(), breedInfo.getSellPrice(), breedInfo.getSpeed(), fishTexture, breedInfo.getMaxFillValue(), this, eggObject.getPosition()));
            }
        }
        for (Fish fish : fishList) {
            fish.update(delta);
        }
        List<Fish> losers = new ArrayList<>();
        for (Fish fish : new ArrayList<>(fishList)) {
            if (fish.isFighting() && fish.getTargetFish() != null && fish.getBounds().overlaps(fish.getTargetFish().getBounds())) {
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
                    fish.feed(pellet.getFoodType().growthBoost());
                    foodPellets.remove(pellet);
                    for (Fish otherFish : fishList) {
                        if (otherFish.getTargetFood() == pellet) otherFish.clearTargetFood();
                    }
                    break;
                }
            }
        }

        // Only goldfish should have schooling behavior
        List<Fish> goldfishList = new ArrayList<>();
        for (Fish fish : fishList) {
            if (fish.getBreed().equals("Goldfish")) {
                goldfishList.add(fish);
            } else {
                fish.clearAsLeader();
                fish.setSchoolingLeader(null);
            }
        }
        assignSchoolingLeaderInGroup(goldfishList);

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

    private void updateEggPus(float delta) {
        Decor eggPus = null;
        for (Decor d : decorItems) {
            if (d.getType().equals("EggPus")) {
                eggPus = d;
                break;
            }
        }
        if (eggPus != null) {
            eggPusSpawnTimer += delta;
            if (eggPusSpawnTimer >= eggPusTargetTime) {
                tossEggFromEggPus(eggPus);
                spawnFoodFromStarfish();
                resetEggPusTimer();
            }
        }
    }

    private void spawnFoodFromStarfish() {
        for (Fish fish : fishList) {
            if (fish.getBreed().equals("Starfish")) {
                float roll = MathUtils.random(0f, 100f);
                Food foodType;
                if (roll <= 30.0f) foodType = createFoodByType("Sunflower");
                else if (roll <= 50.0f) foodType = createFoodByType("Poppy");
                else if (roll <= 65.0f) foodType = createFoodByType("Flax");
                else if (roll <= 75.0f) foodType = createFoodByType("Sesame");
                else if (roll <= 85.0f) foodType = createFoodByType("Chia");
                else if (roll <= 92.5f) foodType = createFoodByType("Hemp");
                else if (roll <= 97.5f) foodType = createFoodByType("Pumpkin");
                else foodType = createFoodByType("Quinoa");

                float centerX = fish.getBounds().x + fish.getBounds().width / 2f;
                float centerY = fish.getBounds().y + fish.getBounds().height / 2f;

                // Star pattern (5 points)
                float[][] offsets = {{0, 10}, {-10, -5}, {10, -5}, {-7, 7}, {7, 7}};
                for (float[] offset : offsets) {
                    foodPellets.add(new FoodPellet(foodType, bubbleTexture, centerX + offset[0], centerY + offset[1]));
                }

                fish.triggerStarfishFlip();
            }
        }
    }

    private void tossEggFromEggPus(Decor eggPus) {
        float roll = MathUtils.random(0f, 100f);
        String breed;
        double price;
        boolean premium = false;

        if (roll <= 20.0f) { breed = "Goldfish"; price = 5; }
        else if (roll <= 35.0f) { breed = "Blue Tang"; price = 15; }
        else if (roll <= 45.0f) { breed = "Clownfish"; price = 30; }
        else if (roll <= 55.0f) { breed = "Angelfish"; price = 20; }
        else if (roll <= 65.0f) { breed = "Betafish"; price = 25; }
        else if (roll <= 75.0f) { breed = "Tigerfish"; price = 50; }
        else if (roll <= 82.0f) { breed = "Tetra"; price = 10; }
        else if (roll <= 88.0f) { breed = "Snail"; price = 35; }
        else if (roll <= 93.0f) { breed = "Blue Guppy"; price = 40; }
        else if (roll <= 96.0f) { breed = "Koi"; price = 100; premium = true; }
        else if (roll <= 98.0f) { breed = "Rainbowfish"; price = 250; premium = true; }
        else if (roll <= 99.0f) { breed = "Gemfish"; price = 350; premium = true; }
        else if (roll <= 99.7f) { breed = "Starfish"; price = 500; premium = true; }
        else { breed = "Platinum Arowana"; price = 1000000; }

        float x = eggPus.getPosition().x + eggPus.getBounds().width / 2f;
        float y = eggPus.getPosition().y + eggPus.getBounds().height / 2f;
        eggObjects.add(new EggObject(new Egg(breed, 1, price, premium), eggTexture, x, y));
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
        dropFood(false);
    }

    public void dropFood(boolean premiumOnly) {
        Food bestFood = null;
        int highestGrowth = -1;

        for (Map.Entry<Food, Integer> entry : foodInventory.entrySet()) {
            Food food = entry.getKey();
            if (food.isPremium() == premiumOnly) {
                if (food.growthBoost() > highestGrowth) {
                    highestGrowth = food.growthBoost();
                    bestFood = food;
                }
            }
        }

        if (bestFood != null) {
            Integer quantity = foodInventory.get(bestFood);
            float x = MathUtils.random(tankWidth * 0.2f, tankWidth * 0.8f);
            float y = MathUtils.random(480, tankHeight - 16);
            foodPellets.add(new FoodPellet(bestFood, bubbleTexture, x, y));
            if (quantity > 1) foodInventory.put(bestFood, quantity - 1);
            else foodInventory.remove(bestFood);
        }
    }

    public void dropEgg(Egg egg) {
        float x = MathUtils.random(tankWidth * 0.2f, tankWidth * 0.8f);
        float y = tankHeight - 16;
        eggObjects.add(new EggObject(egg, eggTexture, x, y));
    }

    public List<Fish> getFishList() { return fishList; }
    public List<EggObject> getEggObjects() { return eggObjects; }
    public List<FoodPellet> getFoodPellets() { return foodPellets; }
    public Map<Food, Integer> getFoodInventory() { return foodInventory; }
    public double getMoney() { return money; }
    public void setMoney(double money) { this.money = money; }
    public int getPearls() { return pearls; }
    public void setPearls(int pearls) { this.pearls = pearls; }

    // --- Save/Load Logic ---

    private static class SaveData {
        double money;
        int pearls;
        List<FishSave> fish;
        List<DecorSave> decor;
        Map<String, Integer> inventory;
        List<EggSave> eggs;
        List<String> activeFood;
    }

    private static class FishSave {
        String name;
        String breed;
        int fillValue;
        float x, y;
    }

    private static class DecorSave {
        String type;
        float x, y;
        float scale;
        int slotIndex;
        float xPercent;
        double purchasePrice;
    }

    private static class EggSave {
        String breed;
        int quantity;
        double price;
        boolean premium;
        float x, y;
    }

    public void save() {
        SaveData data = new SaveData();
        data.money = this.money;
        data.pearls = this.pearls;

        data.fish = new ArrayList<>();
        for (Fish f : fishList) {
            FishSave fs = new FishSave();
            fs.name = f.getName();
            fs.breed = f.getBreed();
            fs.fillValue = f.getFillValue();
            fs.x = f.getBounds().x; // Using bounds.x for position
            fs.y = f.getBounds().y;
            data.fish.add(fs);
        }

        data.decor = new ArrayList<>();
        for (Decor d : decorItems) {
            DecorSave ds = new DecorSave();
            ds.type = d.getType();
            ds.x = d.getPosition().x;
            ds.y = d.getPosition().y;
            ds.scale = d.getScale();
            ds.slotIndex = d.getSlotIndex();
            ds.xPercent = d.getXPercent();
            ds.purchasePrice = d.getPurchasePrice();
            data.decor.add(ds);
        }

        data.inventory = new HashMap<>();
        for (Map.Entry<Food, Integer> entry : foodInventory.entrySet()) {
            data.inventory.put(entry.getKey().type(), entry.getValue());
        }

        data.eggs = new ArrayList<>();
        for (EggObject eo : eggObjects) {
            EggSave es = new EggSave();
            es.breed = eo.getEggData().breed();
            es.quantity = eo.getEggData().quantity();
            es.price = eo.getEggData().price();
            es.premium = eo.getEggData().isPremium();
            es.x = eo.getPosition().x;
            es.y = eo.getPosition().y;
            data.eggs.add(es);
        }

        data.activeFood = new ArrayList<>();
        for (FoodPellet pellet : foodPellets) {
            data.activeFood.add(pellet.getFoodType().type());
        }

        String json = new Gson().toJson(data);
        Preferences prefs = Gdx.app.getPreferences("FishTankGameSave");
        prefs.putString("saveData", json);
        prefs.flush();
    }

    public boolean load() {
        Preferences prefs = Gdx.app.getPreferences("FishTankGameSave");
        String json = prefs.getString("saveData", null);
        if (json == null) return false;

        try {
            SaveData data = new Gson().fromJson(json, SaveData.class);
            this.money = data.money;
            this.pearls = data.pearls;

            this.fishList.clear();
            if (data.fish != null) {
                for (FishSave fs : data.fish) {
                    Texture tex = fishTextures.get(fs.breed);
                    if (tex != null) {
                        FishBreed breedInfo = FishBreed.fromName(fs.breed);
                        Fish fish = new Fish(fs.name, fs.breed, breedInfo.getSellPrice(), breedInfo.getSpeed(), tex, breedInfo.getMaxFillValue(), this, new Vector2(fs.x, fs.y));
                        // We need a way to set fillValue directly or feed it
                        fish.feed(fs.fillValue);
                        this.fishList.add(fish);
                    }
                }
            }

            this.decorItems.clear();
            if (data.decor != null) {
                for (DecorSave ds : data.decor) {
                    Texture tex = decorTextures.get(ds.type);
                    if (tex != null) {
                        this.decorItems.add(new Decor(ds.type, tex, ds.x, ds.y, ds.scale, ds.slotIndex, ds.xPercent, ds.purchasePrice));
                    }
                }
            }

            this.foodInventory.clear();
            if (data.inventory != null) {
                // Reconstruct Food objects - this is a bit tricky as we don't have all Food data here
                // We'll use defaults for now or lookup from a list if we had one.
                // Assuming Sunflower, Poppy, etc. are standard.
                for (Map.Entry<String, Integer> entry : data.inventory.entrySet()) {
                    Food food = createFoodByType(entry.getKey());
                    this.foodInventory.put(food, entry.getValue());
                }
            }

            this.eggObjects.clear();
            if (data.eggs != null) {
                for (EggSave es : data.eggs) {
                    this.eggObjects.add(new EggObject(new Egg(es.breed, es.quantity, es.price, es.premium), eggTexture, es.x, es.y));
                }
            }

            this.foodPellets.clear();
            if (data.activeFood != null) {
                for (String foodType : data.activeFood) {
                    Food food = createFoodByType(foodType);
                    float x = MathUtils.random(tankWidth * 0.2f, tankWidth * 0.8f);
                    float y = MathUtils.random(480, tankHeight - 16);
                    foodPellets.add(new FoodPellet(food, bubbleTexture, x, y));
                }
            }

            return true;
        } catch (Exception e) {
            Gdx.app.error("GameManager", "Failed to load save data", e);
            return false;
        }
    }

    private Food createFoodByType(String type) {
        if (type.equals("Sunflower")) return new Food("Sunflower", 3, 10.0, false);
        if (type.equals("Poppy")) return new Food("Poppy", 6, 20.0, false);
        if (type.equals("Flax")) return new Food("Flax", 8, 30.0, false);
        if (type.equals("Sesame")) return new Food("Sesame", 10, 40.0, false);
        if (type.equals("Chia")) return new Food("Chia", 15, 50.0, false);
        if (type.equals("Hemp")) return new Food("Hemp", 22, 75.0, false);
        if (type.equals("Pumpkin")) return new Food("Pumpkin", 50, 20.0, true);
        if (type.equals("Quinoa")) return new Food("Quinoa", 75, 40.0, true);
        return new Food(type, 1, 1.0, false);
    }
}
