package com.fishtankgame.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.fishtankgame.FishTankGame;
import com.fishtankgame.game.GameManager;
import com.fishtankgame.game.Shop;
import com.fishtankgame.model.Egg;
import com.fishtankgame.model.Food;

public class ShopScreen extends ScreenAdapter {
    private FishTankGame game;
    private Stage stage;
    private Skin skin;
    private Shop shop;
    private GameManager gameManager;
    private Label moneyLabel;

    public ShopScreen(FishTankGame game, Shop shop, GameManager gameManager) {
        this.game = game;
        this.shop = shop;
        this.gameManager = gameManager;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("uiskin.json"));

        // Labels
        moneyLabel = new Label("Money: $" + gameManager.getMoney(), skin);
        moneyLabel.setPosition(100, 650);
        stage.addActor(moneyLabel);

        Label buyEggsLabel = new Label("Buy Eggs", skin);
        buyEggsLabel.setPosition(100, 550);
        stage.addActor(buyEggsLabel);

        Label buyFoodLabel = new Label("Buy Food", skin);
        buyFoodLabel.setPosition(400, 550);
        stage.addActor(buyFoodLabel);

        // Egg Buttons
        TextButton buyBlueTangButton = new TextButton("Buy Blue Tang Egg ($15)", skin);
        buyBlueTangButton.setPosition(100, 500);
        buyBlueTangButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                shop.buyEgg(new Egg("Blue Tang", 5, 15.0));
                updateMoneyLabel();
            }
        });
        stage.addActor(buyBlueTangButton);

        TextButton buyGoldfishButton = new TextButton("Buy Goldfish Egg ($5)", skin);
        buyGoldfishButton.setPosition(100, 450);
        buyGoldfishButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                shop.buyEgg(new Egg("Goldfish", 5, 5.0));
                updateMoneyLabel();
            }
        });
        stage.addActor(buyGoldfishButton);

        // Food Buttons
        TextButton buySunflowerButton = new TextButton("Buy Sunflower Seeds - $1", skin);
        buySunflowerButton.setPosition(400, 500);
        buySunflowerButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                shop.buyFood(new Food("Sunflower", 10, 1.0));
                updateMoneyLabel();
            }
        });
        stage.addActor(buySunflowerButton);

        TextButton buyPoppyButton = new TextButton("Buy Poppy Seeds - $3", skin);
        buyPoppyButton.setPosition(400, 450);
        buyPoppyButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                shop.buyFood(new Food("Poppy", 20, 3.0));
                updateMoneyLabel();
            }
        });
        stage.addActor(buyPoppyButton);

        TextButton buyFlaxButton = new TextButton("Buy Flax Seeds - $5", skin);
        buyFlaxButton.setPosition(400, 400);
        buyFlaxButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                shop.buyFood(new Food("Flax", 30, 5.0));
                updateMoneyLabel();
            }
        });
        stage.addActor(buyFlaxButton);

        TextButton buySesameButton = new TextButton("Buy Sesame Seeds - $7", skin);
        buySesameButton.setPosition(400, 350);
        buySesameButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                shop.buyFood(new Food("Sesame", 40, 7.0));
                updateMoneyLabel();
            }
        });
        stage.addActor(buySesameButton);

        TextButton buyChiaButton = new TextButton("Buy Chia Seeds - $10", skin);
        buyChiaButton.setPosition(400, 300);
        buyChiaButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                shop.buyFood(new Food("Chia", 50, 10.0));
                updateMoneyLabel();
            }
        });
        stage.addActor(buyChiaButton);

        TextButton backButton = new TextButton("Back to Tank", skin);
        backButton.setPosition(1100, 650);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.showGameScreen();
            }
        });
        stage.addActor(backButton);
    }

    @Override
    public void show() {
        updateMoneyLabel();
        Gdx.input.setInputProcessor(stage);
    }

    private void updateMoneyLabel() {
        moneyLabel.setText("Money: $" + String.format("%.2f", gameManager.getMoney()));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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
