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
        if (gameManager.getMoney() >= egg.getPrice()) {
            gameManager.setMoney(gameManager.getMoney() - egg.getPrice());
            gameManager.dropEgg(egg);
        }
    }

    public void buyFood(Food food) {
        if (gameManager.getMoney() >= food.getPrice()) {
            gameManager.setMoney(gameManager.getMoney() - food.getPrice());
            gameManager.addFood(food, 5);
        }
    }

    public void buyDecor(String name, double price, int slotIndex) {
        if (gameManager.getMoney() >= price) {
            gameManager.setMoney(gameManager.getMoney() - price);
            gameManager.addDecor(name, price, slotIndex);
        }
    }

    public void sellDecor(Decor decor) {
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
