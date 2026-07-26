# WebView JavaScript interface
-keepclassmembers class com.homearcade.tv.webview.JSBridge {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep DataStore serialization
-keepclassmembers class com.homearcade.tv.data.ServerConfig {
    *;
}

# Keep ViewModel state classes
-keepclassmembers class com.homearcade.tv.viewmodel.** {
    *;
}

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
