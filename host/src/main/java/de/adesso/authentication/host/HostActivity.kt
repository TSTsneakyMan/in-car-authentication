package de.adesso.authentication.host

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.adesso.authentication.host.network.NetworkingService


class HostActivity : AppCompatActivity() {

    private var networkingService: NetworkingService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName,
                                        service: IBinder
        ) {
            val binder = service as NetworkingService.MyLocalBinder
            networkingService = binder.getService()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host)
    }

    fun onConnectClicked(view: android.view.View) {
        Toast.makeText(this, "Looking for device on network. Please make sure the client app is opened and searching.", Toast.LENGTH_LONG).show()
        //TODO: Send to all in network
        networkingService?.connectToClient()
        //TODO: Wait for responses
    }

    fun onSendClicked(view: android.view.View) {
        Toast.makeText(this, "Sending Authenticationrequest to Client App.", Toast.LENGTH_LONG).show()
        networkingService?.sendString("Authenticate pls")
    }

    override fun onStart() {
        super.onStart()
        Intent(this, NetworkingService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
    }
}