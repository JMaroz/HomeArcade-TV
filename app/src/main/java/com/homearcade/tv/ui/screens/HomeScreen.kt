package com.homearcade.tv.ui.screens

import android.annotation.SuppressLint
import android.view.KeyEvent
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.homearcade.tv.webview.ConnectionMonitor
import com.homearcade.tv.webview.HomeArcadeChromeClient
import com.homearcade.tv.webview.HomeArcadeWebViewClient
import com.homearcade.tv.webview.JSBridge
import com.homearcade.tv.webview.TVKeyBridge
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HomeScreen(
    host: String,
    port: String,
    onDisconnect: () -> Unit = {},
    onSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var progress by remember { mutableIntStateOf(0) }
    var showBanner by remember { mutableStateOf(true) }
    var isFullscreen by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isRetrying by remember { mutableStateOf(false) }
    var retryCountdown by remember { mutableIntStateOf(0) }

    val url = "http://$host:$port/"
    var webView by remember { mutableStateOf<WebView?>(null) }
    var keyBridge by remember { mutableStateOf<TVKeyBridge?>(null) }

    fun startRetry() {
        isRetrying = true
        retryCountdown = 10
    }

    val connectionMonitor = remember {
        ConnectionMonitor(
            host = host,
            port = port,
            onOnline = {
                hasError = false
                isRetrying = false
                webView?.reload()
            },
            onOffline = {
                hasError = true
                errorMessage = "Server at $host:$port is not responding."
                startRetry()
            }
        )
    }

    LaunchedEffect(isRetrying) {
        if (!isRetrying) return@LaunchedEffect
        while (retryCountdown > 0) {
            delay(1000)
            retryCountdown--
        }
        // After countdown, check again
        if (hasError) {
            // Manually trigger monitor check
            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                val online = try {
                    val url = java.net.URL("http://$host:$port/api/health")
                    val conn = url.openConnection() as java.net.HttpURLConnection
                    conn.connectTimeout = 3000
                    conn.readTimeout = 3000
                    conn.responseCode == 200
                } catch (e: Exception) { false }
                if (online) {
                    hasError = false
                    isRetrying = false
                    webView?.reload()
                } else {
                    startRetry()
                }
            }
        }
    }

    BackHandler(enabled = !hasError) {
        val wv = webView
        if (wv != null && wv.canGoBack()) {
            wv.goBack()
        } else {
            connectionMonitor.stop()
            onDisconnect()
        }
    }

    LaunchedEffect(showBanner) {
        if (!isLoading) {
            delay(3000)
            showBanner = false
        }
    }

    DisposableEffect(Unit) {
        connectionMonitor.start()
        onDispose { connectionMonitor.stop() }
    }

    if (isFullscreen) {
        HideSystemUI()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Black)
    ) {
        if (!hasError) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.mediaPlaybackRequiresUserGesture = false
                        settings.databaseEnabled = true
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = true
                        settings.builtInZoomControls = false
                        settings.displayZoomControls = false
                        settings.allowFileAccess = false
                        settings.allowContentAccess = false
                        settings.mixedContentMode =
                            android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                        addJavascriptInterface(JSBridge(ctx), "NativeBridge")

                        val chromeClient = HomeArcadeChromeClient(
                            onFullscreenChange = { full -> isFullscreen = full },
                            onProgress = { p -> progress = p }
                        )
                        val viewClient = HomeArcadeWebViewClient(
                            onPageStarted = { isLoading = true; showBanner = true },
                            onPageFinished = {
                                isLoading = false
                                showBanner = true
                                hasError = false
                            },
                            onError = { msg ->
                                hasError = true
                                errorMessage = msg
                            }
                        )

                        webChromeClient = chromeClient
                        webViewClient = viewClient
                        loadUrl(url)

                        webView = this
                        keyBridge = TVKeyBridge(this)

                        setOnKeyListener { _, keyCode, event ->
                            if (event.action == KeyEvent.ACTION_DOWN) {
                                keyBridge?.handleKeyEvent(event) ?: false
                            } else false
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Top connection banner
            AnimatedVisibility(
                visible = showBanner && !isFullscreen,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HomeArcade",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = "$host:$port",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Loading progress bar
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(top = if (showBanner) 56.dp else 0.dp)
                        .height(3.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress / 100f)
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        // Error overlay
        if (hasError) {
            ErrorOverlay(
                message = errorMessage,
                isRetrying = isRetrying,
                secondsUntilRetry = retryCountdown,
                onRetry = {
                    hasError = false
                    webView?.reload()
                },
                onChangeServer = {
                    connectionMonitor.stop()
                    onDisconnect()
                }
            )
        }
    }
}

@Composable
private fun HideSystemUI() {
    val activity = androidx.compose.ui.platform.LocalView.current.context
        as? android.app.Activity
    DisposableEffect(Unit) {
        activity?.window?.decorView?.systemUiVisibility = (
            android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        )
        onDispose {
            activity?.window?.decorView?.systemUiVisibility =
                android.view.View.SYSTEM_UI_FLAG_VISIBLE
        }
    }
}
