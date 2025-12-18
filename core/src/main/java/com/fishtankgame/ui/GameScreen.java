package com.fishtankgame.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.fishtankgame.FishTankGame;
import com.fishtankgame.effects.Bubble;
import com.fishtankgame.game.GameManager;
import com.fishtankgame.game.Shop;
import com.fishtankgame.model.Fish;
import com.fishtankgame.model.Food;
import com.fishtankgame.model.FoodPellet;
import com.fishtankgame.model.EggObject;
import com.fishtankgame.model.Decor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameScreen extends ScreenAdapter {
    private FishTankGame game;
    private SpriteBatch batch;
    private GameManager gameManager;
    private Shop shop;

    private Texture background;
    private List<Bubble> backgroundBubbles;

    private Stage stage;
    private Viewport viewport;
    private Skin skin;
    private Label moneyLabel;
    private Label inventoryLabel;

    private Table uiRoot;
    private TextButton showUiButton;

    public GameScreen(FishTankGame game, GameManager gameManager, Shop shop, SpriteBatch batch, List<Bubble> backgroundBubbles, Texture background, Skin skin) {
        this.game = game;
        this.gameManager = gameManager;
        this.shop = shop;
        this.batch = batch;
        this.backgroundBubbles = backgroundBubbles;
        this.background = background;
        this.skin = skin;

        viewport = new ExtendViewport(FishTankGame.VIRTUAL_WIDTH, FishTankGame.VIRTUAL_HEIGHT);
        stage = new Stage(viewport, batch);

        // Common translucent background (slightly darker for better contrast)
        com.badlogic.gdx.scenes.scene2d.utils.Drawable translucentBg = skin.newDrawable("white", new Color(0, 0, 0, 0.5f));

        // --- Main UI Root Table ---
        uiRoot = new Table();
        uiRoot.setFillParent(true);
        uiRoot.pad(50);
        stage.addActor(uiRoot);

        // --- Inventory (Top Left) ---
        inventoryLabel = new Label("", skin);
        inventoryLabel.setFontScale(3.0f);
        Table inventoryTable = new Table();
        inventoryTable.setBackground(translucentBg);
        inventoryTable.add(inventoryLabel).pad(20);

        // --- Buttons (Top Right) ---
        Table buttonTable = new Table();
        buttonTable.setBackground(translucentBg);
        buttonTable.pad(20);

        float buttonScale = 3.75f;
        TextButton.TextButtonStyle noBgStyle = new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));
        noBgStyle.up = null;
        noBgStyle.down = null;
        noBgStyle.over = null;
        noBgStyle.focused = null;

        TextButton shopButton = new TextButton("Open Shop", noBgStyle);
        shopButton.getLabel().setFontScale(buttonScale);
        shopButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.showShopScreen();
            }
        });

        TextButton dropFoodButton = new TextButton("Drop Food", noBgStyle);
        dropFoodButton.getLabel().setFontScale(buttonScale);
        dropFoodButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameManager.dropFood();
            }
        });

        TextButton sellButton = new TextButton("Sell Adult Fish", noBgStyle);
        sellButton.getLabel().setFontScale(buttonScale);
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

        float verticalSpace = 50f;
        buttonTable.add(shopButton).fillX().uniformX().pad(10).padBottom(verticalSpace).row();
        buttonTable.add(dropFoodButton).fillX().uniformX().pad(10).padBottom(verticalSpace).row();
        buttonTable.add(sellButton).fillX().uniformX().pad(10);

        // --- Money (Bottom Left) ---
        moneyLabel = new Label("", skin);
        moneyLabel.setFontScale(3.6f);
        Table moneyTable = new Table();
        moneyTable.setBackground(translucentBg);
        moneyTable.add(moneyLabel).pad(20);

        // --- Hide UI (Bottom Right) ---
        TextButton hideUiButton = new TextButton("Hide Text", noBgStyle);
        hideUiButton.getLabel().setFontScale(buttonScale);
        Table hideTable = new Table();
        hideTable.setBackground(translucentBg);
        hideTable.add(hideUiButton).pad(20);

        hideUiButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                uiRoot.setVisible(false);
                showUiButton.setVisible(true);
            }
        });

        // --- Arrange in uiRoot ---
        uiRoot.add(inventoryTable).top().left().expand();
        uiRoot.add(buttonTable).top().right().expand();
        uiRoot.row();
        uiRoot.add(moneyTable).bottom().left().expand();
        uiRoot.add(hideTable).bottom().right().expand();

        // --- Hidden "Show UI" (X) Button (Needs its own Table for background too) ---
        showUiButton = new TextButton("X", skin);
        showUiButton.getLabel().setFontScale(3.75f);
        showUiButton.setSize(120, 120);
        showUiButton.setVisible(false);
        showUiButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                uiRoot.setVisible(true);
                showUiButton.setVisible(false);
            }
        });
        stage.addActor(showUiButton);
    }

    @Override
    public void show() {
        updateMoneyLabel();
        updateInventoryDisplay();
        Gdx.input.setInputProcessor(stage);

        if (gameManager.isBubblerActive()) {
            game.startBubblerSound();
        }
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        game.stopBubblerSound();
    }

    private void updateMoneyLabel() {
        moneyLabel.setText("Balance: $" + String.format("%.2f", gameManager.getMoney()));
        moneyLabel.setColor(gameManager.getMoney() > 10 ? Color.WHITE : Color.RED);
    }

    private void updateInventoryDisplay() {
        StringBuilder inventoryText = new StringBuilder("Inventory:");
        for (Map.Entry<Food, Integer> entry : gameManager.getFoodInventory().entrySet()) {
            inventoryText.append("\n").append(entry.getKey().getType()).append(": ").append(entry.getValue());
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

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        batch.draw(background, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        for (Bubble bubble : backgroundBubbles) {
            bubble.update(delta);
            bubble.draw(batch);
        }

        for (Fish fish : gameManager.getFishList()) {
            if (fish.getBreed().equals("Clownfish") && fish.isBehindDecor()) {
                fish.draw(batch);
            }
        }

        for (Decor decor : gameManager.getDecorItems()) {
            if (decor.getSlotIndex() != -1) {
                decor.draw(batch);
            }
        }

        for (Fish fish : gameManager.getFishList()) {
            boolean isHidingClownfish = fish.getBreed().equals("Clownfish") && fish.isBehindDecor();
            if (!isHidingClownfish) {
                fish.draw(batch);
            }
        }

        for (Decor decor : gameManager.getDecorItems()) {
            if (decor.getSlotIndex() == -1) {
                decor.draw(batch);
            }
        }

        for (Bubble b : gameManager.getDecorBubbles()) {
            b.draw(batch);
        }

        for (FoodPellet pellet : gameManager.getFoodPellets()) {
            pellet.update(delta);
            pellet.draw(batch);
        }

        for (EggObject egg : gameManager.getEggObjects()) {
            egg.update(delta);
            egg.draw(batch);
        }

        batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        gameManager.updateTankSize(viewport.getWorldWidth(), viewport.getWorldHeight());
        showUiButton.setPosition(viewport.getWorldWidth() - 200, 50);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
