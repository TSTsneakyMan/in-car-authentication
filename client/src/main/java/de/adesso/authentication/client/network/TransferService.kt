package de.adesso.authentication.client.network

import android.app.IntentService
import android.content.Intent
import android.content.Context
import android.util.Log
import java.io.IOException
import java.util.*
import android.widget.Toast

import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.Socket


// TODO: Rename actions, choose action names that describe tasks that this
// IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
private const val ACTION_FOO = "de.adesso.authentication.client.network.action.FOO"
private const val ACTION_BAZ = "de.adesso.authentication.client.network.action.BAZ"

// TODO: Rename parameters
private const val EXTRA_PARAM1 = "de.adesso.authentication.client.network.extra.PARAM1"
private const val EXTRA_PARAM2 = "de.adesso.authentication.client.network.extra.PARAM2"

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.

 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.

 */
class TransferService : IntentService("TransferService") {

    private val TAG = "WIFI_DIRECT"

    private val SOCKET_TIMEOUT = 5000
    private val ACTION_SEND_STRING = "sendString"
    private val EXTRAS_GROUP_OWNER_ADDRESS = "go_host"
    private val EXTRAS_GROUP_OWNER_PORT = "go_port"

    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_SEND_STRING -> {
                val host = intent.getStringExtra(EXTRAS_GROUP_OWNER_ADDRESS)
                val port = intent.getStringExtra(EXTRAS_GROUP_OWNER_PORT)
                handleActionSendString(host, port)
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionSendString(host: String?, port: String?) {
        var socket: Socket? = null
        var stream: DataOutputStream? = null
        try{
            socket = Socket()
            socket.bind(null)
            socket.connect(InetSocketAddress(host, Integer.parseInt(port)), SOCKET_TIMEOUT)
            Log.d(TAG, "Client connected socket - " + socket.isConnected)
            stream = DataOutputStream(socket.getOutputStream())
            stream.writeUTF("text")
            stream.close()
            Toast.makeText(this@TransferService, "Sent!", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Log.e(TAG, e.message!!)
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (e: IOException) {
                    e.printStackTrace();
                }
            }
            if (socket != null) {
                if (socket.isConnected()) {
                    try {
                        socket.close();
                    } catch (e: IOException) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    companion object {
        /**
         * Starts this service to perform action Foo with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        // TODO: Customize helper method
        @JvmStatic
        fun startActionFoo(context: Context, param1: String, param2: String) {
            val intent = Intent(context, TransferService::class.java).apply {
                action = ACTION_FOO
                putExtra(EXTRA_PARAM1, param1)
                putExtra(EXTRA_PARAM2, param2)
            }
            context.startService(intent)
        }
    }
}