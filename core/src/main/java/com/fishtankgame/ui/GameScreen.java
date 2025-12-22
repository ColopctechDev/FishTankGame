package com.fishtankgame.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.fishtankgame.FishTankGame;
import com.fishtankgame.effects.Bubble;
import com.fishtankgame.game.GameManager;
import com.fishtankgame.game.Shop;
import com.fishtankgame.model.Fish;
import com.fishtankgame.model.Food;
import com.fishtankgame.model.FoodPellet;
import com.fishtankgame.model.Egg;
import com.fishtankgame.model.EggObject;
import com.fishtankgame.model.Decor;
import com.fishtankgame.model.FishBreed;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GameScreen extends ScreenAdapter {
    private final FishTankGame game;
    private final SpriteBatch batch;
    private final GameManager gameManager;
    private final Shop shop;

    private final Texture background;
    private final List<Bubble> backgroundBubbles;

    private final Stage stage;
    private final Viewport viewport;
    private final Skin skin;
    private final Label moneyLabel;
    private final Label pearlLabel;
    private final Label inventoryLabel;

    private final Table uiRoot;
    private final Table inventoryTable;
    private final Table currencyTable;
    private final TextButton showUiButton;

    private boolean showSplashOnStart = true;
    private final NumberFormat formatter = NumberFormat.getInstance(Locale.US);

    // Easter egg tracking
    private float lastTapX, lastTapY;
    private int consecutiveTapCount = 0;
    private static final float TAP_RADIUS = 60f;

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

        Preferences prefs = Gdx.app.getPreferences("FishTankGamePrefs");
        showSplashOnStart = prefs.getBoolean("showSplash", true);

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
        inventoryTable = new Table();
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
                gameManager.dropFood(false);
            }
        });

        TextButton dropPearlFoodButton = new TextButton("Drop Pearl Food", noBgStyle);
        dropPearlFoodButton.getLabel().setFontScale(buttonScale);
        dropPearlFoodButton.getLabel().setColor(new Color(0.7f, 0.9f, 1f, 1f));
        dropPearlFoodButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameManager.dropFood(true);
            }
        });

        float verticalSpace = 50f;
        buttonTable.add(shopButton).fillX().uniformX().pad(10).padBottom(verticalSpace).row();
        buttonTable.add(dropFoodButton).fillX().uniformX().pad(10).padBottom(verticalSpace).row();
        buttonTable.add(dropPearlFoodButton).fillX().uniformX().pad(10);

        // --- Money & Pearls (Bottom Left) ---
        moneyLabel = new Label("", skin);
        moneyLabel.setFontScale(3.6f);
        pearlLabel = new Label("", skin);
        pearlLabel.setFontScale(3.6f);
        pearlLabel.setColor(new Color(0.7f, 0.9f, 1f, 1f)); // Light blue for pearls

        currencyTable = new Table();
        currencyTable.setBackground(translucentBg);
        currencyTable.add(moneyLabel).pad(10).left().row();
        currencyTable.add(pearlLabel).pad(10).left();

        // --- Exit Button (Bottom Middle) ---
        TextButton exitButton = new TextButton("Exit Game", noBgStyle);
        exitButton.getLabel().setFontScale(buttonScale);
        exitButton.getLabel().setColor(Color.WHITE);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showExitConfirmation();
            }
        });
        Table exitTable = new Table();
        exitTable.setBackground(translucentBg);
        exitTable.add(exitButton).pad(20);

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

        // Use uniform columns by creating a wrapper table for the bottom row
        Table bottomRow = new Table();
        bottomRow.add(currencyTable).left().expandX();
        bottomRow.add(exitTable).center().expandX();
        bottomRow.add(hideTable).right().expandX();

        uiRoot.add(bottomRow).bottom().fillX().colspan(2);

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

        // --- Global Stage Listener for Tap Tracking ---
        stage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Determine region
                String region = "Tank";
                Actor target = event.getTarget();
                if (target != null) {
                    if (target.isDescendantOf(inventoryTable)) region = "Inventory";
                    else if (target.isDescendantOf(currencyTable)) region = "Balance";
                    else if (target.isDescendantOf(buttonTable) || target.isDescendantOf(exitTable) || target.isDescendantOf(hideTable)) {
                        // Taps on other UI buttons are ignored for the easter egg to avoid accidental triggers
                        consecutiveTapCount = 0;
                        return;
                    }
                }

                if (consecutiveTapCount == 0) {
                    lastTapX = x;
                    lastTapY = y;
                    consecutiveTapCount = 1;
                } else {
                    float dist = Vector2.dst(x, y, lastTapX, lastTapY);
                    if (dist < TAP_RADIUS) {
                        consecutiveTapCount++;
                        handleEasterEggProgress(consecutiveTapCount, region);
                    } else {
                        // Reset if tapped elsewhere
                        lastTapX = x;
                        lastTapY = y;
                        consecutiveTapCount = 1;
                    }
                }
            }
        });

        if (showSplashOnStart) {
            showSplashScreen();
        }
    }

    private void handleEasterEggProgress(int count, String region) {
        if (count == 20) {
            if (region.equals("Inventory")) {
                Gdx.app.log("GameScreen", "Easter Egg: Dropping one of every egg!");
                for (FishBreed breed : FishBreed.values()) {
                    gameManager.dropEgg(new Egg(breed.getName(), 1, breed.getBuyPrice(), breed.isPremium()));
                }
                consecutiveTapCount = 0; // Reset after trigger
            } else if (region.equals("Balance")) {
                Gdx.app.log("GameScreen", "Easter Egg: Added 1,000 pearls!");
                gameManager.setPearls(gameManager.getPearls() + 1000);
                consecutiveTapCount = 0; // Reset after trigger
            }
        }
    }

    private void showExitConfirmation() {
        final Dialog dialog = new Dialog("Exit Game", skin);
        dialog.getTitleLabel().setFontScale(2.5f);

        Label message = new Label("Are you sure you want to exit?", skin);
        message.setFontScale(2.0f);
        dialog.getContentTable().add(message).pad(40);

        TextButton okBtn = new TextButton("OK", skin);
        okBtn.getLabel().setFontScale(2.5f);
        okBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        TextButton cancelBtn = new TextButton("Cancel", skin);
        cancelBtn.getLabel().setFontScale(2.5f);
        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
            }
        });

        dialog.getButtonTable().add(okBtn).width(200).height(80).pad(20);
        dialog.getButtonTable().add(cancelBtn).width(200).height(80).pad(20);
        dialog.show(stage);
    }

    private void showSplashScreen() {
        final Dialog dialog = new Dialog("", skin);
        Table content = dialog.getContentTable();
        content.pad(100);

        // Heading: DIVE INTO THE FUN!
        Label title = new Label("DIVE INTO THE FUN!", skin, "subtitle");
        title.setFontScale(7.0f);
        title.setColor(Color.GOLD);
        content.add(title).padBottom(80).row();

        // Subheading 1: Buy & Sell
        Label sub1 = new Label("💰 Buy & Sell:", skin);
        sub1.setFontScale(5.0f);
        sub1.setColor(Color.CYAN);
        content.add(sub1).left().padBottom(10).row();

        Label desc1 = new Label("Trade fish for profit to upgrade your tank!", skin);
        desc1.setFontScale(2.8f);
        content.add(desc1).left().padBottom(40).row();

        // Subheading 2: Bubbles
        Label sub2 = new Label("🫧 Bubbles:", skin);
        sub2.setFontScale(5.0f);
        sub2.setColor(Color.CYAN);
        content.add(sub2).left().padBottom(10).row();

        Label desc2 = new Label("Look for the Scuba Guy to keep your fish happy.", skin);
        desc2.setFontScale(2.8f);
        content.add(desc2).left().padBottom(40).row();

        // Subheading 3: Free Eggs
        Label sub3 = new Label("🥚 Free Eggs:", skin);
        sub3.setFontScale(5.0f);
        sub3.setColor(Color.CYAN);
        content.add(sub3).left().padBottom(10).row();

        Label desc3 = new Label("The friendly Octopus has a surprise for you!", skin);
        desc3.setFontScale(2.8f);
        content.add(desc3).left().padBottom(80).row();

        Label footer = new Label("Click anywhere to play", skin);
        footer.setFontScale(3.5f);
        footer.setColor(Color.WHITE);
        content.add(footer).center().padBottom(60).row();

        final CheckBox dontShowAgain = new CheckBox(" Don't show this again", skin);
        dontShowAgain.getLabel().setFontScale(2.5f);
        content.add(dontShowAgain).center().padBottom(20).row();

        dialog.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (dontShowAgain.isChecked()) {
                    Preferences prefs = Gdx.app.getPreferences("FishTankGamePrefs");
                    prefs.putBoolean("showSplash", false);
                    prefs.flush();
                }
                dialog.hide();
            }
        });

        dialog.show(stage);
    }

    @Override
    public void show() {
        updateCurrencyLabels();
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

    private void updateCurrencyLabels() {
        moneyLabel.setText("Balance: $" + formatter.format(gameManager.getMoney()));
        moneyLabel.setColor(gameManager.getMoney() > 10 ? Color.WHITE : Color.RED);
        pearlLabel.setText("Pearls: " + formatter.format(gameManager.getPearls()));
    }

    private void updateInventoryDisplay() {
        StringBuilder inventoryText = new StringBuilder("Inventory:");
        for (Map.Entry<Food, Integer> entry : gameManager.getFoodInventory().entrySet()) {
            inventoryText.append("\n").append(entry.getKey().getType()).append(": ").append(formatter.format(entry.getValue()));
        }
        inventoryLabel.setText(inventoryText.toString());
    }

    @Override
    public void render(float delta) {
        updateCurrencyLabels();
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
