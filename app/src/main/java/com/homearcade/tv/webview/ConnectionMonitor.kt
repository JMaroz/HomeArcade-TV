package com.homearcade.tv.webview

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class ConnectionMonitor(
    private val host: String,
    private val port: String,
    private val onOnline: () -> Unit = {},
    private val onOffline: () -> Unit = {}
) {
    private var isRunning = false
    private val handler = Handler(Looper.getMainLooper())
    private var wasOnline = true
    private val checkIntervalMs = 15_000L

    fun start() {
        if (isRunning) return
        isRunning = true
        scheduleCheck()
    }

    fun stop() {
        isRunning = false
        handler.removeCallbacksAndMessages(null)
    }

    private fun scheduleCheck() {
        if (!isRunning) return
        handler.postDelayed({ doCheck() }, checkIntervalMs)
    }

    private fun doCheck() {
        Thread {
            val online = try {
                val url = URL("http://$host:$port/api/health")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 3000
                conn.readTimeout = 3000
                val code = conn.responseCode
                conn.disconnect()
                code == 200
            } catch (e: Exception) {
                false
            }

            handler.post {
                if (online != wasOnline) {
                    wasOnline = online
                    if (online) onOnline() else onOffline()
                }
                scheduleCheck()
            }
        }.start()
    }
}
