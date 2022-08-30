package de.adesso.authentication.host.network

import android.app.Service
import android.content.Intent
import android.os.IBinder

import android.os.Binder
import android.util.Log
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



    fun connectToClient() {
        try{
            executorService.execute{
                clientSocket!!.keepAlive = true
                clientSocket!!.connect(InetSocketAddress("192.168.0.51", 8090!!), SOCKET_TIMEOUT)
                Log.d(TAG, "Client connected socket - " + clientSocket!!.isConnected)
                outputStream = DataOutputStream(clientSocket!!.getOutputStream())
                outputStream!!.writeUTF("helloWorldAtTime: ${System.currentTimeMillis()}")
                Log.i(TAG, "Sent!")
            }
        } catch (e: IOException) {
            Log.e(TAG, e.message!!)
        }
    }

    fun sendString(toSend: String?) {
        try{
            executorService.execute{
                Log.d(TAG, "sending string: $toSend")
                outputStream = DataOutputStream(clientSocket!!.getOutputStream())
                outputStream!!.writeUTF(toSend)
                Log.i(TAG, "Sent!")
            }
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
        fun getService() : NetworkingService {
            return this@NetworkingService
        }
    }
}