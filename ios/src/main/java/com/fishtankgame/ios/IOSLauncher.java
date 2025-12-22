package com.fishtankgame.ios;

import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.uikit.UIApplication;

import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;
import com.fishtankgame.FishTankGame;
import com.fishtankgame.PlatformInterface;

public class IOSLauncher extends IOSApplication.Delegate implements PlatformInterface {
    @Override
    protected IOSApplication createApplication() {
        IOSApplicationConfiguration config = new IOSApplicationConfiguration();
        config.orientationLandscape = true;
        config.orientationPortrait = false;
        return new IOSApplication(new FishTankGame(this), config);
    }

    public static void main(String[] argv) {
        NSAutoreleasePool pool = new NSAutoreleasePool();
        UIApplication.main(argv, null, IOSLauncher.class);
        pool.close();
    }

    @Override
    public void purchasePearls(int amount, PurchaseCallback callback) {
        // TODO: Implement iOS StoreKit logic here
        // For now, we simulate a successful purchase for testing purposes, similar to desktop
        if (callback != null) {
            callback.onSuccess(amount);
        }
    }

    @Override
    public boolean isIAPSupported() {
        // Return true to enable shop features, assuming IAP will be implemented
        return true;
    }
}
