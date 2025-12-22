package com.fishtankgame.game;

import com.fishtankgame.model.Decor;
import com.fishtankgame.model.Egg;
import com.fishtankgame.model.Fish;
import com.fishtankgame.model.FishBreed;
import com.fishtankgame.model.Food;

public class Shop {
    private final GameManager gameManager;
    private static final double PEARL_TO_MONEY_RATE = 200.0;

    public Shop(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public void buyEgg(Egg egg) {
        if (egg.isPremium()) {
            if (gameManager.getPearls() >= egg.price()) {
                gameManager.setPearls(gameManager.getPearls() - (int) egg.price());
                gameManager.dropEgg(egg);
            }
        } else {
            if (gameManager.getMoney() >= egg.price()) {
                gameManager.setMoney(gameManager.getMoney() - egg.price());
                gameManager.dropEgg(egg);
            }
        }
    }

    public void buyFood(Food food) {
        int quantity = 10;

        if (food.isPremium()) {
            if (gameManager.getPearls() >= food.getPrice()) {
                gameManager.setPearls(gameManager.getPearls() - (int) food.getPrice());
                gameManager.addFood(food, quantity);
            }
        } else {
            if (gameManager.getMoney() >= food.getPrice()) {
                gameManager.setMoney(gameManager.getMoney() - food.getPrice());
                gameManager.addFood(food, quantity);
            }
        }
    }

    public void buyDecor(String name, double price, int slotIndex) {
        buyDecor(name, price, slotIndex, false);
    }

    public void buyDecor(String name, double price, int slotIndex, boolean isPremium) {
        if (slotIndex == -1) {
            for (Decor d : gameManager.getDecorItems()) {
                if (d.getType().equals(name)) return;
            }
        }

        if (isPremium) {
            if (gameManager.getPearls() >= price) {
                gameManager.setPearls(gameManager.getPearls() - (int) price);
                gameManager.addDecor(name, price, slotIndex);
            }
        } else {
            if (gameManager.getMoney() >= price) {
                gameManager.setMoney(gameManager.getMoney() - price);
                gameManager.addDecor(name, price, slotIndex);
            }
        }
    }

    public void sellDecor(Decor decor) {
        double sellPrice;
        if (decor.getType().equals("Bubbler") || decor.getType().equals("EggPus")) {
            // Premium decor sells for money at exchange rate
            sellPrice = Math.floor(decor.getPurchasePrice() * PEARL_TO_MONEY_RATE / 2.0);
        } else {
            sellPrice = Math.floor(decor.getPurchasePrice() / 2.0);
        }
        gameManager.setMoney(gameManager.getMoney() + sellPrice);
        gameManager.getDecorItems().remove(decor);
    }

    public Fish sellFish(Fish fish) {
        if (fish.isAdult()) {
            FishBreed breed = FishBreed.fromName(fish.getBreed());
            gameManager.setMoney(gameManager.getMoney() + breed.getSellPrice());
            gameManager.getFishList().remove(fish);
            return fish;
        }
        return null;
    }
}
