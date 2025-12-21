package com.fishtankgame.game;

import com.fishtankgame.model.Decor;
import com.fishtankgame.model.Egg;
import com.fishtankgame.model.Fish;
import com.fishtankgame.model.Food;

public class Shop {
    private final GameManager gameManager;

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
        int quantity = 10; // Default
        if (food.getType().equals("Sunflower")) quantity = 30;
        else if (food.getType().equals("Poppy")) quantity = 20;
        else if (food.getType().equals("Flax")) quantity = 15;
        else if (food.getType().equals("Hemp") || food.getType().equals("Chia") || food.getType().equals("Sesame")) quantity = 15;
        else if (food.getType().equals("Pumpkin") || food.getType().equals("Quinoa")) quantity = 10;

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
        // Prevent buying multiple Bubblers or Chests or EggPus
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
        // Check if it's a pearl item (Bubbler, EggPus) - maybe sell for pearls?
        // For now, keep it simple and sell for half price in money or just remove if premium
        // User didn't specify, but usually premium items might return pearls or just half money.
        // Let's stick to money for now or check if it was bought with pearls.
        // Actually, the original code used getPurchasePrice() / 2.0.
        // If it was a pearl item, maybe return some pearls?
        // Bubbler was $150, now it's pearls.

        // I'll assume premium items return 0 money for now or stay as they are.
        // Let's just follow the existing pattern but maybe check if price was high.
        double sellPrice = Math.floor(decor.getPurchasePrice() / 2.0);
        gameManager.setMoney(gameManager.getMoney() + sellPrice);
        gameManager.getDecorItems().remove(decor);
    }

    public Fish sellFish(Fish fish) {
        if (fish.isAdult()) {
            gameManager.setMoney(gameManager.getMoney() + fish.getPrice());
            gameManager.getFishList().remove(fish);
            return fish;
        }
        return null;
    }
}
