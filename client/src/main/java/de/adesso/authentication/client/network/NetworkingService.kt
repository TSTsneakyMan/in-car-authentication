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
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.navigation.NavController
import de.adesso.authentication.client.MainActivity
import de.adesso.authentication.client.R
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.localbroadcastmanager.content.LocalBroadcastManager





class NetworkingService : Service() {

    private val myBinder = MyLocalBinder()
    private var clientSocket: ServerSocket? = null
    private var hostSocket: Socket? = null
    private var inputStream: DataInputStream? = null
    private var outPutStream: DataOutputStream? = null
    private val contextWeakReference: WeakReference<Context>? = null
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)
    private val TAG = "WIFI"
    var navController: NavController? = null
        set(value) {
            field = value
        }

    fun waitForHost() {
        Log.i(TAG, "Waiting on Host")
        var handler = Handler(Looper.getMainLooper())
        executorService.execute(kotlinx.coroutines.Runnable {
            kotlin.run {
                try {
                    clientSocket = ServerSocket(8090)
                    hostSocket = clientSocket?.accept()
                    hostSocket?.keepAlive = true
                    inputStream = DataInputStream(hostSocket?.getInputStream())
                    Log.i(TAG, "Received first: ${inputStream?.readUTF()}")
                } catch (e: IOException) {
                    Log.e(TAG, Objects.requireNonNull(e.message)!!)
                }
                // TODO: Moving this to its own thread sets the sockets to to NULL???
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
                    // Reading the input stream for the whole lifecycle of the thread
                    while (hostSocket!!.isConnected) {
                        received = inputStream?.readUTF()
                        Log.i(TAG, "Received: ${received!!}")
                        if (received.equals("AuthenticationRequest")) {
                            handler.post {
                                //TODO do this earlier
                                Log.i(TAG,"Switching to Driving View now!")
                                navController?.navigate(R.id.action_FirstFragment_to_DrivingViewFragment)
                                //TODO ask the Mainactivity to authenticate
                                val intent = Intent("Authentication")
                                intent.putExtra("message", "Authenticate pls")
                                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
//                                DrivingViewFragment().authenticate()
//                                requireContext().
                            }
                        }

                    }
                } catch (e: IOException) {
                    Log.e(TAG, Objects.requireNonNull(e.message)!!)
                }
            }
        })
    }

    fun sendString(toSend: String?) {
        executorService.execute(kotlinx.coroutines.Runnable {
            kotlin.run {
                try {
                    Log.d(TAG, "Sending String $toSend")
                    outPutStream = DataOutputStream(hostSocket!!.getOutputStream())
                    outPutStream!!.writeUTF(toSend)
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