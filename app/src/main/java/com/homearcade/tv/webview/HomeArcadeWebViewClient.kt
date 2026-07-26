package com.homearcade.tv.webview

import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import java.net.URI

class HomeArcadeWebViewClient(
    private val onPageStarted: () -> Unit = {},
    private val onPageFinished: () -> Unit = {},
    private val onError: (String) -> Unit = {}
) : WebViewClient() {

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        onPageStarted()
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        injectTVSupportJS(view)
        onPageFinished()
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: android.webkit.WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        if (request?.isForMainFrame == true) {
            onError(error?.description?.toString() ?: "Unknown error")
        }
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url?.toString() ?: return false
        val scheme = URI(url).scheme ?: return false
        if (scheme == "http" || scheme == "https") {
            return false
        }
        return true
    }

    private fun injectTVSupportJS(view: WebView?) {
        view ?: return
        val js = """
            (function() {
                if (window.__tvBridgeLoaded) return;
                window.__tvBridgeLoaded = true;

                window.__tvKeyHandler = function(keyCode) {
                    var e = new KeyboardEvent('keydown', {
                        key: window.__tvKeyCodeToKey(keyCode) || 'Unknown',
                        keyCode: keyCode,
                        which: keyCode,
                        bubbles: true,
                        cancelable: true
                    });
                    document.activeElement.dispatchEvent(e);

                    var ce = new CustomEvent('tvkey', {
                        detail: { keyCode: keyCode }
                    });
                    document.dispatchEvent(ce);
                };

                window.__tvKeyCodeToKey = function(kc) {
                    var map = {
                        19: 'ArrowUp', 20: 'ArrowDown', 21: 'ArrowLeft', 22: 'ArrowRight',
                        23: 'Enter', 66: 'Enter', 4: 'Back',
                        82: 'Menu', 85: 'MediaPlayPause',
                        96: 'a', 97: 'b', 99: 'x', 100: 'y',
                        102: 'GamepadButton1', 103: 'GamepadButton2'
                    };
                    return map[kc] || null;
                };

                console.log('[HomeArcadeTV] TV bridge loaded');
            })();
        """.trimIndent()
        view.evaluateJavascript(js, null)
    }
}
