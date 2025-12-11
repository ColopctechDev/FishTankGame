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
    private List<Bubble> bubbles;

    @Override
    public void create() {
        batch = new SpriteBatch();
        gameManager = new GameManager();
        shop = new Shop(gameManager);

        // Load assets
        background = new Texture("background.png");
        bubbleTexture = new Texture("bubble.png");
        waterSound = com.badlogic.gdx.Gdx.audio.newSound(com.badlogic.gdx.Gdx.files.internal("water_sound.wav"));
        blueTangTexture = new Texture("Fish1.png");
        goldfishTexture = new Texture("Fish2.png");

        // Add fish textures to GameManager
        gameManager.addFishTexture("Blue Tang", blueTangTexture);
        gameManager.addFishTexture("Goldfish", goldfishTexture);

        waterSound.loop();

        bubbles = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            bubbles.add(new Bubble(bubbleTexture));
        }

        // Create initial fish
        gameManager.getFishList().add(new Fish("Dory", "Blue Tang", 15.0, 1.5f, blueTangTexture));
        gameManager.getFishList().add(new Fish("Nemo", "Goldfish", 10.0, 1.0f, goldfishTexture));

        // Create screens
        gameScreen = new GameScreen(this, gameManager, batch, bubbles, background);
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
        gameScreen.dispose();
        shopScreen.dispose();
    }
}
