package com.fishtankgame.effects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.Color;

public class Bubble {
    private final Vector2 position;
    private final float speed;
    private final Texture texture;
    private final float scale;
    private final float tankHeight;

    public Bubble(Texture texture, float x, float y, float tankHeight) {
        this.texture = texture;
        this.position = new Vector2(x, y);
        this.tankHeight = tankHeight;
        this.speed = MathUtils.random(1.5f, 3.5f);
        this.scale = MathUtils.random(0.015f, 0.035f);
    }

    public void update(float delta) {
        position.y += speed * 60 * delta;
    }

    public void draw(SpriteBatch batch) {
        Color color = batch.getColor();
        batch.setColor(color.r, color.g, color.b, 0.4f);
        batch.draw(texture, position.x, position.y, texture.getWidth() * scale, texture.getHeight() * scale);
        batch.setColor(color.r, color.g, color.b, 1f);
    }

    public boolean isOffScreen() {
        return position.y > tankHeight;
    }
}
