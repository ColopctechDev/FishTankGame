package com.fishtankgame.model;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.fishtankgame.game.GameManager;

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
    private EggObject targetEgg;
    private Fish schoolingLeader;
    private Vector2 schoolingOffset;
    private float chaseCooldown = 0;
    private boolean isLeader = false;
    private float patrolAngle = 0;
    private float fightTimer = 0;
    private float patrolTimer = 0;
    private int patrolWall = -1; // -1: not set, 0: top, 1: bottom, 2: left, 3: right
    private boolean isGuardingEgg = false;
    private float wanderTimer = MathUtils.random(2f, 5f);
    private GameManager gameManager;

    private float minScale = 0.04f;
    private float maxScale = 0.1f;
    private float currentScale;

    public Fish(String name, String breed, double price, float speed, Texture texture, int maxFillValue, GameManager gameManager) {
        this.name = name;
        this.breed = breed;
        this.fillValue = 0;
        this.price = price;
        this.baseSpeed = speed;
        this.textureRegion = new TextureRegion(texture);
        this.maxFillValue = maxFillValue;
        this.isAdult = false;
        this.currentScale = minScale;
        this.gameManager = gameManager;

        float scaledWidth = texture.getWidth() * currentScale;
        float scaledHeight = texture.getHeight() * currentScale;

        this.position = new Vector2(MathUtils.random(0, gameManager.getTankWidth() - scaledWidth), MathUtils.random(0, gameManager.getTankHeight() - scaledHeight));
        this.direction = new Vector2(MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f)).nor();
        this.bounds = new Rectangle(position.x, position.y, scaledWidth, scaledHeight);
        this.schoolingOffset = new Vector2(MathUtils.random(-100, 100), MathUtils.random(-100, 100));
    }

    public void update(float delta) {
        if (chaseCooldown > 0) {
            chaseCooldown -= delta;
        }
        if (fightTimer > 0) {
            fightTimer -= delta;
        }

        boolean isAvoidingWall = false;
        float scaledWidth = textureRegion.getRegionWidth() * currentScale;
        float scaledHeight = textureRegion.getRegionHeight() * currentScale;
        float margin = 20f;

        // --- Wall Collision ---
        float angle = direction.angleDeg();
        float snappedAngle = Math.round(angle / 45) * 45;
        float collisionWidth = scaledWidth;
        float collisionHeight = scaledHeight;

        if (snappedAngle % 90 != 0) { // is diagonal
            float rad = MathUtils.degreesToRadians * 45;
            collisionWidth = (scaledWidth * MathUtils.cos(rad) + scaledHeight * MathUtils.sin(rad));
            collisionHeight = (scaledWidth * MathUtils.sin(rad) + scaledHeight * MathUtils.cos(rad));
        }

        float centerX = position.x + scaledWidth / 2;
        float centerY = position.y + scaledHeight / 2;
        float halfCollisionWidth = collisionWidth / 2;
        float halfCollisionHeight = collisionHeight / 2;

        if (centerX - halfCollisionWidth < margin && direction.x < 0) {
            direction.x = -direction.x;
            isAvoidingWall = true;
        } else if (centerX + halfCollisionWidth > gameManager.getTankWidth() - margin && direction.x > 0) {
            direction.x = -direction.x;
            isAvoidingWall = true;
        }

        if (centerY - halfCollisionHeight < margin && direction.y < 0) {
            direction.y = -direction.y;
            isAvoidingWall = true;
        } else if (centerY + halfCollisionHeight > gameManager.getTankHeight() - margin && direction.y > 0) {
            direction.y = -direction.y;
            isAvoidingWall = true;
        }

        if (!isAvoidingWall) {
            // Priority 1: Actions with targets
            if (targetFood != null) {
                direction.set(new Vector2(targetFood.getBounds().x, targetFood.getBounds().y).sub(position)).nor();
                isGuardingEgg = false;
            } else if (targetFish != null) {
                Vector2 targetPosition = new Vector2(targetFish.getBounds().x, targetFish.getBounds().y);
                if (isFighting() && position.dst(targetPosition) < 30) {
                    startFightTimer();
                    patrolAngle += delta * 20;
                    direction.x = MathUtils.sin(patrolAngle);
                } else if (position.dst(targetPosition) < 20 && !isFighting()) {
                    clearTargetFish();
                    chaseCooldown = MathUtils.random(3f, 10f);
                } else {
                    direction.set(targetPosition.sub(position)).nor();
                }
                isGuardingEgg = false;
            } else if (targetEgg != null) {
                Vector2 targetPosition = targetEgg.getPosition();
                if (breed.equals("Angelfish") && isAdult()) {
                    if (position.dst(targetPosition) > 15 && !isGuardingEgg) {
                        direction.set(targetPosition.cpy().sub(position)).nor();
                    } else {
                        isGuardingEgg = true;
                        if (targetEgg.isFalling()) {
                            // Fall with egg, face same direction
                            position.y = targetPosition.y - (bounds.height / 2) + 8;
                            float targetX = targetPosition.x - (bounds.width / 2) + 8;
                            if (Math.abs(position.x - targetX) > 2) {
                                position.x += (targetX - position.x) * delta * 5;
                            }
                            direction.y = 0; // Maintain horizontal face
                        } else {
                            // At bottom, move slowly back and forth 20px
                            patrolAngle += delta * 2;
                            float cX = targetPosition.x - (bounds.width / 2) + 8;
                            float oldX = position.x;
                            position.x = cX + MathUtils.sin(patrolAngle) * 20;
                            position.y = targetPosition.y - (bounds.height / 2) + 8;

                            if (position.x > oldX) direction.x = 1;
                            else if (position.x < oldX) direction.x = -1;
                            direction.y = 0;
                        }
                    }
                } else {
                    if (position.dst(targetPosition) > 25) {
                        direction.set(targetPosition.cpy().sub(position)).nor();
                        isGuardingEgg = false;
                    } else {
                        patrolAngle += delta * 4;
                        direction.x = MathUtils.sin(patrolAngle);
                        direction.y = MathUtils.cos(patrolAngle) * 0.2f;
                        isGuardingEgg = false;
                    }
                }
            } else if (schoolingLeader != null) {
                Vector2 targetPosition = new Vector2(schoolingLeader.position).add(schoolingOffset);
                direction.set(targetPosition.sub(position)).nor();
                isGuardingEgg = false;
            } else {
                isGuardingEgg = false;
                // Priority 2: Idle behaviors
                if (breed.equals("Tigerfish") && isAdult()) {
                    handleTigerfishPatrol(delta, scaledWidth, scaledHeight);
                } else {
                    // Default wandering for other fish or non-adult tigerfish
                    wanderTimer -= delta;
                    if (wanderTimer <= 0) {
                        wanderTimer = MathUtils.random(2f, 6f);
                        direction.rotateDeg(MathUtils.random(-60f, 60f));
                    }
                }
            }
        }

        float currentSpeed = baseSpeed;
        if (isFighting()) {
            currentSpeed *= 2;
        }
        if (isLeader) {
            currentSpeed *= 1.05f;
        }
        if (isGuardingEgg) {
            currentSpeed = 0;
        }

        // Apply movement using delta for frame-rate independence (normalized to ~60fps)
        position.add(direction.cpy().nor().scl(currentSpeed * delta * 60));

        // --- Update Bounds after moving ---
        float finalAngle = direction.angleDeg();
        float finalSnappedAngle = Math.round(finalAngle / 45) * 45;
        float finalCollisionWidth = scaledWidth;
        float finalCollisionHeight = scaledHeight;

        if (finalSnappedAngle % 90 != 0) { // is diagonal
            float rad = MathUtils.degreesToRadians * 45;
            finalCollisionWidth = (scaledWidth * MathUtils.cos(rad) + scaledHeight * MathUtils.sin(rad));
            finalCollisionHeight = (scaledWidth * MathUtils.sin(rad) + scaledHeight * MathUtils.cos(rad));
        }

        float newCenterX = position.x + scaledWidth / 2;
        float newCenterY = position.y + scaledHeight / 2;
        bounds.set(newCenterX - finalCollisionWidth / 2, newCenterY - finalCollisionHeight / 2, finalCollisionWidth, finalCollisionHeight);
    }

    private void handleTigerfishPatrol(float delta, float scaledWidth, float scaledHeight) {
        patrolTimer -= delta;
        if (patrolTimer <= 0) {
            patrolTimer = 60f; // 1 minute
            patrolWall = MathUtils.random(0, 3);
            if (patrolWall == 0) direction.set(0, 1); // Top
            else if (patrolWall == 1) direction.set(0, -1); // Bottom
            else if (patrolWall == 2) direction.set(-1, 0); // Left
            else if (patrolWall == 3) direction.set(1, 0); // Right
        }

        float safeZone = 50f;
        if (patrolWall == 0 && position.y > gameManager.getTankHeight() - scaledHeight - safeZone) { // Top
            if (direction.y > 0) direction.set(MathUtils.randomBoolean() ? 1 : -1, 0);
        } else if (patrolWall == 1 && position.y < safeZone) { // Bottom
            if (direction.y < 0) direction.set(MathUtils.randomBoolean() ? 1 : -1, 0);
        } else if (patrolWall == 2 && position.x < safeZone) { // Left
            if (direction.x < 0) direction.set(0, MathUtils.randomBoolean() ? 1 : -1);
        } else if (patrolWall == 3 && position.x > gameManager.getTankWidth() - scaledWidth - safeZone) { // Right
            if (direction.x > 0) direction.set(0, MathUtils.randomBoolean() ? 1 : -1);
        }
    }

    public void draw(SpriteBatch batch) {
        float angle = direction.angleDeg();
        float snappedAngle = Math.round(angle / 45) * 45;

        boolean flipX = direction.x < 0;

        if (flipX != textureRegion.isFlipX()) {
            textureRegion.flip(true, false);
        }

        float rotation = 0;
        if (snappedAngle % 90 != 0) { // is diagonal
            if (flipX) {
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
