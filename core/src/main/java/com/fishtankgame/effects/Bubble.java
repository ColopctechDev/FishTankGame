package com.fishtankgame.effects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.Color;

public class Bubble {
    private Vector2 position;
    private float speed;
    private Texture texture;
    private float scale = MathUtils.random(0.0125f, 0.0375f); // Adjusted scale

    public Bubble(Texture texture) {
        this.texture = texture;
        this.position = new Vector2(MathUtils.random(0, 1280), MathUtils.random(-100, -10));
        this.speed = MathUtils.random(1, 3);
    }

    public void update() {
        position.y += speed;
        if (position.y > 720) {
            position.x = MathUtils.random(0, 1280);
            position.y = MathUtils.random(-100, -10);
        }
    }

    public void draw(SpriteBatch batch) {
        Color color = batch.getColor();
        batch.setColor(color.r, color.g, color.b, 0.5f); // 50% transparent
        batch.draw(texture, position.x, position.y, texture.getWidth() * scale, texture.getHeight() * scale);
        batch.setColor(color.r, color.g, color.b, 1f); // Reset to opaque
    }
}
