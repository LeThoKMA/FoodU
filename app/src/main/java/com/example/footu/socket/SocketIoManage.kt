package com.example.footu.socket

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter

object SocketIoManage {
    var mSocket: Socket? = null
    val defaultChannel = "land-channel-"

    init {
        mSocket = IO.socket("https://9d18-27-79-143-130.ngrok-free.app")

        val onConnect: Emitter.Listener =
            Emitter.Listener { Log.d("SocketIoManage", "connected...") }

        val onConnectError: Emitter.Listener =
            Emitter.Listener {
                val e = it[0] as Exception
                Log.d("SocketIoManage", e.toString())
            }

        val onConnectDis: Emitter.Listener =
            Emitter.Listener { Log.d("SocketIoManage", "Disconnect...") }
        mSocket?.on(Socket.EVENT_CONNECT, onConnect)
        mSocket?.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
        mSocket?.on(Socket.EVENT_DISCONNECT, onConnectDis)
        mSocket?.connect()
    }

    fun subcribe() {
        mSocket?.emit("bill")
    }

    fun unSubcribe(channel: String) {
        mSocket?.emit("unSubcribe_channel", defaultChannel + channel)
    }

    fun closeConnection() {
        mSocket?.disconnect()
    }

    fun rejectCall(channel: String) {
        mSocket?.emit("get_rejected_call", defaultChannel + channel, true)
    }
}
