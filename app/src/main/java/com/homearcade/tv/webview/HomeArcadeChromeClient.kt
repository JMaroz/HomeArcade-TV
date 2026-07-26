package com.homearcade.tv.webview

import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView

class HomeArcadeChromeClient(
    private val onFullscreenChange: (Boolean) -> Unit = {},
    private val onProgress: (Int) -> Unit = {}
) : WebChromeClient() {

    private var customView: View? = null
    private var customViewCallback: CustomViewCallback? = null

    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
        if (customView != null) {
            callback?.onCustomViewHidden()
            return
        }
        customView = view
        customViewCallback = callback
        onFullscreenChange(true)
    }

    override fun onHideCustomView() {
        customView?.let { view ->
            customViewCallback?.onCustomViewHidden()
            customView = null
            customViewCallback = null
        }
        onFullscreenChange(false)
    }

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        onProgress(newProgress)
    }
}
