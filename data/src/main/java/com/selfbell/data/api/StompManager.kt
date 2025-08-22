package com.selfbell.data.api

import android.util.Log
import io.reactivex.disposables.CompositeDisposable
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent

class StompManager {
    private var stompClient: StompClient? = null
    // RxJava 구독을 관리하기 위한 CompositeDisposable
    private val compositeDisposable = CompositeDisposable()

    // 1. 웹소켓 연결
    fun connect(token: String, sessionId: Long) {
        // 1. 헤더 타입을 List -> Map으로 수정
        val headers = mapOf("Authorization" to "Bearer $token")

        // 2. 엔드포인트 URL 형식을 올바르게 수정
        // SockJS 지원 엔드포인트는 /ws/websocket 경로를 사용합니다.
        val endpoint = "ws://3.37.244.247:8080/ws/websocket"
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, endpoint, headers)

        // 연결 라이프사이클 이벤트 로그 추가 (디버깅에 유용)
        val lifecycleDisposable = stompClient?.lifecycle()?.subscribe { lifecycleEvent ->
            when (lifecycleEvent.type) {
                LifecycleEvent.Type.OPENED -> {
                    Log.d("StompManager", "Stomp connection opened")
                    // 연결이 열리면 토픽 구독 시작
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

        // 연결 시도
        stompClient?.connect()
    }

    // 2. 토픽 구독 (보호자용)
    fun subscribeToTopic(sessionId: Long) {
        val topicDisposable = stompClient?.topic("/topic/safe-walk/$sessionId")?.subscribe({ stompMessage ->
            // 위치 정보(stompMessage.payload)를 받아서 처리하는 로직
            Log.d("StompManager", "Received: ${stompMessage.payload}")
            // 예: val location = Gson().fromJson(stompMessage.payload, Location::class.java)
            // viewModel.updateLocation(location)
        }, { error ->
            Log.e("StompManager", "Subscription error", error)
        })

        if (topicDisposable != null) {
            compositeDisposable.add(topicDisposable)
        }
    }

    // 3. 메시지 발행 (피보호자용)
    fun sendLocation(sessionId: Long, lat: Double, lon: Double) {
        // capturedAt은 실제 시간을 보내는 것이 좋습니다. (예: ISO 8601 형식)
        val capturedAt = java.time.OffsetDateTime.now().toString()
        val payload = """
            { "type":"TRACK", "lat":$lat, "lon":$lon, "capturedAt":"$capturedAt" }
        """.trimIndent()

        // 메시지를 보낼 서버의 목적지 경로 (API 명세 확인 필요)
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

    // 4. 연결 해제 및 리소스 정리
    fun disconnect() {
        // 모든 구독을 한번에 해제하여 메모리 누수 방지
        compositeDisposable.clear()
        stompClient?.disconnect()
        stompClient = null
    }
}
