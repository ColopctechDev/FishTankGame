package com.fishtankgame.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.fishtankgame.FishTankGame;
import com.fishtankgame.game.GameManager;
import com.fishtankgame.game.Shop;
import com.fishtankgame.model.Decor;
import com.fishtankgame.model.Egg;
import com.fishtankgame.model.Food;

public class ShopScreen extends ScreenAdapter {
    private FishTankGame game;
    private Stage stage;
    private Viewport viewport;
    private Skin skin;
    private Shop shop;
    private GameManager gameManager;
    private Label moneyLabel;
    private Label activeTabLabel;

    private Table eggContent;
    private Table foodContent;
    private Table decorContent;

    private TextButton eggTabButton;
    private TextButton foodTabButton;
    private TextButton decorTabButton;

    private boolean isDecorBuyMode = true;

    private void updateMoneyLabel() {
        double money = gameManager.getMoney();
        String moneyText = (money == (long) money) ? String.format("%d", (long) money) : String.format("%.2f", money);
        moneyLabel.setText("Funds: $" + moneyText);
    }

    private void refreshDecorTab() {
        if (decorContent == null) return;
        decorContent.clear();

        Table mainDecorTable = new Table();
        mainDecorTable.top();

        // Sub-tabs for Decor: BUY and SELL
        Table subTabs = new Table();
        TextButton buyModeBtn = new TextButton("BUY", skin);
        TextButton sellModeBtn = new TextButton("SELL", skin);

        float subTabScale = 2.0f;
        buyModeBtn.getLabel().setFontScale(subTabScale);
        sellModeBtn.getLabel().setFontScale(subTabScale);

        // Highlight active sub-tab in Gold
        if (isDecorBuyMode) buyModeBtn.getLabel().setColor(new Color(1f, 0.843f, 0f, 1f));
        else sellModeBtn.getLabel().setColor(new Color(1f, 0.843f, 0f, 1f));

        buyModeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                isDecorBuyMode = true;
                refreshDecorTab();
            }
        });
        sellModeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                isDecorBuyMode = false;
                refreshDecorTab();
            }
        });

        subTabs.add(buyModeBtn).width(300).height(80).pad(10);
        subTabs.add(sellModeBtn).width(300).height(80).pad(10);
        mainDecorTable.add(subTabs).pad(20).row();

        if (isDecorBuyMode) {
            // Buy Section
            Table buyGrid = new Table();
            addDecorButton(buyGrid, "Green Fern", 10);
            addDecorButton(buyGrid, "Red Kelp", 20);
            addDecorButton(buyGrid, "Purple Coral", 30);
            addDecorButton(buyGrid, "Amazon Sword", 40);
            buyGrid.row();
            addDecorButton(buyGrid, "Treasure Chest", 100);
            addDecorButton(buyGrid, "Bubbler", 150);
            mainDecorTable.add(buyGrid).expandX().fillX();
        } else {
            // Sell Section: 15 spots grid (3 rows of 5)
            Table sellGrid = new Table();
            sellGrid.top();

            for (int i = 0; i < 15; i++) {
                final int slotIndex = i;
                Decor foundDecor = null;
                for (Decor d : gameManager.getDecorItems()) {
                    if (d.getSlotIndex() == slotIndex) {
                        foundDecor = d;
                        break;
                    }
                }

                // Create a unique style for this button
                TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));

                if (foundDecor != null) {
                    final Decor decorToSell = foundDecor;
                    int sellPrice = (int) Math.floor(foundDecor.getPurchasePrice() / 2.0);

                    style.up = skin.newDrawable("white", Color.BLACK);
                    style.fontColor = Color.WHITE;
                    TextButton sellBtn = new TextButton(foundDecor.getType() + "\n$" + sellPrice, style);
                    sellBtn.getLabel().setFontScale(2.0f);

                    sellBtn.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            shop.sellDecor(decorToSell);
                            updateMoneyLabel();
                            refreshDecorTab();
                        }
                    });
                    sellGrid.add(sellBtn).width(320).height(160).pad(10);
                } else {
                    style.up = skin.newDrawable("white", Color.GRAY);
                    style.fontColor = Color.WHITE;
                    TextButton sellBtn = new TextButton("Spot " + (i + 1) + "\n[EMPTY]", style);
                    sellBtn.getLabel().setFontScale(2.0f);
                    sellBtn.setDisabled(true);
                    sellGrid.add(sellBtn).width(320).height(160).pad(10);
                }

                if ((i + 1) % 5 == 0) sellGrid.row();
            }

            // Add Fixed items (Chest/Bubbler) at the bottom
            sellGrid.row().padTop(30);
            for (final Decor decor : gameManager.getDecorItems()) {
                if (decor.getSlotIndex() == -1) {
                    int sellPrice = (int) Math.floor(decor.getPurchasePrice() / 2.0);
                    TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));
                    style.up = skin.newDrawable("white", Color.BLACK);
                    style.fontColor = Color.WHITE;

                    TextButton fixedSellBtn = new TextButton(decor.getType() + "\n$" + sellPrice, style);
                    fixedSellBtn.getLabel().setFontScale(2.0f);
                    fixedSellBtn.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            shop.sellDecor(decor);
                            updateMoneyLabel();
                            refreshDecorTab();
                        }
                    });
                    sellGrid.add(fixedSellBtn).width(320).height(160).pad(10);
                }
            }

            ScrollPane scroll = new ScrollPane(sellGrid, skin);
            mainDecorTable.add(scroll).expand().fill().maxHeight(700);
        }

        decorContent.add(mainDecorTable).expand().fill();
    }

    public ShopScreen(FishTankGame game, Shop shop, GameManager gameManager, Skin skin) {
        this.game = game;
        this.shop = shop;
        this.gameManager = gameManager;
        this.skin = skin;

        viewport = new ExtendViewport(FishTankGame.VIRTUAL_WIDTH, FishTankGame.VIRTUAL_HEIGHT);
        stage = new Stage(viewport);

        Table root = new Table();
        root.setFillParent(true);
        root.setBackground(skin.newDrawable("white", new Color(0.1f, 0.1f, 0.2f, 1f)));
        stage.addActor(root);

        // Header
        Table header = new Table();
        header.pad(20);
        Label title = new Label("AQUARIUM SHOP", skin, "subtitle");
        title.setFontScale(3.5f);
        moneyLabel = new Label("", skin);
        moneyLabel.setFontScale(3.0f);
        header.add(title).expandX().left();
        header.add(moneyLabel).right();
        root.add(header).fillX().row();

        // Active Tab Display - Gold Color - Made bigger
        activeTabLabel = new Label("EGGS", skin);
        activeTabLabel.setFontScale(3.2f);
        activeTabLabel.setColor(new Color(1f, 0.843f, 0f, 1f)); // Gold
        root.add(activeTabLabel).pad(10).row();

        // Tabs
        Table tabs = new Table();
        eggTabButton = new TextButton("EGGS", skin);
        foodTabButton = new TextButton("FOOD", skin);
        decorTabButton = new TextButton("DECOR", skin);

        float tabFontScale = 2.2f;
        eggTabButton.getLabel().setFontScale(tabFontScale);
        foodTabButton.getLabel().setFontScale(tabFontScale);
        decorTabButton.getLabel().setFontScale(tabFontScale);

        eggTabButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showTab(eggContent, "EGGS");
            }
        });
        foodTabButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showTab(foodContent, "FOOD");
            }
        });
        decorTabButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showTab(decorContent, "DECOR");
                refreshDecorTab();
            }
        });

        tabs.add(eggTabButton).expandX().fillX().height(80);
        tabs.add(foodTabButton).expandX().fillX().height(80);
        tabs.add(decorTabButton).expandX().fillX().height(80);
        root.add(tabs).fillX().row();

        // Main content area
        Stack contentStack = new Stack();

        // --- Egg Content ---
        eggContent = new Table();
        eggContent.top();
        Table eggGrid = new Table();
        eggGrid.pad(10);
        addEggButton(eggGrid, "Goldfish", 5);
        addEggButton(eggGrid, "Blue Tang", 15);
        addEggButton(eggGrid, "Angelfish", 20);
        addEggButton(eggGrid, "Betafish", 25);
        eggGrid.row();
        addEggButton(eggGrid, "Clownfish", 30);
        addEggButton(eggGrid, "Tigerfish", 50);
        eggContent.add(eggGrid).expand().fill();

        // --- Food Content ---
        foodContent = new Table();
        foodContent.top();
        Table foodGrid = new Table();
        foodGrid.pad(10);
        addFoodButton(foodGrid, "Sunflower", 10, 1.0);
        addFoodButton(foodGrid, "Poppy", 20, 3.0);
        addFoodButton(foodGrid, "Flax", 30, 5.0);
        addFoodButton(foodGrid, "Sesame", 40, 7.0);
        foodGrid.row();
        addFoodButton(foodGrid, "Chia", 50, 10.0);
        foodContent.add(foodGrid).expand().fill();

        // --- Decor Content ---
        decorContent = new Table();
        decorContent.top();
        refreshDecorTab();

        contentStack.add(eggContent);
        contentStack.add(foodContent);
        contentStack.add(decorContent);

        foodContent.setVisible(false);
        decorContent.setVisible(false);

        root.add(contentStack).expand().fill().row();

        // Footer
        TextButton backButton = new TextButton("BACK TO TANK", skin);
        backButton.getLabel().setFontScale(2.5f);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.showGameScreen();
            }
        });
        root.add(backButton).pad(20, 20, 100, 20).width(500).height(100);
    }

    private void showInsufficientFundsDialog() {
        final Dialog dialog = new Dialog("Transaction Denied", skin);
        dialog.getTitleLabel().setFontScale(2.5f);

        Label message = new Label("You don't have enough money for this item.\nSell adult fish to make more money.", skin);
        message.setFontScale(2.0f);
        message.setAlignment(com.badlogic.gdx.utils.Align.center);

        dialog.getContentTable().add(message).pad(40);

        TextButton okBtn = new TextButton("OK", skin);
        okBtn.getLabel().setFontScale(2.5f);
        okBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
            }
        });

        dialog.getButtonTable().add(okBtn).width(200).height(80).pad(20);
        dialog.show(stage);
    }

    private void addDecorButton(Table table, final String type, final float price) {
        String label = type + "\n$" + (int)price;

        TextButton button = new TextButton(label, skin);
        button.getLabel().setFontScale(2.2f);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (gameManager.getMoney() < price) {
                    showInsufficientFundsDialog();
                    return;
                }

                // Updated to check for the new names
                if (type.equals("Green Fern") || type.equals("Red Kelp") ||
                    type.equals("Purple Coral") || type.equals("Amazon Sword")) {
                    showSlotPicker(type, price);
                } else {
                    shop.buyDecor(type, price, -1);
                    updateMoneyLabel();
                    refreshDecorTab();
                }
            }
        });
        table.add(button).width(340).height(160).pad(10);
    }

    private void showSlotPicker(final String plantType, final float price) {
        final Dialog dialog = new Dialog("Pick a Spot", skin);
        dialog.getTitleLabel().setFontScale(2.0f);

        Table grid = new Table();
        for (int i = 0; i < 15; i++) {
            final int slot = i;
            String label = "Spot " + (i + 1);
            boolean isOccupied = gameManager.isSlotOccupied(i);
            if (isOccupied) {
                label = "FULL";
            }

            TextButton slotBtn = new TextButton(label, skin);
            slotBtn.getLabel().setFontScale(4.0f);

            if (isOccupied) {
                slotBtn.setDisabled(true);
                slotBtn.getLabel().setColor(Color.RED);
            }

            slotBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (!slotBtn.isDisabled()) {
                        // Check again in case money changed while dialog was open
                        if (gameManager.getMoney() >= price) {
                            shop.buyDecor(plantType, price, slot);
                            updateMoneyLabel();
                            dialog.hide();
                            refreshDecorTab();
                        } else {
                            dialog.hide();
                            showInsufficientFundsDialog();
                        }
                    }
                }
            });
            grid.add(slotBtn).width(300).height(180).pad(10);
            if ((i + 1) % 5 == 0) grid.row();
        }

        dialog.getContentTable().add(grid);
        TextButton cancelBtn = new TextButton("CANCEL", skin);
        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
            }
        });
        dialog.getButtonTable().add(cancelBtn).width(300).height(80).pad(20);
        dialog.show(stage);
    }

    private void showTab(Table visibleTab, String name) {
        eggContent.setVisible(visibleTab == eggContent);
        foodContent.setVisible(visibleTab == foodContent);
        decorContent.setVisible(visibleTab == decorContent);
        activeTabLabel.setText(name);
    }

    private String formatPrice(double price) {
        if (price == (long) price) {
            return String.format("%d", (long) price);
        } else {
            return String.format("%s", price);
        }
    }

    private void addEggButton(Table table, final String breed, final double price) {
        TextButton button = new TextButton(breed + "\n$" + formatPrice(price), skin);
        button.getLabel().setFontScale(2.5f);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (gameManager.getMoney() < price) {
                    showInsufficientFundsDialog();
                    return;
                }
                shop.buyEgg(new Egg(breed, 5, price));
                updateMoneyLabel();
            }
        });
        table.add(button).width(340).height(160).pad(10);
    }

    private void addFoodButton(Table table, final String type, final int growth, final double price) {
        TextButton button = new TextButton(type + "\n$" + formatPrice(price), skin);
        button.getLabel().setFontScale(2.5f);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (gameManager.getMoney() < price) {
                    showInsufficientFundsDialog();
                    return;
                }
                shop.buyFood(new Food(type, growth, price));
                updateMoneyLabel();
            }
        });
        table.add(button).width(340).height(160).pad(10);
    }

    @Override
    public void show() {
        updateMoneyLabel();
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
