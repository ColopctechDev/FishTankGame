package com.fishtankgame.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
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

    private Table eggContent;
    private Table foodContent;
    private Table decorContent;
    private Table gemsContent;

    private TextButton eggTabButton;
    private TextButton foodTabButton;
    private TextButton decorTabButton;
    private TextButton gemsTabButton;

    private ButtonGroup<TextButton> tabGroup;

    public ShopScreen(FishTankGame game, Shop shop, GameManager gameManager) {
        this.game = game;
        this.shop = shop;
        this.gameManager = gameManager;

        viewport = new ExtendViewport(FishTankGame.VIRTUAL_WIDTH, FishTankGame.VIRTUAL_HEIGHT);
        stage = new Stage(viewport);

        skin = new Skin(Gdx.files.internal("uiskin.json"));

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

        // Tabs
        Table tabs = new Table();
        eggTabButton = new TextButton("EGGS", skin, "toggle");
        foodTabButton = new TextButton("FOOD", skin, "toggle");
        decorTabButton = new TextButton("DECOR", skin, "toggle");
        gemsTabButton = new TextButton("GEMS", skin, "toggle");

        float tabFontScale = 2.2f;
        eggTabButton.getLabel().setFontScale(tabFontScale);
        foodTabButton.getLabel().setFontScale(tabFontScale);
        decorTabButton.getLabel().setFontScale(tabFontScale);
        gemsTabButton.getLabel().setFontScale(tabFontScale);

        tabGroup = new ButtonGroup<>(eggTabButton, foodTabButton, decorTabButton, gemsTabButton);
        tabGroup.setMaxCheckCount(1);
        tabGroup.setMinCheckCount(1);
        tabGroup.setUncheckLast(false);

        eggTabButton.setChecked(true);

        eggTabButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showTab(eggContent);
            }
        });
        foodTabButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showTab(foodContent);
            }
        });
        decorTabButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showTab(decorContent);
            }
        });
        gemsTabButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showTab(gemsContent);
            }
        });

        tabs.add(eggTabButton).expandX().fillX().height(80);
        tabs.add(foodTabButton).expandX().fillX().height(80);
        tabs.add(decorTabButton).expandX().fillX().height(80);
        tabs.add(gemsTabButton).expandX().fillX().height(80);
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

        // --- Decor Content (Empty for now) ---
        decorContent = new Table();
        decorContent.top();
        Label decorPlaceholder = new Label("Coming Soon...", skin);
        decorPlaceholder.setFontScale(2.5f);
        decorContent.add(decorPlaceholder).padTop(100);

        // --- Gems Content (Empty for now) ---
        gemsContent = new Table();
        gemsContent.top();
        Label gemsPlaceholder = new Label("Coming Soon...", skin);
        gemsPlaceholder.setFontScale(2.5f);
        gemsContent.add(gemsPlaceholder).padTop(100);

        contentStack.add(eggContent);
        contentStack.add(foodContent);
        contentStack.add(decorContent);
        contentStack.add(gemsContent);

        foodContent.setVisible(false);
        decorContent.setVisible(false);
        gemsContent.setVisible(false);

        root.add(contentStack).expand().fill().row();

        // Footer - higher positioning
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

    private void showTab(Table visibleTab) {
        eggContent.setVisible(visibleTab == eggContent);
        foodContent.setVisible(visibleTab == foodContent);
        decorContent.setVisible(visibleTab == decorContent);
        gemsContent.setVisible(visibleTab == gemsContent);
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
        button.getLabel().setFontScale(2.0f);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                shop.buyEgg(new Egg(breed, 5, price));
                updateMoneyLabel();
            }
        });
        table.add(button).width(280).height(130).pad(10);
    }

    private void addFoodButton(Table table, final String type, final int growth, final double price) {
        TextButton button = new TextButton(type + "\n$" + formatPrice(price), skin);
        button.getLabel().setFontScale(2.0f);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                shop.buyFood(new Food(type, growth, price));
                updateMoneyLabel();
            }
        });
        table.add(button).width(280).height(130).pad(10);
    }

    @Override
    public void show() {
        updateMoneyLabel();
        Gdx.input.setInputProcessor(stage);
    }

    private void updateMoneyLabel() {
        double money = gameManager.getMoney();
        String moneyText = (money == (long) money) ? String.format("%d", (long) money) : String.format("%.2f", money);
        moneyLabel.setText("Funds: $" + moneyText);
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
        skin.dispose();
    }
}
