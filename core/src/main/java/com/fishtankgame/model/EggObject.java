package com.fishtankgame.model;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class EggObject {
    private Egg eggData;
    private Vector2 position;
    private Vector2 velocity;
    private Texture texture;
    private Rectangle bounds;
    private float floorY;
    private float hatchTimer;

    public EggObject(Egg eggData, Texture texture, float x, float y) {
        this.eggData = eggData;
        this.texture = texture;
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(0, -MathUtils.random(12.5f, 25f)); // Sinking speed

        // Egg size adjusted (Reduced to 32 from 64)
        float size = 32;
        this.bounds = new Rectangle(x, y, size, size);
        this.floorY = MathUtils.random(30f, 50f); // Random floor position

        // Set hatch timer based on breed info
        FishBreed breedInfo = FishBreed.fromName(eggData.getBreed());
        this.hatchTimer = breedInfo.getHatchTime();
    }

    public void update(float delta) {
        // Falling logic
        if (position.y > floorY) {
            position.mulAdd(velocity, delta);
            bounds.setPosition(position);
        } else {
            position.y = floorY;
            velocity.y = 0;
        }

        // Hatching logic
        if (velocity.y == 0) { // Only start hatching once it has landed
            hatchTimer -= delta;
        }
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y, bounds.width, bounds.height);
    }

    public boolean isReadyToHatch() {
        return hatchTimer <= 0;
    }

    public Egg getEggData() {
        return eggData;
    }

    public Vector2 getPosition() {
        return position;
    }

    public boolean isFalling() {
        return velocity.y != 0;
    }

    public Vector2 getVelocity() {
        return velocity;
    }
}
