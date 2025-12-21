package com.fishtankgame.model;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.fishtankgame.game.GameManager;

public class Fish {
    private final String name;
    private final String breed;
    private int fillValue;
    private final int maxFillValue;
    private final double price;
    private final float baseSpeed;
    private final Vector2 position;
    private final Vector2 direction;
    private boolean isAdult;
    private final TextureRegion textureRegion;
    private final Rectangle bounds;
    private FoodPellet targetFood;
    private Fish targetFish;
    private EggObject targetEgg;
    private Fish schoolingLeader;
    private final Vector2 schoolingOffset;
    private float chaseCooldown = 0;
    private boolean isLeader = false;
    private float patrolAngle = 0;
    private float fightTimer = 0;
    private float patrolTimer = 0;
    private int patrolWall = -1; // -1: not set, 0: top, 1: bottom, 2: left, 3: right
    private boolean isGuardingEgg = false;
    private float wanderTimer = MathUtils.random(2f, 5f);
    private final GameManager gameManager;
    private final boolean isPremium;

    private float minScale = 0.053f; // Starting size increased by 33% from 0.04f
    private float maxScale = 0.1f;
    private float currentScale;

    private boolean lastFlipX = false;
    private boolean isBehindDecor = false;

    public Fish(String name, String breed, double price, float speed, Texture texture, int maxFillValue, GameManager gameManager) {
        this(name, breed, price, speed, texture, maxFillValue, gameManager, null);
    }

