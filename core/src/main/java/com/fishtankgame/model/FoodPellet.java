package com.fishtankgame.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class FoodPellet {
    private final Food foodType;
    private final Vector2 position;
    private final Vector2 velocity;
    private final Texture texture;
    private final Rectangle bounds;
    private final float floorY;

    public FoodPellet(Food foodType, Texture texture, float x, float y) {
        this.foodType = foodType;
        this.texture = texture;
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(0, -MathUtils.random(6.25f, 12.5f)); // Halved sinking speed again
        this.bounds = new Rectangle(x, y, 8, 8);
        this.floorY = MathUtils.random(30f, 50f);
    }

    public void update(float delta) {
        if (position.y > floorY) {
            position.mulAdd(velocity, delta);
            bounds.setPosition(position);
        } else {
            position.y = floorY;
            velocity.y = 0;
        }
    }

    public void draw(SpriteBatch batch) {
        Color oldColor = batch.getColor().cpy();
        batch.setColor(Color.BROWN);
        batch.draw(texture, position.x, position.y, bounds.width, bounds.height);
        batch.setColor(oldColor);
    }

    public Food getFoodType() {
        return foodType;
    }

    public Rectangle getBounds() {
        return bounds;
    }
}
