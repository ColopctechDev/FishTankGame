# libGDX Proguard Rules
-keep class com.badlogic.gdx.backends.android.AndroidInput* { *; }
-keep class com.badlogic.gdx.backends.android.AndroidAudio* { *; }
-keep class com.badlogic.gdx.backends.android.AndroidGraphics* { *; }
-keep class com.badlogic.gdx.backends.android.AndroidApplication* { *; }
-keep class com.badlogic.gdx.backends.android.AndroidNet* { *; }
-keep class com.badlogic.gdx.backends.android.AndroidFiles* { *; }
-keep class com.badlogic.gdx.backends.android.AndroidThreading* { *; }
-keep class com.badlogic.gdx.backends.android.AndroidEventListener { *; }
-keep class com.badlogic.gdx.backends.android.SurfaceViewWrapper { *; }
-keep class com.badlogic.gdx.backends.android.AndroidLiveWallpaperService { *; }
-keep class com.badlogic.gdx.backends.android.AndroidClipboard { *; }
-keep class com.badlogic.gdx.backends.android.AndroidAccessibility { *; }

# Keeping all natives
-keep class com.badlogic.gdx.physics.box2d.World { *; }
-keep class com.badlogic.gdx.physics.box2d.ContactListener { *; }

# Keep the platform interface and its callbacks
-keep class com.fishtankgame.PlatformInterface* { *; }

# Google Play Billing Library
-keep class com.android.billingclient.** { *; }

# General libGDX
-dontwarn com.badlogic.gdx.backends.android.AndroidFragmentApplication
-dontwarn com.badlogic.gdx.utils.GdxBuild
-dontwarn com.badlogic.gdx.physics.box2d.gwt.**
-dontwarn com.badlogic.gdx.jnigen.**
-keepattributes Signature
-keepattributes *Annotation*
