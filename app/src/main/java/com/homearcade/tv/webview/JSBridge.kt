package com.homearcade.tv.webview

import android.content.Context
import android.webkit.JavascriptInterface
import android.widget.Toast

class JSBridge(private val context: Context) {

    @JavascriptInterface
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun getAppVersion(): String = "1.0.0"

    @JavascriptInterface
    fun isTV(): Boolean = true

    @JavascriptInterface
    fun exitApp() {
        val activity = context as? android.app.Activity
        activity?.finish()
    }
}
