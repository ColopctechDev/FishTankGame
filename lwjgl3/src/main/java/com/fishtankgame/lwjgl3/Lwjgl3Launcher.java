package com.fishtankgame.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.fishtankgame.FishTankGame;
import com.fishtankgame.PlatformInterface;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        try {
            createApplication();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Lwjgl3Application createApplication() {
        // Desktop implementation: IAP not supported directly via this interface
        PlatformInterface desktopPlatform = new PlatformInterface() {
            @Override
            public void purchasePearls(int amount, PurchaseCallback callback) {
                System.out.println("Desktop: Purchase of " + amount + " pearls not implemented.");
                if (callback != null) callback.onFailure("Not supported on Desktop");
            }

            @Override
            public boolean isIAPSupported() {
                return false;
            }
        };
        return new Lwjgl3Application(new FishTankGame(desktopPlatform), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("Fishy Business");
        configuration.useVsync(true);
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        configuration.setWindowedMode(1280, 720);
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        configuration.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20, 0, 0);
        return configuration;
    }
}
