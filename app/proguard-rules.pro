# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Preserve line number information for debugging
-keepattributes SourceFile,LineNumberTable

# Google Mobile Ads SDK ProGuard Rules
-keep class com.google.android.gms.ads.** { *; }
-keep interface com.google.android.gms.ads.** { *; }

# Meta (Facebook) Audience Network Mediation Adapter Rules
-keep class com.facebook.ads.** { *; }
-keep interface com.facebook.ads.** { *; }
-dontwarn com.facebook.ads.**