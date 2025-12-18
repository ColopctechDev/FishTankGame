package com.fishtankgame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.fishtankgame.effects.Bubble;
import com.fishtankgame.game.GameManager;
import com.fishtankgame.game.Shop;
import com.fishtankgame.model.Fish;
import com.fishtankgame.ui.GameScreen;
import com.fishtankgame.ui.ShopScreen;

import java.util.ArrayList;
import java.util.List;

public class FishTankGame extends Game {
    public static final float VIRTUAL_WIDTH = 1280;
    public static final float VIRTUAL_HEIGHT = 720;

    private SpriteBatch batch;
    private GameManager gameManager;
    private Shop shop;

    private GameScreen gameScreen;
    private ShopScreen shopScreen;

    // Assets
    private Texture background;
    private Texture bubbleTexture;
    private Sound waterSound;
    private Texture blueTangTexture;
    private Texture goldfishTexture;
    private Texture angelfishTexture;
    private Texture betafishTexture;
    private Texture clownfishTexture;
    private Texture tigerfishTexture;
    private List<Bubble> bubbles;

    @Override
    public void create() {
        batch = new SpriteBatch();

        // Load assets
        background = new Texture("background.png");
        bubbleTexture = new Texture("bubble.png");
        waterSound = com.badlogic.gdx.Gdx.audio.newSound(com.badlogic.gdx.Gdx.files.internal("water_sound.wav"));
        blueTangTexture = new Texture("Bluetang.png");
        goldfishTexture = new Texture("Goldfish.png");
        angelfishTexture = new Texture("Angelfish.png");
        betafishTexture = new Texture("Betafish.png");
        clownfishTexture = new Texture("Clownfish.png");
        tigerfishTexture = new Texture("Tigerfish.png");

        gameManager = new GameManager(bubbleTexture);
        shop = new Shop(gameManager);

        // Add fish textures to GameManager
        gameManager.addFishTexture("Blue Tang", blueTangTexture);
        gameManager.addFishTexture("Goldfish", goldfishTexture);
        gameManager.addFishTexture("Angelfish", angelfishTexture);
        gameManager.addFishTexture("Betafish", betafishTexture);
        gameManager.addFishTexture("Clownfish", clownfishTexture);
        gameManager.addFishTexture("Tigerfish", tigerfishTexture);

        waterSound.loop();

        bubbles = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            bubbles.add(new Bubble(bubbleTexture));
        }

        // Create initial fish for testing
        gameManager.getFishList().add(new Fish("Goldie", "Goldfish", 10.0, 0.8f, goldfishTexture, 80, gameManager));
        gameManager.getFishList().add(new Fish("Dory", "Blue Tang", 15.0, 1.0f, blueTangTexture, 100, gameManager));
        gameManager.getFishList().add(new Fish("Angel", "Angelfish", 20.0, 0.9f, angelfishTexture, 120, gameManager));
        gameManager.getFishList().add(new Fish("Beta", "Betafish", 25.0, 1.2f, betafishTexture, 150, gameManager));
        gameManager.getFishList().add(new Fish("Nemo", "Clownfish", 30.0, 1.1f, clownfishTexture, 180, gameManager));
        gameManager.getFishList().add(new Fish("Tigger", "Tigerfish", 50.0, 1.8f, tigerfishTexture, 250, gameManager));

        // Create screens
        gameScreen = new GameScreen(this, gameManager, shop, batch, bubbles, background);
        shopScreen = new ShopScreen(this, shop, gameManager);

        // Set the initial screen
        setScreen(gameScreen);
    }

    public void showGameScreen() {
        setScreen(gameScreen);
    }

    public void showShopScreen() {
        setScreen(shopScreen);
    }

    @Override
    public void dispose() {
        batch.dispose();
        background.dispose();
        bubbleTexture.dispose();
        waterSound.dispose();
        blueTangTexture.dispose();
        goldfishTexture.dispose();
        angelfishTexture.dispose();
        betafishTexture.dispose();
        clownfishTexture.dispose();
        tigerfishTexture.dispose();
        gameScreen.dispose();
        shopScreen.dispose();
    }
}
