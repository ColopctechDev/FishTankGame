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
    private Viewport viewport;
    private Skin skin;
    private Label moneyLabel;
    private Label inventoryLabel;

    private Table mainTable;
    private TextButton showUiButton;

    public GameScreen(FishTankGame game, GameManager gameManager, Shop shop, SpriteBatch batch, List<Bubble> bubbles, Texture background) {
        this.game = game;
        this.gameManager = gameManager;
        this.shop = shop;
        this.batch = batch;
        this.bubbles = bubbles;
        this.background = background;

        viewport = new ExtendViewport(FishTankGame.VIRTUAL_WIDTH, FishTankGame.VIRTUAL_HEIGHT);
        stage = new Stage(viewport, batch);
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        // Use a Table for layout
        mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.top(); // Align table to the top of the screen
        stage.addActor(mainTable);

        // --- Top Left Stats Panel ---
        Table statsTable = new Table();
        statsTable.pad(20);

        moneyLabel = new Label("", skin);
        moneyLabel.setFontScale(3.6f);
        statsTable.add(moneyLabel).left().row();

        inventoryLabel = new Label("", skin);
        inventoryLabel.setFontScale(3.0f);
        statsTable.add(inventoryLabel).left().padTop(10);

        // --- Top Right Buttons Panel ---
        Table buttonTable = new Table();
        buttonTable.pad(20);

        float buttonScale = 3.75f;

        // Create a style without background for the main screen buttons
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

        TextButton hideUiButton = new TextButton("Hide UI", noBgStyle);
        hideUiButton.getLabel().setFontScale(buttonScale);
        hideUiButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                mainTable.setVisible(false);
                showUiButton.setVisible(true);
            }
        });

        // Add buttons with padding between them
        float verticalSpace = 50f;
        buttonTable.add(shopButton).fillX().uniformX().padBottom(verticalSpace).row();
        buttonTable.add(dropFoodButton).fillX().uniformX().padBottom(verticalSpace).row();
        buttonTable.add(sellButton).fillX().uniformX().padBottom(verticalSpace).row();
        buttonTable.add(hideUiButton).fillX().uniformX();

        // Add both panels to the main table
        mainTable.add(statsTable).top().left().expandX().pad(20);
        mainTable.add(buttonTable).top().right().pad(20);

        // --- Hidden "Show UI" (X) Button ---
        // We use the default style for this one so it has a visible box as requested
        showUiButton = new TextButton("X", skin);
        showUiButton.getLabel().setFontScale(2.5f);
        showUiButton.setSize(80, 80);
        showUiButton.setPosition(viewport.getWorldWidth() - 100, viewport.getWorldHeight() - 100);
        showUiButton.setVisible(false);
        showUiButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                mainTable.setVisible(true);
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
        // Background should cover the extended area
        batch.draw(background, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        for (Bubble bubble : bubbles) {
            bubble.update();
            bubble.draw(batch);
        }

        for (FoodPellet pellet : gameManager.getFoodPellets()) {
            pellet.update(delta);
            pellet.draw(batch);
        }

        for (EggObject egg : gameManager.getEggObjects()) {
            egg.update(delta);
            egg.draw(batch);
        }

        for (Fish fish : gameManager.getFishList()) {
            fish.draw(batch);
        }
        batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        gameManager.updateTankSize(viewport.getWorldWidth(), viewport.getWorldHeight());
        // Reposition the X button on resize
        showUiButton.setPosition(viewport.getWorldWidth() - 100, viewport.getWorldHeight() - 100);
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
