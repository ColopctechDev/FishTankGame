package com.fishtankgame.model;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Fish {
    private String name;
    private String breed;
    private int age;
    private double price;
    private float speed;
    private Vector2 position;
    private Vector2 direction;
    private boolean isAdult;
    private TextureRegion textureRegion;

    private float minScale = 0.02f; // 1/10th of original 0.2f
    private float maxScale = 0.1f;  // half of original 0.2f
    private float currentScale;
    private int maxAge = 20; // Age at which fish is fully grown

    public Fish(String name, String breed, double price, float speed, Texture texture) {
        this.name = name;
        this.breed = breed;
        this.age = 0;
        this.price = price;
        this.speed = speed;
        this.textureRegion = new TextureRegion(texture);
        this.isAdult = false;
        this.currentScale = minScale;

        float scaledWidth = texture.getWidth() * currentScale;
        float scaledHeight = texture.getHeight() * currentScale;

        this.position = new Vector2(MathUtils.random(0, 1280 - scaledWidth), MathUtils.random(0, 720 - scaledHeight));
        this.direction = new Vector2(MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f)).nor();
    }

    public void update() {
        position.add(direction.x * speed, direction.y * speed);

        float scaledWidth = textureRegion.getRegionWidth() * currentScale;
        float scaledHeight = textureRegion.getRegionHeight() * currentScale;

        // Wall bouncing and clamping logic
        if (position.x < 0) {
            position.x = 0;
            direction.x = -direction.x;
        } else if (position.x > 1280 - scaledWidth) {
            position.x = 1280 - scaledWidth;
            direction.x = -direction.x;
        }

        if (position.y < 0) {
            position.y = 0;
            direction.y = -direction.y;
        } else if (position.y > 720 - scaledHeight) {
            position.y = 720 - scaledHeight;
            direction.y = -direction.y;
        }
    }

    public void draw(SpriteBatch batch) {
        if (direction.x < 0 && !textureRegion.isFlipX()) {
            textureRegion.flip(true, false);
        } else if (direction.x > 0 && textureRegion.isFlipX()) {
            textureRegion.flip(true, false);
        }

        batch.draw(textureRegion, position.x, position.y, 0, 0,
                textureRegion.getRegionWidth(), textureRegion.getRegionHeight(),
                currentScale, currentScale, 0);
    }

    public void grow() {
        if (age < maxAge) {
            age++;
            currentScale = minScale + (maxScale - minScale) * ((float)age / maxAge);
        }
        if (age >= 10 && !isAdult) {
            isAdult = true;
        }
    }

    public String getName() {
        return name;
    }

    public String getBreed() {
        return breed;
    }

    public int getAge() {
        return age;
    }

    public double getPrice() {
        return price;
    }

    public boolean isAdult() {
        return isAdult;
    }
}
