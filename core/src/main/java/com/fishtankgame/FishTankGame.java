package com.fishtankgame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.fishtankgame.effects.Bubble;
import com.fishtankgame.game.GameManager;
import com.fishtankgame.game.Shop;
import com.fishtankgame.model.Fish;
import com.fishtankgame.model.FishBreed;
import com.fishtankgame.ui.GameScreen;
import com.fishtankgame.ui.ShopScreen;

import java.util.ArrayList;
import java.util.List;

public class FishTankGame extends Game {
    public static final float VIRTUAL_WIDTH = 1920;
    public static final float VIRTUAL_HEIGHT = 1080;

    private final PlatformInterface platform;
    private SpriteBatch batch;
    private GameManager gameManager;
    private Shop shop;
    private GameScreen gameScreen;
    private ShopScreen shopScreen;
    private Skin skin;

    private Texture background;
    private List<Bubble> bubbles;
    private List<Texture> texturesToDispose;

    private Sound waterAmbient;
    private long waterAmbientId = -1;

    private Sound bubblerSound;
    private long bubblerSoundId = -1;

    public FishTankGame(PlatformInterface platform) {
        this.platform = platform;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        texturesToDispose = new ArrayList<>();
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        Texture bubbleTexture = loadTexture("bubble.png");
        gameManager = new GameManager(bubbleTexture);
        shop = new Shop(gameManager);

        background = loadTexture("background.png");

        // Load Egg Texture
        gameManager.setEggTexture(loadTexture("TF2Coconut.png"));

        // Load Sounds
        waterAmbient = Gdx.audio.newSound(Gdx.files.internal("sounds/water_sound.wav"));
        bubblerSound = Gdx.audio.newSound(Gdx.files.internal("sounds/bubbler.wav"));

        // Load Fish Textures
        gameManager.addFishTexture("Goldfish", loadTexture("fish/Goldfish.png"));
        gameManager.addFishTexture("Blue Tang", loadTexture("fish/Bluetang.png"));
        gameManager.addFishTexture("Angelfish", loadTexture("fish/Angelfish.png"));
        gameManager.addFishTexture("Betafish", loadTexture("fish/Betafish.png"));
        gameManager.addFishTexture("Clownfish", loadTexture("fish/Clownfish.png"));
        gameManager.addFishTexture("Tigerfish", loadTexture("fish/Tigerfish.png"));
        gameManager.addFishTexture("Koi", loadTexture("fish/Koifish.png"));
        gameManager.addFishTexture("Rainbowfish", loadTexture("fish/rainbowfish.png"));
        gameManager.addFishTexture("Tetra", loadTexture("fish/Tetra.png"));
        gameManager.addFishTexture("Snail", loadTexture("fish/Snail.png"));
        gameManager.addFishTexture("Blue Guppy", loadTexture("fish/Blue Guppy.png"));
        gameManager.addFishTexture("Gemfish", loadTexture("fish/GemFish.png"));
        gameManager.addFishTexture("Platinum Arowana", loadTexture("fish/Platinum Arowana.png"));
        gameManager.addFishTexture("Starfish", loadTexture("fish/StarTop.png"));

        // Load Decor Textures
        Texture fernTex = loadTexture("decor/plant1.png");
        gameManager.initDecor(
                loadTexture("decor/chest.png"),
                loadTexture("decor/bubbler.png"),
                loadTexture("decor/plant1.png"),
                loadTexture("decor/plant2.png"),
                loadTexture("decor/plant3.png"),
                loadTexture("decor/plant4.png"),
                loadTexture("decor/YardRock.png"),
                loadTexture("decor/EggPus.png")
        );

        // Attempt to load saved game
        boolean loaded = gameManager.load();

        if (!loaded) {
            // --- Starting Game Status (Only if no save exists) ---
            // 1. Initial Fern in position 8
            gameManager.addPlantToSlot("Green Fern", fernTex, 8, 1.2f, 10.0);

            // 2. Initial Baby Goldfish
            FishBreed goldfish = FishBreed.GOLDFISH;
            gameManager.getFishList().add(new Fish("Starter Goldy", goldfish.getName(), 15.0, goldfish.getSpeed(), gameManager.getFishTexture(goldfish.getName()), goldfish.getMaxFillValue(), gameManager));

            // 3. Initial Baby Angelfish
            FishBreed angelfish = FishBreed.ANGELFISH;
            gameManager.getFishList().add(new Fish("Starter Angel", angelfish.getName(), 60.0, angelfish.getSpeed(), gameManager.getFishTexture(angelfish.getName()), angelfish.getMaxFillValue(), gameManager));
        }

        bubbles = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            float x = MathUtils.random(0, VIRTUAL_WIDTH);
            float y = MathUtils.random(-VIRTUAL_HEIGHT, VIRTUAL_HEIGHT);
            bubbles.add(new Bubble(bubbleTexture, x, y, VIRTUAL_HEIGHT));
        }

        gameScreen = new GameScreen(this, gameManager, shop, batch, bubbles, background, skin);
        shopScreen = new ShopScreen(this, shop, gameManager, skin);

        // Start background ambient sound immediately
        startAmbientSound();

        setScreen(gameScreen);
    }

    private Texture loadTexture(String path) {
        Texture tex = new Texture(path);
        texturesToDispose.add(tex);
        return tex;
    }

    public void startAmbientSound() {
        if (waterAmbientId == -1) {
            waterAmbientId = waterAmbient.loop(0.15f); // Very low background hum
        }
    }

    public void startBubblerSound() {
        if (bubblerSoundId == -1) {
            bubblerSoundId = bubblerSound.loop(0.35f); // Louder bubbling effect
        }
    }

    public void stopBubblerSound() {
        if (bubblerSoundId != -1) {
            bubblerSound.stop(bubblerSoundId);
            bubblerSoundId = -1;
        }
    }

    public void showShopScreen() {
        setScreen(shopScreen);
    }

    public void showGameScreen() {
        setScreen(gameScreen);
    }

    public PlatformInterface getPlatform() {
        return platform;
    }

    @Override
    public void pause() {
        super.pause();
        if (gameManager != null) {
            gameManager.save();
        }
    }

    @Override
    public void dispose() {
        if (gameManager != null) {
            gameManager.save();
        }
        batch.dispose();
        skin.dispose();
        for (Texture tex : texturesToDispose) {
            tex.dispose();
        }
        waterAmbient.dispose();
        bubblerSound.dispose();
        gameScreen.dispose();
        shopScreen.dispose();
    }
}
