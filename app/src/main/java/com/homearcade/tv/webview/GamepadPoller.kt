package com.homearcade.tv.webview

import android.hardware.input.InputManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.webkit.WebView
import org.json.JSONArray
import org.json.JSONObject

class GamepadPoller(
    private val webView: WebView?,
    private val inputManager: InputManager
) {
    private var isPolling = false
    private val handler = Handler(Looper.getMainLooper())
    private val pollIntervalMs = 32L

    fun start() {
        if (isPolling) return
        isPolling = true
        poll()
    }

    fun stop() {
        isPolling = false
        handler.removeCallbacksAndMessages(null)
    }

    private fun poll() {
        if (!isPolling) return
        val wv = webView ?: return

        val gamepads = getConnectedGamepads()
        val gamepadArray = JSONArray()

        for (gp in gamepads) {
            val obj = JSONObject()
            obj.put("id", gp.descriptor)
            obj.put("index", gp.id)
            obj.put("connected", true)

            val axes = JSONObject()
            val buttons = JSONObject()

            for (i in 0 until gp.buttonCount) {
                buttons.put("b$i", false)
            }

            val js = """
                (function() {
                    if (!window.__tvGamepads) {
                        window.__tvGamepads = {};
                        if (navigator.getGamepads) {
                            var orig = navigator.getGamepads.bind(navigator);
                            navigator.getGamepads = function() {
                                var gps = orig();
                                var tvGp = window.__tvGamepads;
                                for (var k in tvGp) {
                                    if (gps[k]) {
                                        gps[k].connected = tvGp[k].connected;
                                        gps[k].buttons = tvGp[k].buttons;
                                        gps[k].axes = tvGp[k].axes;
                                    }
                                }
                                return gps;
                            };
                        }
                    }
                    window.__tvGamepads[${gp.id}] = {
                        connected: true,
                        id: ${JSONObject.quote(gp.descriptor)},
                        index: ${gp.id},
                        mapping: 'standard',
                        buttons: [],
                        axes: []
                    };
                })();
            """.trimIndent()
            wv.evaluateJavascript(js, null)
        }

        handler.postDelayed({ poll() }, pollIntervalMs)
    }

    private fun getConnectedGamepads(): List<InputDevice> {
        val ids = inputManager.inputDeviceIds
        return ids.mapNotNull { id ->
            val device = InputDevice.getDevice(id) ?: return@mapNotNull null
            val sources = device.sources
            if (sources and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD ||
                sources and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK
            ) {
                device
            } else null
        }
    }
}
