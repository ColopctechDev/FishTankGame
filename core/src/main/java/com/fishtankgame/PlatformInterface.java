package com.fishtankgame;

public interface PlatformInterface {
    /**
     * Triggers the platform-specific in-app purchase flow for pearls.
     * @param amount The number of pearls the user wants to buy.
     * @param callback The callback to notify when the purchase is complete.
     */
    void purchasePearls(int amount, PurchaseCallback callback);

    /**
     * Returns whether the platform currently supports in-app purchases.
     */
    boolean isIAPSupported();

    interface PurchaseCallback {
        void onSuccess(int amount);
        void onFailure(String message);
    }
}
