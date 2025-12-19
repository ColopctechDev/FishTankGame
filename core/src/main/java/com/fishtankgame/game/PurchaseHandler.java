package com.fishtankgame.game;

public interface PurchaseHandler {
    void purchasePremiumCurrency(int amount, PurchaseCallback callback);

    interface PurchaseCallback {
        void onSuccess();
        void onFailure(String message);
    }
}
