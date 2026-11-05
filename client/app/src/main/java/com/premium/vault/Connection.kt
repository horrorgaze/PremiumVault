package com.premium.vault

import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.*
import java.util.concurrent.TimeUnit

class Connection(
    private val url: String,
    private val authToken: String = "",
    private val onStatus: (String) -> Unit = {}
) {
    companion object {
        private const val TAG = "Vault"
        private const val MAX_BACKOFF_MS = 30_000L

        // Only these exact strings may be executed. Extend deliberately.
        private val ALLOWED = setOf("ping", "status", "deviceinfo")
    }

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    private val mainHandler = Handler(Looper.getMainLooper())
    private var webSocket: WebSocket? = null
    private var attempt = 0
    private var closedByUs = false

    fun connect() {
        closedByUs = false

        if (!url.startsWith("wss://") || url.contains("your-server-url-here")) {
            onStatus("Not configured — set the wss:// URL")
            return
        }

        onStatus(if (attempt == 0) "Connecting…" else "Reconnecting (attempt $attempt)…")

        val request = Request.Builder()
            .url(url)
            .apply { if (authToken.isNotEmpty()) header("Authorization", "Bearer $authToken") }
            .build()

        webSocket = client.newWebSocket(request, listener)
    }

    private val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            attempt = 0
            Log.d(TAG, "Connected to relay")
            onStatus("Connected")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val cmd = text.trim().lowercase()
            Log.d(TAG, "Inbound: $cmd")
            val output = if (cmd in ALLOWED) {
                Commands.execute(cmd)
            } else {
                "Rejected: '$cmd' is not in the command whitelist"
            }
            webSocket.send(output)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.w(TAG, "Socket failure: ${t.message}")
            scheduleReconnect()
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "Socket closed: $code $reason")
            scheduleReconnect()
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(1000, null)
        }
    }

    private fun scheduleReconnect() {
        if (closedByUs) return
        attempt++
        val exponent = (attempt - 1).coerceIn(0, 5)
        val delayMs = minOf(MAX_BACKOFF_MS, 1000L * (1 shl exponent))
        onStatus("Disconnected — retrying in ${delayMs / 1000}s")
        mainHandler.postDelayed({ connect() }, delayMs)
    }

    fun close() {
        closedByUs = true
        mainHandler.removeCallbacksAndMessages(null)
        webSocket?.close(1000, "client shutting down")
        webSocket = null
    }
}
