package xyz.redtorch.common.web.socket

import org.springframework.web.socket.CloseStatus
import java.nio.ByteBuffer

interface WebSocketClientCallBack {
    @Throws(java.lang.Exception::class)
    fun afterConnectionEstablished()

    @Throws(Exception::class)
    fun handleTextMessage(message: String)


    @Throws(Exception::class)
    fun handleBinaryMessage(message: ByteBuffer)


    @Throws(Exception::class)
    fun handlePongMessage(delay: Long)


    @Throws(Exception::class)
    fun handleTransportError(exception: Throwable)


    @Throws(Exception::class)
    fun afterConnectionClosed(status: CloseStatus)

    fun handleAuthFailed()

    fun handleAuthSucceeded()
}