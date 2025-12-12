package com.fishtankgame.game;

import com.fishtankgame.model.Egg;
import com.fishtankgame.model.Fish;
import com.fishtankgame.model.Food;

public class Shop {
    private GameManager gameManager;

    public Shop(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public void buyEgg(Egg egg) {
        if (gameManager.getMoney() >= egg.getPrice()) {
            gameManager.setMoney(gameManager.getMoney() - egg.getPrice());
            gameManager.getEggList().add(egg);
        }
    }

    public void buyFood(Food food) {
        if (gameManager.getMoney() >= food.getPrice()) {
            gameManager.setMoney(gameManager.getMoney() - food.getPrice());
            gameManager.addFood(food, 5); // Add a pack of 5
        }
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
