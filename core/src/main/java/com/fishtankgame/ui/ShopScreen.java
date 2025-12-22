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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.fishtankgame.FishTankGame;
import com.fishtankgame.game.GameManager;
import com.fishtankgame.game.Shop;
import com.fishtankgame.model.Decor;
import com.fishtankgame.model.Egg;
import com.fishtankgame.model.Fish;
import com.fishtankgame.model.FishBreed;
import com.fishtankgame.model.Food;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ShopScreen extends ScreenAdapter {
    private final FishTankGame game;
    private final Stage stage;
    private final Viewport viewport;
    private final Skin skin;
    private final Shop shop;
    private final GameManager gameManager;
    private final Label moneyLabel;
    private final Label pearlLabel;
    private final Label activeTabLabel;

    private final Table eggContent;
    private final Table foodContent;
    private final Table decorContent;
    private final Table pearlContent;

    private final TextButton eggTabButton;
    private final TextButton foodTabButton;
    private final TextButton decorTabButton;
    private final TextButton pearlTabButton;

    private String decorMode = "BUY"; // BUY, SELL, MOVE
    private boolean isFishBuyMode = true;
    private final NumberFormat formatter = NumberFormat.getInstance(Locale.US);
    private int selectedSlot = -1; // For moving decor

    private void updateCurrencyLabels() {
        double money = gameManager.getMoney();
        moneyLabel.setText("Funds: $" + formatter.format(money));
        pearlLabel.setText("Pearls: " + formatter.format(gameManager.getPearls()));
    }

    private void refreshFishTab() {
        if (eggContent == null) return;
        eggContent.clear();

        Table mainFishTable = new Table();
        mainFishTable.top();

        // Sub-tabs for Fish: BUY and SELL
        Table subTabs = new Table();

        TextButton.TextButtonStyle activeStyle = new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));
        activeStyle.up = skin.newDrawable("white", Color.BLACK);
        activeStyle.fontColor = Color.WHITE;

        TextButton.TextButtonStyle inactiveStyle = new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));
        inactiveStyle.up = skin.newDrawable("white", Color.GRAY);
        inactiveStyle.fontColor = Color.BLACK;

        TextButton buyModeBtn = new TextButton("BUY", isFishBuyMode ? activeStyle : inactiveStyle);
        TextButton sellModeBtn = new TextButton("SELL", isFishBuyMode ? inactiveStyle : activeStyle);

        float subTabScale = 2.0f;
        buyModeBtn.getLabel().setFontScale(subTabScale);
        sellModeBtn.getLabel().setFontScale(subTabScale);

        buyModeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                isFishBuyMode = true;
                refreshFishTab();
            }
        });
        sellModeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                isFishBuyMode = false;
                refreshFishTab();
            }
        });

        subTabs.add(buyModeBtn).width(300).height(80).pad(10);
        subTabs.add(sellModeBtn).width(300).height(80).pad(10);
        mainFishTable.add(subTabs).pad(20);
        mainFishTable.row();

        // Sort breeds by price
        List<FishBreed> allBreeds = new ArrayList<>(Arrays.asList(FishBreed.values()));
        allBreeds.sort(Comparator.comparingDouble(FishBreed::getBuyPrice));

        List<FishBreed> cashBreeds = new ArrayList<>();
        List<FishBreed> pearlBreeds = new ArrayList<>();
        for (FishBreed breed : allBreeds) {
            if (breed.isPremium()) pearlBreeds.add(breed);
            else cashBreeds.add(breed);
        }

        if (isFishBuyMode) {
            Table eggGrid = new Table();
            eggGrid.pad(10);
            eggGrid.top(); // Ensure alignment starts from top

            // Add cash breeds
            for (int i = 0; i < cashBreeds.size(); i++) {
                addEggButton(eggGrid, cashBreeds.get(i));
                if ((i + 1) % 5 == 0) eggGrid.row();
            }

            // Add pearl breeds in a new section
            eggGrid.row();
            TextButton.TextButtonStyle yellowStyle = new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));
            yellowStyle.up = skin.newDrawable("white", new Color(1f, 0.843f, 0f, 1f));
            yellowStyle.fontColor = Color.BLACK;
            TextButton pearlsBox = new TextButton("Pearls!\n--->", yellowStyle);
            pearlsBox.getLabel().setFontScale(2.5f);
            eggGrid.add(pearlsBox).width(340).height(160).pad(10);

            for (int i = 0; i < pearlBreeds.size(); i++) {
                addEggButton(eggGrid, pearlBreeds.get(i));
                if ((i + 1) % 4 == 0 && i < pearlBreeds.size() - 1) { // 4 pearl fish per row
                    eggGrid.row();
                    eggGrid.add(); //- empty cell for alignment
                }
            }

            ScrollPane scroll = new ScrollPane(eggGrid, skin);
            mainFishTable.add(scroll).expand().fill().maxHeight(700);
        } else {
            Table sellGrid = new Table();
            sellGrid.top();

            // Use the sorted lists for selling as well
            for (int i = 0; i < cashBreeds.size(); i++) {
                addSellButton(sellGrid, cashBreeds.get(i));
                if ((i + 1) % 5 == 0) sellGrid.row();
            }

            sellGrid.row();
            TextButton.TextButtonStyle yellowStyle = new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));
            yellowStyle.up = skin.newDrawable("white", new Color(1f, 0.843f, 0f, 1f));
            yellowStyle.fontColor = Color.BLACK;
            TextButton cashOnlyBox = new TextButton("Sells for\nCASH ONLY", yellowStyle);
            cashOnlyBox.getLabel().setFontScale(2.5f);
            sellGrid.add(cashOnlyBox).width(340).height(160).pad(10);

            for (int i = 0; i < pearlBreeds.size(); i++) {
                addSellButton(sellGrid, pearlBreeds.get(i));
                if ((i + 1) % 4 == 0 && i < pearlBreeds.size() - 1) {
                    sellGrid.row();
                    sellGrid.add(); //- empty cell for alignment
                }
            }

            ScrollPane scroll = new ScrollPane(sellGrid, skin);
            mainFishTable.add(scroll).expand().fill().maxHeight(700);
        }

        eggContent.add(mainFishTable).expand().fill();
    }

    private void addSellButton(Table table, final FishBreed breed) {
        int adultCount = 0;
        for (Fish f : gameManager.getFishList()) {
            if (f.getBreed().equals(breed.getName()) && f.isAdult()) {
                adultCount++;
            }
        }

        final String breedName = breed.getName();
        final double sellPrice = breed.getSellPrice();
        final int finalAdultCount = adultCount;

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));
        if (adultCount > 0) {
            style.up = skin.newDrawable("white", Color.BLACK);
            style.fontColor = Color.WHITE;
        } else {
            style.up = skin.newDrawable("white", Color.GRAY);
            style.fontColor = Color.BLACK;
        }

        String label = breedName + "\n$" + formatter.format(sellPrice) + " X " + adultCount + " Total";

        TextButton sellBtn = new TextButton(label, style);
        sellBtn.getLabel().setFontScale(2.0f);
        if (adultCount <= 0) {
            sellBtn.setDisabled(true);
        }

        sellBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (finalAdultCount > 0) {
                    for (Fish f : new ArrayList<>(gameManager.getFishList())) {
                        if (f.getBreed().equals(breedName) && f.isAdult()) {
                            Fish soldFish = shop.sellFish(f);
                            if (soldFish != null) {
                                gameManager.handleSoldFish(soldFish);
                                updateCurrencyLabels();
                                refreshFishTab();
                            }
                            break;
                        }
                    }
                }
            }
        });
        table.add(sellBtn).width(340).height(180).pad(10);
    }

    private void refreshDecorTab() {
        if (decorContent == null) return;
        decorContent.clear();

        Table mainDecorTable = new Table();
        mainDecorTable.top();

        // Sub-tabs for Decor: BUY, SELL, and MOVE
        Table subTabs = new Table();

        TextButton.TextButtonStyle activeStyle = new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));
        activeStyle.up = skin.newDrawable("white", Color.BLACK);
        activeStyle.fontColor = Color.WHITE;

        TextButton.TextButtonStyle inactiveStyle = new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));
        inactiveStyle.up = skin.newDrawable("white", Color.GRAY);
        inactiveStyle.fontColor = Color.BLACK;

        TextButton buyModeBtn = new TextButton("BUY", decorMode.equals("BUY") ? activeStyle : inactiveStyle);
        TextButton sellModeBtn = new TextButton("SELL", decorMode.equals("SELL") ? activeStyle : inactiveStyle);
        TextButton moveModeBtn = new TextButton("MOVE", decorMode.equals("MOVE") ? activeStyle : inactiveStyle);

        float subTabScale = 2.0f;
        buyModeBtn.getLabel().setFontScale(subTabScale);
        sellModeBtn.getLabel().setFontScale(subTabScale);
        moveModeBtn.getLabel().setFontScale(subTabScale);

        buyModeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                decorMode = "BUY";
                selectedSlot = -1;
                refreshDecorTab();
            }
        });
        sellModeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                decorMode = "SELL";
                selectedSlot = -1;
                refreshDecorTab();
            }
        });
        moveModeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                decorMode = "MOVE";
                selectedSlot = -1;
                refreshDecorTab();
            }
        });

        subTabs.add(buyModeBtn).width(300).height(80).pad(10);
        subTabs.add(sellModeBtn).width(300).height(80).pad(10);
        subTabs.add(moveModeBtn).width(300).height(80).pad(10);
        mainDecorTable.add(subTabs).pad(20);
        mainDecorTable.row();

        if (decorMode.equals("BUY")) {
            Table buyGrid = new Table();
            addDecorButton(buyGrid, "Green Fern", 10);
            addDecorButton(buyGrid, "Red Kelp", 20);
            addDecorButton(buyGrid, "Purple Coral", 30);
            addDecorButton(buyGrid, "Amazon Sword", 40);
            buyGrid.row();
            addDecorButton(buyGrid, "YardRock", 10);
            addDecorButton(buyGrid, "Treasure Chest", 100, false, true);
            addDecorButton(buyGrid, "Bubbler", 50, true, true);
            addDecorButton(buyGrid, "EggPus", 500, true, true);
            mainDecorTable.add(buyGrid).expandX().fillX();

        } else if (decorMode.equals("SELL")) {
            // ... (rest of sell logic is unchanged for now)
            Table sellGrid = new Table();
            sellGrid.top();

            String[] fixedTypes = {"Treasure Chest", "Bubbler", "EggPus"};
            Color goldColor = new Color(1f, 0.843f, 0f, 1f);

            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 5; col++) {
                    final int slotIndex = row * 5 + col;
                    Decor foundDecor = gameManager.getDecorInSlot(slotIndex);

                    TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));
                    if (foundDecor != null) {
                        final Decor decorToSell = foundDecor;
                        double sellPrice = Math.floor(foundDecor.getPurchasePrice() / 2.0);
                        style.up = skin.newDrawable("white", Color.BLACK);
                        style.fontColor = Color.WHITE;
                        String displayName = foundDecor.getType().equals("EggPus") ? "Octopus" : foundDecor.getType();
                        TextButton sellBtn = new TextButton(displayName + "\n$" + formatter.format(sellPrice), style);
                        sellBtn.getLabel().setFontScale(2.0f);
                        sellBtn.addListener(new ClickListener() {
                            @Override
                            public void clicked(InputEvent event, float x, float y) {
                                shop.sellDecor(decorToSell);
                                updateCurrencyLabels();
                                refreshDecorTab();
                            }
                        });
                        sellGrid.add(sellBtn).width(270).height(160).pad(5);
                    } else {
                        style.up = skin.newDrawable("white", Color.GRAY);
                        style.fontColor = Color.BLACK;
                        TextButton sellBtn = new TextButton("Spot " + (slotIndex + 1) + "\n[EMPTY]", style);
                        sellBtn.getLabel().setFontScale(2.0f);
                        sellBtn.setDisabled(true);
                        sellGrid.add(sellBtn).width(270).height(160).pad(5);
                    }
                }

                final String fixedType = fixedTypes[row];
                Decor foundFixed = gameManager.getFixedDecor(fixedType);
                TextButton.TextButtonStyle fStyle = new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));
                String displayName = fixedType.equals("EggPus") ? "Octopus" : fixedType;

                if (foundFixed != null) {
                    final Decor decorToSell = foundFixed;
                    double sellPrice;
                    if (fixedType.equals("Bubbler") || fixedType.equals("EggPus")) {
                        sellPrice = Math.floor(foundFixed.getPurchasePrice() * 200.0 / 2.0);
                    } else {
                        sellPrice = Math.floor(foundFixed.getPurchasePrice() / 2.0);
                    }

                    fStyle.up = skin.newDrawable("white", goldColor);
                    fStyle.fontColor = Color.BLACK;
                    TextButton fixedBtn = new TextButton(displayName + "\nSELL $" + formatter.format(sellPrice), fStyle);
                    fixedBtn.getLabel().setFontScale(2.0f);
                    fixedBtn.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            shop.sellDecor(decorToSell);
                            updateCurrencyLabels();
                            refreshDecorTab();
                        }
                    });
                    sellGrid.add(fixedBtn).width(270).height(160).pad(5);
                } else {
                    fStyle.up = skin.newDrawable("white", Color.GRAY);
                    fStyle.fontColor = Color.DARK_GRAY;
                    TextButton fixedBtn = new TextButton(displayName + "\n[NOT OWNED]", fStyle);
                    fixedBtn.getLabel().setFontScale(2.0f);
                    fixedBtn.setDisabled(true);
                    sellGrid.add(fixedBtn).width(270).height(160).pad(5);
                }
                sellGrid.row();
            }
            ScrollPane scroll = new ScrollPane(sellGrid, skin);
            mainDecorTable.add(scroll).expand().fill().maxHeight(700).row();

        } else if (decorMode.equals("MOVE")) {
            Table moveGrid = new Table();
            moveGrid.top().pad(20);

            // --- Plant slots (0-14) ---
            for (int i = 0; i < 15; i++) {
                addMoveButton(moveGrid, i);
                if ((i + 1) % 5 == 0) moveGrid.row();
            }

            // --- Special slots (15-17) ---
            moveGrid.row().padTop(40);
            addMoveButton(moveGrid, 15); // Left
            addMoveButton(moveGrid, 16); // Center
            addMoveButton(moveGrid, 17); // Right

            ScrollPane scroll = new ScrollPane(moveGrid, skin);
            mainDecorTable.add(scroll).expand().fill().maxHeight(700).row();
        }

        decorContent.add(mainDecorTable).expand().fill();
    }

    private void addMoveButton(Table table, final int slotIndex) {
        Decor foundDecor = gameManager.getDecorInSlot(slotIndex);

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));
        String label;

        if (foundDecor != null) {
            label = foundDecor.getType().equals("EggPus") ? "Octopus" : foundDecor.getType();
            style.up = skin.newDrawable("white", Color.BLACK);
            style.fontColor = Color.WHITE;
        } else {
            if (slotIndex >= 15) {
                label = "[SPECIAL SLOT]";
            } else {
                label = "[EMPTY]";
            }
            style.up = skin.newDrawable("white", Color.GRAY);
            style.fontColor = Color.BLACK;
        }

        if (slotIndex == selectedSlot) {
            style.up = skin.newDrawable("white", Color.GOLD);
            style.fontColor = Color.BLACK;
        }

        TextButton moveBtn = new TextButton(label, style);
        moveBtn.getLabel().setFontScale(2.0f);

        moveBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Decor selectedDecor = gameManager.getDecorInSlot(selectedSlot);

                if (foundDecor != null) {
                    // A decor item is in this slot, so select/deselect it
                    if (selectedSlot == slotIndex) {
                        selectedSlot = -1; // Deselect
                    } else {
                        selectedSlot = slotIndex;
                    }
                } else {
                    // This is an empty slot
                    if (selectedDecor != null) {
                        // Check if the move is valid
                        boolean isMovingSpecial = selectedDecor.getType().equals("Bubbler") || selectedDecor.getType().equals("Treasure Chest") || selectedDecor.getType().equals("EggPus");
                        if (isMovingSpecial && slotIndex >= 15) {
                            gameManager.moveDecorToSlot(selectedDecor, slotIndex);
                            selectedSlot = -1; // Reset selection
                        } else if (!isMovingSpecial && slotIndex <= 14) {
                            gameManager.moveDecorToSlot(selectedDecor, slotIndex);
                            selectedSlot = -1; // Reset selection
                        }
                    }
                }
                refreshDecorTab(); // Redraw to show selection/move
            }
        });

        table.add(moveBtn).width(340).height(180).pad(10);
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
        activeTabLabel = new Label("DECOR", skin);
        activeTabLabel.setFontScale(3.2f);
        activeTabLabel.setColor(new Color(1f, 0.843f, 0f, 1f));
        root.add(activeTabLabel).pad(10).row();

        // Tabs
        Table tabs = new Table();
        eggTabButton = new TextButton("FISH & EGGS", skin);
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
                showTab(eggContent, "FISH & EGGS");
                refreshFishTab();
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

        // --- Fish & Egg Content ---
        eggContent = new Table();
        eggContent.top();
        refreshFishTab();

        // --- Food Content ---
        foodContent = new Table();
        foodContent.top();
        Table foodGrid = new Table();
        foodGrid.pad(10);
        addFoodButton(foodGrid, "Sunflower", 10, 3.0);
        addFoodButton(foodGrid, "Poppy", 20, 6.0);
        addFoodButton(foodGrid, "Flax", 30, 8.0);
        addFoodButton(foodGrid, "Sesame", 40, 10.0);
        foodGrid.row();
        addFoodButton(foodGrid, "Chia", 50, 15.0);
        addFoodButton(foodGrid, "Hemp", 75, 25.0);
        addFoodButton(foodGrid, "Pumpkin", 20, 50.0, true);
        addFoodButton(foodGrid, "Quinoa", 40, 90.0, true);
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

        // Column layout: 100, 550, (Thank You), 1200, 2500
        addPearlPurchaseButton(pearlGrid, 100, "$0.99");
        addPearlPurchaseButton(pearlGrid, 550, "$4.99");

        TextButton.TextButtonStyle thankStyle = new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));
        thankStyle.up = skin.newDrawable("white", Color.CLEAR);
        thankStyle.fontColor = new Color(1f, 0.843f, 0f, 1f); // Gold
        TextButton thankBtn = new TextButton("Thank\nYOU", thankStyle);
        thankBtn.getLabel().setFontScale(3.5f);
        thankBtn.setDisabled(true);
        pearlGrid.add(thankBtn).width(340).height(160).pad(10);

        addPearlPurchaseButton(pearlGrid, 1200, "$9.99");
        addPearlPurchaseButton(pearlGrid, 2500, "$19.99");

        pearlGrid.row().padTop(50); // intentional space between upper two rows

        // Column layout: (Popular!!), 4000, (for your support), 7000, 15000
        TextButton.TextButtonStyle popStyle = new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));
        popStyle.up = skin.newDrawable("white", new Color(1f, 0.843f, 0f, 1f));
        popStyle.fontColor = Color.BLACK;
        TextButton popularBtn = new TextButton("Popular!!\n--->", popStyle);
        popularBtn.getLabel().setFontScale(2.5f);
        pearlGrid.add(popularBtn).width(340).height(160).pad(10);

        addPearlPurchaseButton(pearlGrid, 4000, "$29.99");

        TextButton.TextButtonStyle supportStyle = new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));
        supportStyle.up = skin.newDrawable("white", Color.CLEAR);
        supportStyle.fontColor = new Color(1f, 0.843f, 0f, 1f); // Gold
        TextButton supportBtn = new TextButton("for your\nSUPPORT", supportStyle);
        supportBtn.getLabel().setFontScale(2.5f);
        supportBtn.setDisabled(true);
        pearlGrid.add(supportBtn).width(340).height(160).pad(10);

        addPearlPurchaseButton(pearlGrid, 7000, "$49.99");
        addPearlPurchaseButton(pearlGrid, 15000, "$99.99");

        pearlGrid.row().padTop(100); // intentional space

        // Put exchange button on 3rd row in the middle column
        pearlGrid.add().colspan(2); // Skip first two columns
        addPearlMoneyExchangeButton(pearlGrid);
        pearlGrid.add().colspan(2); // Balance the row

        pearlContent.add(pearlGrid).expand().fill();

        contentStack.add(eggContent);
        contentStack.add(foodContent);
        contentStack.add(decorContent);
        contentStack.add(pearlContent);

        eggContent.setVisible(false);
        foodContent.setVisible(false);
        pearlContent.setVisible(false);

        root.add(contentStack).expand().fill().row();

        // --- Footer ---
        Table footer = new Table();
        TextButton faqButton = new TextButton("FAQ", skin);
        faqButton.getLabel().setFontScale(2.5f);
        faqButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showFaqDialog();
            }
        });

        TextButton backButton = new TextButton("BACK TO TANK", skin);
        backButton.getLabel().setFontScale(2.5f);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.showGameScreen();
            }
        });

        TextButton rightButton = new TextButton("Leaderboard & Stats", skin);
        rightButton.getLabel().setFontScale(2.5f);

        footer.add(faqButton).expandX().left().pad(20, 100, 100, 20).width(400).height(100);
        footer.add(backButton).expandX().center().pad(20, 20, 100, 20).width(500).height(100);
        footer.add(rightButton).expandX().right().pad(20, 20, 100, 100).width(400).height(100);
        root.add(footer).fillX().row();
    }

    private void showFaqDialog() {
        final Dialog dialog = new Dialog("Frequently Asked Questions", skin);
        dialog.getTitleLabel().setFontScale(2.5f);

        Table content = new Table(skin);
        content.pad(20);

        // --- Food Section ---
        Label foodHeader = new Label("--- FOOD VALUES ---", skin);
        foodHeader.setFontScale(2.0f);
        foodHeader.setColor(Color.CYAN);
        content.add(foodHeader).center().padBottom(10).row();

        String foodInfo = "Sunflower: 3\nPoppy: 6\nFlax: 8\nSesame: 10\nChia: 15\nHemp: 22\nPumpkin (Premium): 50\nQuinoa (Premium): 75";
        Label foodLabel = new Label(foodInfo, skin);
        foodLabel.setFontScale(1.8f);
        foodLabel.setWrap(true);
        content.add(foodLabel).width(800).padBottom(30).row();

        // --- Fish Section ---
        Label fishHeader = new Label("--- FISH MATURATION ---", skin);
        fishHeader.setFontScale(2.0f);
        fishHeader.setColor(Color.CYAN);
        content.add(fishHeader).center().padBottom(10).row();

        StringBuilder fishInfo = new StringBuilder();
        List<FishBreed> allBreeds = new ArrayList<>(Arrays.asList(FishBreed.values()));
        allBreeds.sort(Comparator.comparingInt(FishBreed::getMaxFillValue));
        for (FishBreed breed : allBreeds) {
            fishInfo.append(String.format(Locale.US, "%s: %d food points to mature\n", breed.getName(), breed.getMaxFillValue()));
        }
        Label fishLabel = new Label(fishInfo.toString(), skin);
        fishLabel.setFontScale(1.8f);
        fishLabel.setWrap(true);
        fishLabel.setAlignment(Align.center);
        content.add(fishLabel).width(800).padBottom(30).row();

        // --- General Tips ---
        Label tipsHeader = new Label("--- TIPS ---", skin);
        tipsHeader.setFontScale(2.0f);
        tipsHeader.setColor(Color.CYAN);
        content.add(tipsHeader).center().padBottom(10).row();

        String tipsInfo = "* Only young fish (that are not yet adults) will eat food.\n"
                + "* Bettafish will fight each other if they are adults.\n"
                + "* Angelfish will guard eggs from other fish.\n"
                + "* You can only sell adult fish.";
        Label tipsLabel = new Label(tipsInfo, skin);
        tipsLabel.setFontScale(1.8f);
        tipsLabel.setWrap(true);
        content.add(tipsLabel).width(800).padBottom(20).row();

        ScrollPane scrollPane = new ScrollPane(content, skin);
        dialog.getContentTable().add(scrollPane).width(1000).height(600);

        TextButton okBtn = new TextButton("Close", skin);
        okBtn.getLabel().setFontScale(2.5f);
        okBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
            }
        });

        dialog.getButtonTable().add(okBtn).width(300).height(80).pad(20);
        dialog.show(stage);
    }

    private void addPearlPurchaseButton(Table table, final int amount, String priceLabel) {
        TextButton button = new TextButton(formatter.format(amount) + " Pearls\n" + priceLabel, skin);
        button.getLabel().setFontScale(2.5f);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameManager.purchasePearls(amount);
            }
        });
        table.add(button).width(340).height(160).pad(10);
    }

    private void addPearlMoneyExchangeButton(Table table) {
        final int pearlPrice = 200;
        TextButton button = new TextButton("Exchange $" + formatter.format(pearlPrice) + "\nfor 1 Pearl", skin);
        button.getLabel().setFontScale(2.5f);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (gameManager.getMoney() >= pearlPrice) {
                    gameManager.setMoney(gameManager.getMoney() - pearlPrice);
                    gameManager.setPearls(gameManager.getPearls() + 1);
                    updateCurrencyLabels();
                } else {
                    showInsufficientFundsDialog(false);
                }
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
        addDecorButton(table, type, price, false, false);
    }

    private void addDecorButton(Table table, final String type, final float price, final boolean isPremium, final boolean isSpecial) {
        String displayName = type.equals("EggPus") ? "Octopus" : type;
        String label = displayName + "\n" + formatter.format(price) + (isPremium ? " Pearls" : "");
        if (!isPremium) label = displayName + "\n$" + formatter.format(price);

        TextButton button = new TextButton(label, skin);
        button.getLabel().setFontScale(2.2f);
        if (isPremium) button.getLabel().setColor(new Color(0.7f, 0.9f, 1f, 1f));

        // For special items, check if one is already owned
        if (isSpecial) {
            if (gameManager.getFixedDecor(type) != null) {
                 button.setDisabled(true);
                 button.getLabel().setColor(Color.GRAY);
                 button.setText(displayName + "\n[OWNED]");
            }
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

                if (isSpecial) {
                    showFixedDecorSlotPicker(type, price, isPremium);
                } else {
                    showSlotPicker(type, price, isPremium);
                }
            }
        });
        table.add(button).width(340).height(160).pad(10);
    }

    private void showSlotPicker(final String plantType, final float price, final boolean isPremium) {
        final Dialog dialog = new Dialog("Pick a Plant Spot", skin);
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
                        shop.buyDecor(plantType, price, slot, isPremium);
                        updateCurrencyLabels();
                        dialog.hide();
                        refreshDecorTab();
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

    private void showFixedDecorSlotPicker(final String decorType, final double price, final boolean isPremium) {
        final Dialog dialog = new Dialog("Pick a Special Spot", skin);
        dialog.getTitleLabel().setFontScale(2.0f);

        Table grid = new Table();
        String[] slotNames = {"Left", "Center", "Right"};
        boolean hasOpenSlot = false;

        for (int i = 0; i < 3; i++) {
            final int slotIndex = 15 + i;
            boolean isOccupied = gameManager.isSlotOccupied(slotIndex);
            String label = isOccupied ? "[TAKEN]" : slotNames[i];
            if (!isOccupied) hasOpenSlot = true;

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
                        shop.buyDecor(decorType, price, slotIndex, isPremium);
                        updateCurrencyLabels();
                        dialog.hide();
                        refreshDecorTab();
                    }
                }
            });
            grid.add(slotBtn).width(400).height(180).pad(10);
        }

        if (!hasOpenSlot) {
            dialog.getContentTable().clear();
            Label noRoomLabel = new Label("No special spots available!\nMove or sell an existing item.", skin);
            noRoomLabel.setFontScale(2.5f);
            noRoomLabel.setAlignment(Align.center);
            dialog.getContentTable().add(noRoomLabel).pad(50);
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

    private void addEggButton(Table table, final FishBreed breed) {
        final String breedName = breed.getName();
        final double price = breed.getBuyPrice();
        final boolean isPremium = breed.isPremium();

        String label = breedName + "\n" + formatter.format(price) + (isPremium ? " Pearls" : "");
        if (!isPremium) label = breedName + "\n$" + formatter.format(price);

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
                shop.buyEgg(new Egg(breedName, 5, price, isPremium));
                updateCurrencyLabels();
            }
        });
        table.add(button).width(340).height(160).pad(10);
    }

    private void addFoodButton(Table table, final String type, final double price, final double growth) {
        addFoodButton(table, type, price, growth, false);
    }

    private void addFoodButton(Table table, final String type, final double price, final double growth, final boolean isPremium) {
        String label = type + "\n" + formatter.format(price) + (isPremium ? " Pearls" : "");
        if (!isPremium) label = type + "\n$" + formatter.format(price);

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
