package de.adesso.authentication.host.network

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class NetworkingService : Service() {

    private val myBinder = MyLocalBinder()
    private var clientSocket: Socket? = Socket()
    private var outputStream: DataOutputStream? = null
    private var inputStream: DataInputStream? = null
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)
    private val SOCKET_TIMEOUT = 5000
    private val TAG = "WIFI"

    fun getConnectionStatus(): Boolean? {
        return clientSocket?.isConnected
    }

    fun connectToClient() {
        var handler = Handler(Looper.getMainLooper())
        executorService.execute(kotlinx.coroutines.Runnable {
            kotlin.run {
                try {
                    clientSocket?.keepAlive = true
                    clientSocket?.connect(
                        InetSocketAddress("192.168.0.51", 8090),
                        SOCKET_TIMEOUT
                    )
                    Log.d(TAG, "Client connected socket - " + clientSocket?.isConnected)
                    outputStream = DataOutputStream(clientSocket?.getOutputStream())
                    inputStream = DataInputStream(clientSocket?.getInputStream())
                    outputStream?.writeUTF("helloWorldAtTime: ${System.currentTimeMillis()}")
                    Log.i(TAG, "Sent!")
                } catch (e: IOException) {
                    Log.e(TAG, e.message!!)
                }
                listenOnClient(handler)
            }
        })
    }

    private fun listenOnClient(handler: Handler) {
        Log.i(TAG, "Is connected: ${clientSocket?.isConnected}")
        executorService.execute(kotlinx.coroutines.Runnable {
            kotlin.run {
                var received: String?
                try {
                    // Reading the input stream for the whole lifecycle of the thread
                    while (clientSocket?.isConnected == true) {
                        received = inputStream?.readUTF()
                        Log.i(TAG, "Received: ${received!!}")
                        handler.post {
                            Toast.makeText(this@NetworkingService, "Client response: $received", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: IOException) {
                    Log.e(TAG, Objects.requireNonNull(e.message)!!)
                    clientSocket?.close()
                }
            }
        })
    }

    fun sendString(toSend: String?) {
        executorService.execute(kotlinx.coroutines.Runnable {
            kotlin.run {
                try {
                    if(clientSocket?.isConnected == true){
                        Log.d(TAG, "Trying to send string: $toSend")
                        outputStream = DataOutputStream(clientSocket!!.getOutputStream())
                        outputStream!!.writeUTF(toSend)
                    } else Log.e(TAG, "Client Socket not connected")
                } catch (e: IOException) {
                    Log.e(TAG, e.message!!)
                }
            }
        })
    }

    override fun onBind(intent: Intent): IBinder {
        return myBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    inner class MyLocalBinder : Binder() {
        fun getService(): NetworkingService {
            return this@NetworkingService
        }
    }
}