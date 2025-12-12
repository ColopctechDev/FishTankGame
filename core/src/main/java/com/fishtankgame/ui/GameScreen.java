package com.fishtankgame.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.fishtankgame.FishTankGame;
import com.fishtankgame.effects.Bubble;
import com.fishtankgame.game.GameManager;
import com.fishtankgame.game.Shop;
import com.fishtankgame.model.Fish;
import com.fishtankgame.model.Food;
import com.fishtankgame.model.FoodPellet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameScreen extends ScreenAdapter {
    private FishTankGame game;
    private SpriteBatch batch;
    private GameManager gameManager;
    private Shop shop;

    private Texture background;
    private List<Bubble> bubbles;

    private Stage stage;
    private Skin skin;
    private Label moneyLabel;
    private Label inventoryLabel;

    public GameScreen(FishTankGame game, GameManager gameManager, Shop shop, SpriteBatch batch, List<Bubble> bubbles, Texture background) {
        this.game = game;
        this.gameManager = gameManager;
        this.shop = shop;
        this.batch = batch;
        this.bubbles = bubbles;
        this.background = background;

        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        // Money Label
        moneyLabel = new Label("", skin);
        moneyLabel.setPosition(100, 680);
        stage.addActor(moneyLabel);

        // Inventory Label
        inventoryLabel = new Label("", skin);
        inventoryLabel.setPosition(100, 640);
        stage.addActor(inventoryLabel);

        // Buttons
        TextButton shopButton = new TextButton("Shop", skin);
        shopButton.setPosition(1100, 650);
        shopButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.showShopScreen();
            }
        });
        stage.addActor(shopButton);

        TextButton dropFoodButton = new TextButton("Drop Food", skin);
        dropFoodButton.setPosition(1100, 600);
        dropFoodButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameManager.dropFood();
            }
        });
        stage.addActor(dropFoodButton);

        TextButton sellButton = new TextButton("Sell Adult Fish", skin);
        sellButton.setPosition(1100, 550);
        sellButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                for (Fish fish : new ArrayList<>(gameManager.getFishList())) {
                    if (fish.isAdult()) {
                        Fish soldFish = shop.sellFish(fish);
                        if (soldFish != null) {
                            gameManager.handleSoldFish(soldFish);
                        }
                        break;
                    }
                }
            }
        });
        stage.addActor(sellButton);
    }

    @Override
    public void show() {
        updateMoneyLabel();
        updateInventoryDisplay();
        Gdx.input.setInputProcessor(stage);
    }

    private void updateMoneyLabel() {
        moneyLabel.setText("Money: $" + String.format("%.2f", gameManager.getMoney()));
    }

    private void updateInventoryDisplay() {
        StringBuilder inventoryText = new StringBuilder("Food Inventory:\n");
        for (Map.Entry<Food, Integer> entry : gameManager.getFoodInventory().entrySet()) {
            inventoryText.append(entry.getKey().getType()).append(": ").append(entry.getValue()).append("\n");
        }
        inventoryLabel.setText(inventoryText.toString());
    }

    @Override
    public void render(float delta) {
        updateMoneyLabel();
        updateInventoryDisplay();
        Gdx.gl.glClearColor(0, 0, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        gameManager.update(delta);

        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        for (Bubble bubble : bubbles) {
            bubble.update();
            bubble.draw(batch);
        }

        for (FoodPellet pellet : gameManager.getFoodPellets()) {
            pellet.update(delta);
            pellet.draw(batch);
        }

        for (Fish fish : gameManager.getFishList()) {
            fish.draw(batch);
        }
        batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
