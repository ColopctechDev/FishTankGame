package com.fishtankgame.android;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.fishtankgame.FishTankGame;
import com.fishtankgame.PlatformInterface;

import java.util.ArrayList;
import java.util.List;

public class AndroidLauncher extends AndroidApplication implements PlatformInterface, PurchasesUpdatedListener {
    private BillingClient billingClient;
    private boolean isBillingClientReady = false;
    private PurchaseCallback pendingCallback;
    private int pendingAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initBillingClient();

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useImmersiveMode = true;
        initialize(new FishTankGame(this), config);
    }

    private void initBillingClient() {
        billingClient = BillingClient.newBuilder(this)
                .setListener(this)
                .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    isBillingClientReady = true;
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                isBillingClientReady = false;
            }
        });
    }

    @Override
    public void purchasePearls(int amount, PurchaseCallback callback) {
        if (!isBillingClientReady) {
            if (callback != null) callback.onFailure("Billing service not ready.");
            return;
        }

        this.pendingCallback = callback;
        this.pendingAmount = amount;

        String productId = "pearls_" + amount;

        List<QueryProductDetailsParams.Product> productList = new ArrayList<>();
        productList.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build());

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        billingClient.queryProductDetailsAsync(params, (billingResult, productDetailsResult) -> {
            List<ProductDetails> productDetailsList = productDetailsResult.getProductDetailsList();
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && productDetailsList != null && !productDetailsList.isEmpty()) {
                ProductDetails productDetails = productDetailsList.get(0);

                List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList = new ArrayList<>();
                productDetailsParamsList.add(BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build());

                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build();

                billingClient.launchBillingFlow(this, billingFlowParams);
            } else {
                if (callback != null) callback.onFailure("Product not found: " + productId);
            }
        });
    }

    @Override
    public boolean isIAPSupported() {
        return isBillingClientReady;
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else {
            if (pendingCallback != null) {
                pendingCallback.onFailure("Purchase failed: " + billingResult.getDebugMessage());
                pendingCallback = null;
            }
        }
    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            // Pearls are consumables, so we must consume them to allow re-purchase
            ConsumeParams consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.getPurchaseToken())
                    .build();

            billingClient.consumeAsync(consumeParams, (billingResult, purchaseToken) -> {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    if (pendingCallback != null) {
                        pendingCallback.onSuccess(pendingAmount);
                        pendingCallback = null;
                    }
                    runOnUiThread(() -> Toast.makeText(getContext(), "Pearls added to your account!", Toast.LENGTH_SHORT).show());
                } else {
                    if (pendingCallback != null) {
                        pendingCallback.onFailure("Failed to consume purchase.");
                        pendingCallback = null;
                    }
                }
            });
        }
    }
}
