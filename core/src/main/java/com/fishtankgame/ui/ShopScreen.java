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
    private Label pearlLabel;
    private Label activeTabLabel;

    private Table eggContent;
    private Table foodContent;
    private Table decorContent;
    private Table pearlContent;

    private TextButton eggTabButton;
    private TextButton foodTabButton;
    private TextButton decorTabButton;
    private TextButton pearlTabButton;

    private boolean isDecorBuyMode = true;

    private void updateCurrencyLabels() {
        double money = gameManager.getMoney();
        String moneyText = (money == (long) money) ? String.format("%d", (long) money) : String.format("%.2f", money);
        moneyLabel.setText("Funds: $" + moneyText);
        pearlLabel.setText("Pearls: " + gameManager.getPearls());
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
        mainDecorTable.add(subTabs).pad(20);
        mainDecorTable.row();

        if (isDecorBuyMode) {
            // Buy Section
            Table buyGrid = new Table();
            addDecorButton(buyGrid, "Green Fern", 10);
            addDecorButton(buyGrid, "Red Kelp", 20);
            addDecorButton(buyGrid, "Purple Coral", 30);
            addDecorButton(buyGrid, "Amazon Sword", 40);
            buyGrid.row();
            addDecorButton(buyGrid, "YardRock", 10);
            addDecorButton(buyGrid, "Treasure Chest", 100);
            addDecorButton(buyGrid, "Bubbler", 50, true);
            addDecorButton(buyGrid, "EggPus", 500, true);
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

                    String displayName = foundDecor.getType().equals("EggPus") ? "Octopus" : foundDecor.getType();
                    TextButton sellBtn = new TextButton(displayName + "\n$" + sellPrice, style);
                    sellBtn.getLabel().setFontScale(2.0f);

                    sellBtn.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            shop.sellDecor(decorToSell);
                            updateCurrencyLabels();
                            refreshDecorTab();
                        }
                    });
                    sellGrid.add(sellBtn).width(320).height(160).pad(10);
                } else {
                    style.up = skin.newDrawable("white", Color.GRAY);
                    style.fontColor = Color.BLACK;
                    TextButton sellBtn = new TextButton("Spot " + (i + 1) + "\n[EMPTY]", style);
                    sellBtn.getLabel().setFontScale(2.0f);
                    sellBtn.setDisabled(true);
                    sellGrid.add(sellBtn).width(320).height(160).pad(10);
                }

                if ((i + 1) % 5 == 0) sellGrid.row();
            }

            // Add Fixed items (Chest/Bubbler/EggPus) at the bottom
            sellGrid.row().padTop(30);
            for (final Decor decor : gameManager.getDecorItems()) {
                if (decor.getSlotIndex() == -1) {
                    int sellPrice = (int) Math.floor(decor.getPurchasePrice() / 2.0);
                    TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));
                    style.up = skin.newDrawable("white", Color.BLACK);
                    style.fontColor = Color.WHITE;

                    String displayName = decor.getType().equals("EggPus") ? "Octopus" : decor.getType();
                    TextButton fixedSellBtn = new TextButton(displayName + "\n$" + sellPrice, style);
                    fixedSellBtn.getLabel().setFontScale(2.0f);
                    fixedSellBtn.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            shop.sellDecor(decor);
                            updateCurrencyLabels();
                            refreshDecorTab();
                        }
                    });
                    sellGrid.add(fixedSellBtn).width(320).height(160).pad(10);
                }
            }

            ScrollPane scroll = new ScrollPane(sellGrid, skin);
            mainDecorTable.add(scroll).expand().fill().maxHeight(700).row();

            TextButton cancelBtn = new TextButton("CANCEL", skin);
            cancelBtn.getLabel().setFontScale(2.5f);
            cancelBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    isDecorBuyMode = true;
                    refreshDecorTab();
                }
            });
            mainDecorTable.add(cancelBtn).width(300).height(80).pad(20);
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
        pearlLabel = new Label("", skin);
        pearlLabel.setFontScale(3.0f);
        pearlLabel.setColor(new Color(0.7f, 0.9f, 1f, 1f));

        Table infoTable = new Table();
        infoTable.add(moneyLabel).right().row();
        infoTable.add(pearlLabel).right();

        header.add(title).expandX().left();
        header.add(infoTable).right();
        root.add(header).fillX().row();

        // Active Tab Display
        activeTabLabel = new Label("EGGS", skin);
        activeTabLabel.setFontScale(3.2f);
        activeTabLabel.setColor(new Color(1f, 0.843f, 0f, 1f));
        root.add(activeTabLabel).pad(10).row();

        // Tabs
        Table tabs = new Table();
        eggTabButton = new TextButton("EGGS", skin);
        foodTabButton = new TextButton("FOOD", skin);
        decorTabButton = new TextButton("DECOR", skin);
        pearlTabButton = new TextButton("PEARLS", skin);

        float tabFontScale = 2.2f;
        eggTabButton.getLabel().setFontScale(tabFontScale);
        foodTabButton.getLabel().setFontScale(tabFontScale);
        decorTabButton.getLabel().setFontScale(tabFontScale);
        pearlTabButton.getLabel().setFontScale(tabFontScale);

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
        pearlTabButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showTab(pearlContent, "PEARLS");
            }
        });

        tabs.add(eggTabButton).expandX().fillX().height(80);
        tabs.add(foodTabButton).expandX().fillX().height(80);
        tabs.add(decorTabButton).expandX().fillX().height(80);
        tabs.add(pearlTabButton).expandX().fillX().height(80);
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
        addEggButton(eggGrid, "Koi", 100, true);
        addEggButton(eggGrid, "Rainbowfish", 250, true);
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
        addFoodButton(foodGrid, "Hemp", 75, 15.0);
        addFoodButton(foodGrid, "Pumpkin", 10, 50.0, true);
        addFoodButton(foodGrid, "Quinoa", 25, 100.0, true);
        foodContent.add(foodGrid).expand().fill();

        // --- Decor Content ---
        decorContent = new Table();
        decorContent.top();
        refreshDecorTab();

        // --- Pearl Content ---
        pearlContent = new Table();
        pearlContent.top();
        Table pearlGrid = new Table();
        pearlGrid.pad(10);
        addPearlPurchaseButton(pearlGrid, 100, "$0.99");
        addPearlPurchaseButton(pearlGrid, 550, "$4.99");
        addPearlPurchaseButton(pearlGrid, 1200, "$9.99");
        addPearlPurchaseButton(pearlGrid, 2500, "$19.99");
        pearlContent.add(pearlGrid).expand().fill();

        contentStack.add(eggContent);
        contentStack.add(foodContent);
        contentStack.add(decorContent);
        contentStack.add(pearlContent);

        foodContent.setVisible(false);
        decorContent.setVisible(false);
        pearlContent.setVisible(false);

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

    private void addPearlPurchaseButton(Table table, final int amount, String priceLabel) {
        TextButton button = new TextButton(amount + " Pearls\n" + priceLabel, skin);
        button.getLabel().setFontScale(2.5f);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameManager.purchasePearls(amount);
            }
        });
        table.add(button).width(340).height(160).pad(10);
    }

    private void showInsufficientFundsDialog(boolean isPremium) {
        final Dialog dialog = new Dialog("Transaction Denied", skin);
        dialog.getTitleLabel().setFontScale(2.5f);

        String currencyName = isPremium ? "pearls" : "money";
        String advice = isPremium ? "Visit the PEARLS tab to get more." : "Sell adult fish to make more money.";
        Label message = new Label("You don't have enough " + currencyName + " for this item.\n" + advice, skin);
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
        addDecorButton(table, type, price, false);
    }

    private void addDecorButton(Table table, final String type, final float price, final boolean isPremium) {
        String displayName = type.equals("EggPus") ? "Octopus" : type;
        String label = displayName + "\n" + (int)price + (isPremium ? " Pearls" : "");
        if (!isPremium) label = displayName + "\n$" + (int)price;

        TextButton button = new TextButton(label, skin);
        button.getLabel().setFontScale(2.2f);
        if (isPremium) button.getLabel().setColor(new Color(0.7f, 0.9f, 1f, 1f));

        // Check if the item is already owned (for Bubbler, Chest, EggPus)
        boolean isOwned = false;
        if (type.equals("Bubbler") || type.equals("Treasure Chest") || type.equals("EggPus")) {
            for (Decor d : gameManager.getDecorItems()) {
                if (d.getType().equals(type)) {
                    isOwned = true;
                    break;
                }
            }
        }

        if (isOwned) {
            button.setDisabled(true);
            button.getLabel().setColor(Color.GRAY);
            button.setText(displayName + "\n[OWNED]");
        }

        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (button.isDisabled()) return;

                if (isPremium) {
                    if (gameManager.getPearls() < price) {
                        showInsufficientFundsDialog(true);
                        return;
                    }
                } else {
                    if (gameManager.getMoney() < price) {
                        showInsufficientFundsDialog(false);
                        return;
                    }
                }

                if (type.equals("Green Fern") || type.equals("Red Kelp") ||
                    type.equals("Purple Coral") || type.equals("Amazon Sword") || type.equals("YardRock")) {
                    showSlotPicker(type, price);
                } else {
                    shop.buyDecor(type, price, -1, isPremium);
                    updateCurrencyLabels();
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
                        if (gameManager.getMoney() >= price) {
                            shop.buyDecor(plantType, price, slot);
                            updateCurrencyLabels();
                            dialog.hide();
                            refreshDecorTab();
                        } else {
                            dialog.hide();
                            showInsufficientFundsDialog(false);
                        }
                    }
                }
            });
            grid.add(slotBtn).width(300).height(180).pad(10);
            if ((i + 1) % 5 == 0) grid.row();
        }

        dialog.getContentTable().add(grid);
        TextButton cancelBtn = new TextButton("CANCEL", skin);
        cancelBtn.getLabel().setFontScale(2.5f);
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
        pearlContent.setVisible(visibleTab == pearlContent);
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
        addEggButton(table, breed, price, false);
    }

    private void addEggButton(Table table, final String breed, final double price, final boolean isPremium) {
        String label = breed + "\n" + formatPrice(price) + (isPremium ? " Pearls" : "");
        if (!isPremium) label = breed + "\n$" + formatPrice(price);

        TextButton button = new TextButton(label, skin);
        button.getLabel().setFontScale(2.5f);
        if (isPremium) button.getLabel().setColor(new Color(0.7f, 0.9f, 1f, 1f));

        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isPremium) {
                    if (gameManager.getPearls() < price) {
                        showInsufficientFundsDialog(true);
                        return;
                    }
                } else {
                    if (gameManager.getMoney() < price) {
                        showInsufficientFundsDialog(false);
                        return;
                    }
                }
                shop.buyEgg(new Egg(breed, 5, price, isPremium));
                updateCurrencyLabels();
            }
        });
        table.add(button).width(340).height(160).pad(10);
    }

    private void addFoodButton(Table table, final String type, final double price, final double growth) {
        addFoodButton(table, type, price, growth, false);
    }

    private void addFoodButton(Table table, final String type, final double price, final double growth, final boolean isPremium) {
        String label = type + "\n" + formatPrice(price) + (isPremium ? " Pearls" : "");
        if (!isPremium) label = type + "\n$" + formatPrice(price);

        TextButton button = new TextButton(label, skin);
        button.getLabel().setFontScale(2.5f);
        if (isPremium) button.getLabel().setColor(new Color(0.7f, 0.9f, 1f, 1f));

        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isPremium) {
                    if (gameManager.getPearls() < price) {
                        showInsufficientFundsDialog(true);
                        return;
                    }
                } else {
                    if (gameManager.getMoney() < price) {
                        showInsufficientFundsDialog(false);
                        return;
                    }
                }
                shop.buyFood(new Food(type, (int) growth, price, isPremium));
                updateCurrencyLabels();
            }
        });
        table.add(button).width(340).height(160).pad(10);
    }

    @Override
    public void show() {
        updateCurrencyLabels();
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        updateCurrencyLabels(); // Refresh pearls if purchase completed
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
