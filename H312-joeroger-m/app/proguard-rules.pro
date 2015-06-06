# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Un-comment if you do not want your application obfuscated
#-dontobfuscate

# --------------------------------------------------------------------
# REMOVE all Log messages except warnings and errors. This explicitly
# strips them from the code.  Note this only strips the logs, if you
# are building a complex message to log prior to invoking the log method
# you should still wrap with if (BuildConfig.DEBUG)
# --------------------------------------------------------------------
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# App compat v7 library rules. Search view is accessed indirectly so need to
# keep it explicitly.
-keep class android.support.v7.widget.SearchView { *; }

# Picasso rules. Has references to okhttp, which is not being used.
-dontwarn com.squareup.okhttp.**