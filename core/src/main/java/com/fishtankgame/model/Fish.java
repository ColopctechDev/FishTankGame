package com.fishtankgame.model;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Fish {
    private String name;
    private String breed;
    private int fillValue;
    private int maxFillValue;
    private double price;
    private float baseSpeed;
    private Vector2 position;
    private Vector2 direction;
    private boolean isAdult;
    private TextureRegion textureRegion;
    private Rectangle bounds;
    private FoodPellet targetFood;
    private Fish targetFish;
    private Fish schoolingLeader;
    private Vector2 schoolingOffset;
    private float chaseCooldown = 0;
    private boolean isLeader = false;

    private float minScale = 0.04f;
    private float maxScale = 0.1f;
    private float currentScale;

    public Fish(String name, String breed, double price, float speed, Texture texture, int maxFillValue) {
        this.name = name;
        this.breed = breed;
        this.fillValue = 0;
        this.price = price;
        this.baseSpeed = speed;
        this.textureRegion = new TextureRegion(texture);
        this.maxFillValue = maxFillValue;
        this.isAdult = false;
        this.currentScale = minScale;

        float scaledWidth = texture.getWidth() * currentScale;
        float scaledHeight = texture.getHeight() * currentScale;

        this.position = new Vector2(MathUtils.random(0, 1280 - scaledWidth), MathUtils.random(0, 720 - scaledHeight));
        this.direction = new Vector2(MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f)).nor();
        this.bounds = new Rectangle(position.x, position.y, scaledWidth, scaledHeight);
        this.schoolingOffset = new Vector2(MathUtils.random(-100, 100), MathUtils.random(-100, 100));
    }

    public void update(float delta) {
        if (chaseCooldown > 0) {
            chaseCooldown -= delta;
        }

        // --- AI Decision Making ---
        boolean isAvoidingWall = false;
        float scaledWidth = textureRegion.getRegionWidth() * currentScale;
        float scaledHeight = textureRegion.getRegionHeight() * currentScale;
        float margin = 30f;

        if (position.x < margin) {
            direction.x = 1; isAvoidingWall = true;
        } else if (position.x > 1280 - scaledWidth - margin) {
            direction.x = -1; isAvoidingWall = true;
        }

        if (position.y < margin) {
            direction.y = 1; isAvoidingWall = true;
        } else if (position.y > 720 - scaledHeight - margin) {
            direction.y = -1; isAvoidingWall = true;
        }

        if (!isAvoidingWall) {
            if (targetFood != null) {
                Vector2 targetPosition = new Vector2(targetFood.getBounds().x, targetFood.getBounds().y);
                direction.set(targetPosition.sub(position)).nor();
            } else if (targetFish != null) {
                Vector2 targetPosition = new Vector2(targetFish.getBounds().x, targetFish.getBounds().y);
                if (position.dst(targetPosition) < 20) {
                    clearTargetFish();
                    chaseCooldown = MathUtils.random(3f, 10f);
                } else {
                    direction.set(targetPosition.sub(position)).nor();
                }
            } else if (schoolingLeader != null) {
                Vector2 targetPosition = new Vector2(schoolingLeader.position).add(schoolingOffset);
                direction.set(targetPosition.sub(position)).nor();
            }
        }

        float currentSpeed = isLeader ? baseSpeed * 0.8f : baseSpeed;
        position.add(direction.x * currentSpeed, direction.y * currentSpeed);
        bounds.setPosition(position);
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

    public void feed(int foodValue) {
        if (!isAdult) {
            fillValue += foodValue;
            if (fillValue >= maxFillValue) {
                fillValue = maxFillValue;
                isAdult = true;
            }
            currentScale = minScale + (maxScale - minScale) * ((float)fillValue / maxFillValue);
        }
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void setTargetFood(FoodPellet pellet) {
        this.targetFood = pellet;
    }

    public FoodPellet getTargetFood() {
        return targetFood;
    }

    public void clearTargetFood() {
        this.targetFood = null;
    }

    public void setTargetFish(Fish fish) {
        this.targetFish = fish;
    }

    public Fish getTargetFish() {
        return targetFish;
    }

    public void clearTargetFish() {
        this.targetFish = null;
    }

    public void setSchoolingLeader(Fish leader) {
        this.schoolingLeader = leader;
    }

    public Fish getSchoolingLeader() {
        return schoolingLeader;
    }

    public void setAsLeader() {
        this.isLeader = true;
    }

    public void clearAsLeader() {
        this.isLeader = false;
    }

    public boolean canChase() {
        return chaseCooldown <= 0;
    }

    public String getName() {
        return name;
    }

    public String getBreed() {
        return breed;
    }

    public int getFillValue() {
        return fillValue;
    }

    public double getPrice() {
        return price;
    }

    public boolean isAdult() {
        return isAdult;
    }
}
