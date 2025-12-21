package com.fishtankgame.model;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Decor {
    private final String type;
    private final Vector2 position;
    private final float scale;
    private final Texture texture;
    private final Rectangle bounds;
    private int slotIndex = -1;
    private final float xPercent;
    private final double purchasePrice;

    public Decor(String type, Texture texture, float x, float y, float scale, int slotIndex, float xPercent, double purchasePrice) {
        this.type = type;
        this.texture = texture;
        this.position = new Vector2(x, y);
        this.scale = scale;
        this.slotIndex = slotIndex;
        this.xPercent = xPercent;
        this.purchasePrice = purchasePrice;

        float width = texture.getWidth() * scale;
        float height = texture.getHeight() * scale;
        this.bounds = new Rectangle(x, y, width, height);
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y, bounds.width, bounds.height);
    }

    public void updatePosition(float newX) {
        this.position.x = newX;
        this.bounds.x = newX;
    }

    public String getType() {
        return type;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public float getXPercent() {
        return xPercent;
    }

    public float getScale() {
        return scale;
    }

    public Vector2 getPosition() {
        return position;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }
}
