package de.adesso.authentication.client.network

import android.app.Service
import android.content.Context
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
import java.lang.ref.WeakReference
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class NetworkingService : Service() {

    private val myBinder = MyLocalBinder()
    private var clientSocket: ServerSocket? = null
    private var hostSocket: Socket? = null
    private var inputStream: DataInputStream? = null
    private var outPutStream: DataOutputStream? = null
    private val contextWeakReference: WeakReference<Context>? = null
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)
    private val TAG = "WIFI"

    fun waitForHost() {
        Log.i(TAG, "Waiting on Host")
        var handler = Handler(Looper.getMainLooper())
        executorService.execute(kotlinx.coroutines.Runnable {
            kotlin.run {
                try {
                    clientSocket = ServerSocket(8090)
                    hostSocket = clientSocket!!.accept()
                    hostSocket!!.keepAlive = true
                    inputStream = DataInputStream(hostSocket!!.getInputStream())
                    Log.i(TAG, "Received first: ${inputStream!!.readUTF()}")
                } catch (e: IOException) {
                    Log.e(TAG, Objects.requireNonNull(e.message)!!)
                }
                listenOnHost(handler)
            }
        })
    }

    private fun listenOnHost(handler: Handler) {
        Log.i(TAG, "Is connected: ${hostSocket?.isConnected}")
        executorService.execute(kotlinx.coroutines.Runnable {
            kotlin.run {
                var received: String? = null
                try {
                    while (true) {
                        received = inputStream?.readUTF()
                        handler.post {
                            //TODO make auth request
                            Log.i(TAG, "Received: ${received!!}")
                        }
                    }
                } catch (e: IOException) {
                    Log.e(TAG, Objects.requireNonNull(e.message)!!)
                }
            }
        })
    }

    fun sendString(toSend: String?) {
        try {
            Log.d(TAG, "Sending String $toSend")
            outPutStream = DataOutputStream(hostSocket!!.getOutputStream())
            outPutStream!!.writeUTF(toSend)
            Toast.makeText(this, "Sent!", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Log.e(TAG, e.message!!)
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return myBinder
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    inner class MyLocalBinder : Binder() {
        fun getService(): NetworkingService {
            return this@NetworkingService
        }
    }
}