    public Fish(String name, String breed, double price, float speed, Texture texture, int maxFillValue, GameManager gameManager, Vector2 spawnPosition) {
        this.name = name;
        this.breed = breed;
        this.fillValue = 0;
        this.price = price;
        this.baseSpeed = speed;
        this.textureRegion = new TextureRegion(texture);
        this.maxFillValue = maxFillValue;
        this.isAdult = false;
        this.gameManager = gameManager;

        FishBreed breedInfo = FishBreed.fromName(breed);
        this.isPremium = breedInfo.isPremium();

        // Premium fish scales (Starting size also increased by 33% from 0.08f)
        if (this.isPremium) {
            this.minScale = 0.106f;
            this.maxScale = 0.2f;
        }

        this.currentScale = minScale;

        float scaledWidth = texture.getWidth() * currentScale;
        float scaledHeight = texture.getHeight() * currentScale;

        if (spawnPosition != null) {
            this.position = new Vector2(spawnPosition.x, spawnPosition.y);
        } else {
            this.position = new Vector2(MathUtils.random(0, gameManager.getTankWidth() - scaledWidth), MathUtils.random(0, gameManager.getTankHeight() - scaledHeight));
        }

        this.direction = new Vector2(MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f)).nor();
        if (direction.len() == 0) direction.set(1, 0);
        this.bounds = new Rectangle(position.x, position.y, scaledWidth, scaledHeight);
        this.schoolingOffset = new Vector2(MathUtils.random(-100, 100), MathUtils.random(-100, 100));
        this.lastFlipX = direction.x < 0;
    }

    public void update(float delta) {
        if (chaseCooldown > 0) {
            chaseCooldown -= delta;
        }
        if (fightTimer > 0) {
            fightTimer -= delta;
        }

        float scaledWidth = textureRegion.getRegionWidth() * currentScale;
        float scaledHeight = textureRegion.getRegionHeight() * currentScale;
        float margin = 40f;

        // --- Robust Wall Collision ---
        boolean isCollidingWall = false;
        if (position.x < margin) {
            position.x = margin;
            direction.x = Math.abs(direction.x);
            if (direction.x < 0.3f) direction.x = 0.5f;
            isCollidingWall = true;
        } else if (position.x + scaledWidth > gameManager.getTankWidth() - margin) {
            position.x = gameManager.getTankWidth() - margin - scaledWidth;
            direction.x = -Math.abs(direction.x);
            if (direction.x > -0.3f) direction.x = -0.5f;
            isCollidingWall = true;
        }

        if (position.y < margin) {
            position.y = margin;
            direction.y = Math.abs(direction.y);
            if (direction.y < 0.3f) direction.y = 0.5f;
            isCollidingWall = true;
        } else if (position.y + scaledHeight > gameManager.getTankHeight() - margin) {
            position.y = gameManager.getTankHeight() - margin - scaledHeight;
            direction.y = -Math.abs(direction.y);
            if (direction.y > -0.3f) direction.y = -0.5f;
            isCollidingWall = true;
        }

        if (isCollidingWall) {
            direction.nor();
            isGuardingEgg = false;
        }

        if (!isCollidingWall) {
            // Priority 1: Actions with targets
            if (targetFood != null) {
                direction.set(new Vector2(targetFood.getBounds().x, targetFood.getBounds().y).sub(position)).nor();
                isGuardingEgg = false;
            } else if (targetFish != null) {
                Vector2 targetPosition = new Vector2(targetFish.getBounds().x, targetFish.getBounds().y);
                float dist = position.dst(targetPosition);
                if (isFighting() && dist < 30) {
                    startFightTimer();
                    patrolAngle += delta * 20;
                    direction.x = MathUtils.sin(patrolAngle);
                } else if (dist < 20 && !isFighting()) {
                    clearTargetFish();
                    chaseCooldown = MathUtils.random(3f, 10f);
                } else if (dist > 5) {
                    direction.set(targetPosition.cpy().sub(position)).nor();
                }
                isGuardingEgg = false;
            } else if (targetEgg != null) {
                Vector2 targetPosition = targetEgg.getPosition();
                float dist = position.dst(targetPosition);

                if (breed.equals("Angelfish") && isAdult()) {
                    if (dist > 15 && !isGuardingEgg) {
                        direction.set(targetPosition.cpy().sub(position)).nor();
                    } else {
                        isGuardingEgg = true;
                        if (targetEgg.isFalling()) {
                            float targetY = targetPosition.y - (scaledHeight / 2) + 32;
                            position.y = MathUtils.clamp(targetY, margin, gameManager.getTankHeight() - margin - scaledHeight);

                            float targetX = targetPosition.x - (scaledWidth / 2) + 32;
                            float clampedTargetX = MathUtils.clamp(targetX, margin, gameManager.getTankWidth() - margin - scaledWidth);

                            if (Math.abs(position.x - clampedTargetX) > 2) {
                                position.x += (clampedTargetX - position.x) * delta * 5;
                                direction.x = (clampedTargetX > position.x) ? 1 : -1;
                            }
                            direction.y = 0;
                        } else {
                            patrolAngle += delta * 2;
                            float cX = targetPosition.x - (scaledWidth / 2) + 32;
                            float oldX = position.x;

                            float targetX = cX + MathUtils.sin(patrolAngle) * 20;
                            position.x = MathUtils.clamp(targetX, margin, gameManager.getTankWidth() - margin - scaledWidth);

                            float targetY = targetPosition.y - (scaledHeight / 2) + 32;
                            position.y = MathUtils.clamp(targetY, margin, gameManager.getTankHeight() - margin - scaledHeight);

                            if (position.x > oldX + 0.5f) direction.x = 1;
                            else if (position.x < oldX - 0.5f) direction.x = -1;
                            direction.y = 0;
                        }
                    }
                } else {
                    if (dist > 25) {
                        direction.set(targetPosition.cpy().sub(position)).nor();
                        isGuardingEgg = false;
                    } else {
                        // All non-angel fish just circle the egg
                        patrolAngle += delta * 4;
                        direction.x = MathUtils.sin(patrolAngle);
                        direction.y = MathUtils.cos(patrolAngle) * 0.2f;
                        isGuardingEgg = false;
                    }
                }
            } else if (schoolingLeader != null) {
                Vector2 targetPosition = new Vector2(schoolingLeader.position).add(schoolingOffset);
                float dist = position.dst(targetPosition);
                if (dist > 15) {
                    direction.set(targetPosition.sub(position)).nor();
                } else {
                    direction.set(schoolingLeader.direction).nor();
                }
                isGuardingEgg = false;
            } else {
                isGuardingEgg = false;
                // Priority 2: Idle behaviors
                if (breed.equals("Tigerfish") && isAdult()) {
                    handleTigerfishPatrol(delta, scaledWidth, scaledHeight, margin);
                } else if (breed.equals("Clownfish") && isAdult()) {
                    handleClownfishPatrol(delta, scaledWidth, scaledHeight, margin);
                } else if (breed.equals("Angelfish") && isAdult()) {
                    handleAngelfishPatrol(delta, scaledWidth, scaledHeight, margin);
                } else if (breed.equals("Goldfish") && isAdult()) {
                    handleGoldfishPatrol(delta, scaledWidth, scaledHeight, margin);
                } else {
                    // Default wandering
                    wanderTimer -= delta;
                    if (wanderTimer <= 0) {
                        wanderTimer = MathUtils.random(3f, 8f);
                        direction.rotateDeg(MathUtils.random(-45f, 45f));

                        if (position.y < gameManager.getTankHeight() * 0.3f) {
                            direction.y += 0.5f;
                        }
                        if (position.y > gameManager.getTankHeight() * 0.7f) {
                            direction.y -= 0.3f;
                        }
                        direction.nor();
                    }
                }
            }
        }

        float currentSpeed = baseSpeed;
        if (isFighting()) {
            currentSpeed *= 2;
        }
        if (isLeader) {
            currentSpeed *= 1.1f;
        }
        if (isGuardingEgg) {
            currentSpeed = 0;
        }

        // Apply movement
        if (direction.len() > 0) {
            position.add(direction.cpy().nor().scl(currentSpeed * delta * 60));
        }

        // --- Update Bounds ---
        float finalAngle = direction.angleDeg();
        float finalSnappedAngle = Math.round(finalAngle / 45) * 45;
        float finalCollisionWidth = scaledWidth;
        float finalCollisionHeight = scaledHeight;

        if (finalSnappedAngle % 90 != 0) {
            float rad = MathUtils.degreesToRadians * 45;
            finalCollisionWidth = (scaledWidth * MathUtils.cos(rad) + scaledHeight * MathUtils.sin(rad));
            finalCollisionHeight = (scaledWidth * MathUtils.sin(rad) + scaledHeight * MathUtils.cos(rad));
        }

        float newCenterX = position.x + scaledWidth / 2;
        float newCenterY = position.y + scaledHeight / 2;
        bounds.set(newCenterX - finalCollisionWidth / 2, newCenterY - finalCollisionHeight / 2, finalCollisionWidth, finalCollisionHeight);
    }

    private void handleTigerfishPatrol(float delta, float scaledWidth, float scaledHeight, float margin) {
        patrolTimer -= delta;
        if (patrolTimer <= 0 || patrolWall == -1) {
            patrolTimer = 60f; // Paces for 60 seconds
            patrolWall = MathUtils.random(0, 3); // 0: Top, 1: Bottom, 2: Left, 3: Right

            // Set initial pacing direction
            if (patrolWall == 0 || patrolWall == 1) {
                direction.set(MathUtils.randomBoolean() ? 1 : -1, 0);
            } else {
                direction.set(0, MathUtils.randomBoolean() ? 1 : -1);
            }
        }

        float wallOffset = margin + 5f;
        if (patrolWall == 0) { // Top (Roof)
            float targetY = gameManager.getTankHeight() - scaledHeight - wallOffset;
            position.y += (targetY - position.y) * delta * 2;
            direction.y = 0;
            if (Math.abs(direction.x) < 0.1f) direction.x = 1;
        } else if (patrolWall == 1) { // Bottom (Floor)
            float targetY = wallOffset;
            position.y += (targetY - position.y) * delta * 2;
            direction.y = 0;
            if (Math.abs(direction.x) < 0.1f) direction.x = 1;
        } else if (patrolWall == 2) { // Left Wall
            float targetX = wallOffset;
            position.x += (targetX - position.x) * delta * 2;
            direction.x = 0;
            if (Math.abs(direction.y) < 0.1f) direction.y = 1;
        } else if (patrolWall == 3) { // Right Wall
            float targetX = wallOffset;
            position.x += (targetX - position.x) * delta * 2;
            direction.x = 0;
            if (Math.abs(direction.y) < 0.1f) direction.y = 1;
        }
        direction.nor();
    }

    private void handleClownfishPatrol(float delta, float scaledWidth, float scaledHeight, float margin) {
        wanderTimer -= delta;
        if (wanderTimer <= 0) {
            wanderTimer = MathUtils.random(3f, 7f);
            direction.rotateDeg(MathUtils.random(-45f, 45f));
            if (position.y > gameManager.getTankHeight() * 0.5f) {
                direction.y -= 0.5f;
            } else if (position.y < gameManager.getTankHeight() * 0.15f) {
                direction.y += 0.5f;
            }
            direction.nor();

            // Randomly decide to swim behind decor
            isBehindDecor = MathUtils.randomBoolean();
        }
    }

    private void handleAngelfishPatrol(float delta, float scaledWidth, float scaledHeight, float margin) {
        wanderTimer -= delta;
        if (wanderTimer <= 0) {
            wanderTimer = MathUtils.random(4f, 9f);
            direction.rotateDeg(MathUtils.random(-30f, 30f));
            if (position.y < gameManager.getTankHeight() * 0.4f) {
                direction.y += 0.4f;
            }
            direction.nor();
        }
    }

    private void handleGoldfishPatrol(float delta, float scaledWidth, float scaledHeight, float margin) {
        wanderTimer -= delta;
        if (wanderTimer <= 0) {
            wanderTimer = MathUtils.random(3f, 8f);
            direction.rotateDeg(MathUtils.random(-60f, 60f));
            if (position.y > gameManager.getTankHeight() * 0.6f) {
                direction.y -= 0.4f;
            } else if (position.y < gameManager.getTankHeight() * 0.2f) {
                direction.y += 0.4f;
            }
            direction.nor();
        }
    }

    public void draw(SpriteBatch batch) {
        if (Math.abs(direction.x) > 0.1f) {
            lastFlipX = direction.x < 0;
        }

        if (lastFlipX != textureRegion.isFlipX()) {
            textureRegion.flip(true, false);
        }

        float angle = direction.angleDeg();
        float snappedAngle = Math.round(angle / 45) * 45;
        float rotation = 0;
        if (snappedAngle % 90 != 0) {
            if (lastFlipX) {
                rotation = snappedAngle - 180;
            } else {
                rotation = snappedAngle;
            }
        }

        batch.draw(textureRegion, position.x, position.y, textureRegion.getRegionWidth() * currentScale / 2, textureRegion.getRegionHeight() * currentScale / 2,
                textureRegion.getRegionWidth(), textureRegion.getRegionHeight(),
                currentScale, currentScale, rotation);
    }

    public void feed(int foodValue) {
        if (!isAdult) {
            fillValue += foodValue;
            if (fillValue >= maxFillValue) {
                fillValue = maxFillValue;
                isAdult = true;
                clearTargetFood();
                clearTargetFish();
                clearTargetEgg();
                schoolingLeader = null;
            }
            currentScale = minScale + (maxScale - minScale) * ((float) fillValue / maxFillValue);
        }
    }

    public void startFightTimer() {
        if (fightTimer <= 0) {
            fightTimer = 5f;
        }
    }

    public float getFightTimer() {
        return fightTimer;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isFighting() {
        return this.breed.equals("Betafish") && this.targetFish != null && this.targetFish.getBreed().equals("Betafish");
    }

    public void setTargetFood(FoodPellet pellet) {
        this.targetFood = pellet;
    }

    public FoodPellet getTargetFood() {
        return targetFood;
    }

    public void clearTargetFood() {
        this.targetFood = null;
        this.isGuardingEgg = false;
        this.direction.set(MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f)).nor();
        if (direction.len() == 0) direction.set(1, 0);
    }

    public void setTargetFish(Fish fish) {
        this.targetFish = fish;
    }

    public Fish getTargetFish() {
        return targetFish;
    }

    public void clearTargetFish() {
        this.targetFish = null;
        this.isGuardingEgg = false;
        this.direction.set(MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f)).nor();
        if (direction.len() == 0) direction.set(1, 0);
    }

    public void setSchoolingLeader(Fish leader) {
        this.schoolingLeader = leader;
    }

    public Fish getSchoolingLeader() {
        return schoolingLeader;
    }

    public void setTargetEgg(EggObject egg) {
        this.targetEgg = egg;
    }

    public EggObject getTargetEgg() {
        return targetEgg;
    }

    public void clearTargetEgg() {
        this.targetEgg = null;
        this.isGuardingEgg = false;
        this.direction.set(MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f)).nor();
        if (direction.len() == 0) direction.set(1, 0);
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

    public boolean isBehindDecor() {
        return isBehindDecor;
    }

    public boolean isPremium() {
        return isPremium;
    }
}
