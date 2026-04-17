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

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep metadata used by Kotlin/reflective serializers where needed.
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Firebase Realtime Database deserializes these models reflectively.
-keep class com.alvaroquintana.domain.Country { *; }
-keep class com.alvaroquintana.domain.Currency { *; }
-keep class com.alvaroquintana.domain.Language { *; }

# Crashlytics needs line tables + mapping; avoid leaking real source file names.
-keepattributes LineNumberTable
-renamesourcefileattribute SourceFile
