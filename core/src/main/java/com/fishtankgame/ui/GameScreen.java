package com.fishtankgame.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.fishtankgame.FishTankGame;
import com.fishtankgame.effects.Bubble;
import com.fishtankgame.game.GameManager;
import com.fishtankgame.model.Fish;

import java.util.List;

public class GameScreen extends ScreenAdapter {
    private FishTankGame game;
    private SpriteBatch batch;
    private GameManager gameManager;

    private Texture background;
    private List<Bubble> bubbles;

    private Stage stage;
    private Skin skin;

    public GameScreen(FishTankGame game, GameManager gameManager, SpriteBatch batch, List<Bubble> bubbles, Texture background) {
        this.game = game;
        this.gameManager = gameManager;
        this.batch = batch;
        this.bubbles = bubbles;
        this.background = background;

        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        TextButton shopButton = new TextButton("Shop", skin);
        shopButton.setPosition(1100, 650);
        shopButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.showShopScreen();
            }
        });
        stage.addActor(shopButton);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        gameManager.update();

        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        for (Bubble bubble : bubbles) {
            bubble.update();
            bubble.draw(batch);
        }

        for (Fish fish : gameManager.getFishList()) {
            fish.update();
            fish.draw(batch);
        }
        batch.end();

        stage.act(delta);
        stage.draw();
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
