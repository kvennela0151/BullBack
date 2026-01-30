package com.example.bullback.data.model.websocket

import android.util.Log
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object AppWebSocketManager {

    private var webSocket: WebSocket? = null
    private var isConnected = false

    private var marketListener: ((JSONObject) -> Unit)? = null

    var currentTab: String = "indices"  // default tab

    fun sendMessage(message: String) {
        webSocket?.send(message)
    }


    fun setMarketListener(listener: ((JSONObject) -> Unit)?) {
        marketListener = listener
    }

    fun connect(token: String) {
        if (isConnected) return

        val client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()

        val request = Request.Builder()
            .url("wss://bullback.xtention.in/api/v1/live/ws?token=$token")
            .build()

        webSocket = client.newWebSocket(request, socketListener)
        isConnected = true
    }

    fun disconnect() {
        webSocket?.close(1000, "App closed")
        webSocket = null
        isConnected = false
    }

    private val socketListener = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d("AppWS", "WebSocket Connected")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d("AppWS", "Message: $text")
            try {
                val json = JSONObject(text)
                marketListener?.invoke(json)
            } catch (e: Exception) {
                Log.e("AppWS", "JSON error", e)
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e("AppWS", "WebSocket failure", t)
            isConnected = false
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d("AppWS", "WebSocket closed")
            isConnected = false
        }
    }
}
