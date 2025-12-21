# libGDX Proguard Rules - DISABLE OBFUSCATION
# This prevents different classes from being renamed to the same letter (e.g., 'a'),
# which causes the "Attempt to add pool with already existing class" crash.
-dontobfuscate

-keep class com.badlogic.** { *; }
-keep enum com.badlogic.** { *; }
-dontwarn com.badlogic.**

# Keep your game logic entirely
-keep class com.fishtankgame.** { *; }
-keep enum com.fishtankgame.** { *; }

# Keep the platform interface and all its implementations
-keep class com.fishtankgame.PlatformInterface* { *; }
-keep interface com.fishtankgame.PlatformInterface* { *; }

# Google Play Billing Library
-keep class com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**

# Android-specific keeps
-keep class com.badlogic.gdx.backends.android.** { *; }
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses

# Native method handling
-keepclasseswithmembernames class * {
    native <methods>;
}
