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
import com.fishtankgame.model.Fish;

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

        moneyLabel = new Label("Money: $" + gameManager.getMoney(), skin);
        moneyLabel.setPosition(100, 600);
        stage.addActor(moneyLabel);

        TextButton buyBlueTangButton = new TextButton("Buy Blue Tang Egg ($15)", skin);
        buyBlueTangButton.setPosition(100, 400);
        buyBlueTangButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                shop.buyEgg(new Egg("Blue Tang", 5, 15.0));
                moneyLabel.setText("Money: $" + gameManager.getMoney());
            }
        });
        stage.addActor(buyBlueTangButton);

        TextButton buyGoldfishButton = new TextButton("Buy Goldfish Egg ($5)", skin);
        buyGoldfishButton.setPosition(100, 300);
        buyGoldfishButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                shop.buyEgg(new Egg("Goldfish", 5, 5.0));
                moneyLabel.setText("Money: $" + gameManager.getMoney());
            }
        });
        stage.addActor(buyGoldfishButton);

        TextButton sellFishButton = new TextButton("Sell Adult Fish", skin);
        sellFishButton.setPosition(100, 200);
        sellFishButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                for (Fish fish : gameManager.getFishList()) {
                    if (fish.isAdult()) {
                        shop.sellFish(fish);
                        break; // Sell one fish at a time
                    }
                }
                moneyLabel.setText("Money: $" + gameManager.getMoney());
            }
        });
        stage.addActor(sellFishButton);

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
        moneyLabel.setText("Money: $" + gameManager.getMoney());
        Gdx.input.setInputProcessor(stage);
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
