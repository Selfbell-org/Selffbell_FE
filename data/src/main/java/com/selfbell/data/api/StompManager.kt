package com.selfbell.data.api

import android.content.Context
import android.util.Log
import com.selfbell.data.R
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.disposables.CompositeDisposable
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StompManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var stompClient: StompClient? = null
    private val compositeDisposable = CompositeDisposable()

    fun connect(token: String, sessionId: Long) {
        val headers = mapOf("Authorization" to "Bearer $token")

        val endpoint = context.getString(R.string.websocket_endpoint)
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, endpoint, headers)

        val lifecycleDisposable = stompClient?.lifecycle()?.subscribe { lifecycleEvent ->
            when (lifecycleEvent.type) {
                LifecycleEvent.Type.OPENED -> {
                    Log.d("StompManager", "Stomp connection opened")
                    subscribeToTopic(sessionId)
                }
                LifecycleEvent.Type.ERROR -> Log.e("StompManager", "Error", lifecycleEvent.exception)
                LifecycleEvent.Type.CLOSED -> Log.d("StompManager", "Stomp connection closed")
                LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> Log.w("StompManager", "Failed server heartbeat")
            }
        }
        if (lifecycleDisposable != null) {
            compositeDisposable.add(lifecycleDisposable)
        }

        stompClient?.connect()
    }

    fun subscribeToTopic(sessionId: Long) {
        val topicDisposable = stompClient?.topic("/topic/safe-walk/$sessionId")?.subscribe({ stompMessage ->
            Log.d("StompManager", "Received: ${stompMessage.payload}")
        }, { error ->
            Log.e("StompManager", "Subscription error", error)
        })

        if (topicDisposable != null) {
            compositeDisposable.add(topicDisposable)
        }
    }

    fun sendLocation(sessionId: Long, lat: Double, lon: Double) {
        val capturedAt = java.time.OffsetDateTime.now().toString()
        val payload = """
            { "type":"TRACK", "lat":$lat, "lon":$lon, "capturedAt":"$capturedAt" }
        """.trimIndent()

        val sendDisposable = stompClient?.send("/app/safe-walks/$sessionId/track", payload)
            ?.subscribe({
                Log.d("StompManager", "Message sent successfully")
            }, { error ->
                Log.e("StompManager", "Failed to send message", error)
            })

        if (sendDisposable != null) {
            compositeDisposable.add(sendDisposable)
        }
    }

    fun disconnect() {
        compositeDisposable.clear()
        stompClient?.disconnect()
        stompClient = null
    }
}
