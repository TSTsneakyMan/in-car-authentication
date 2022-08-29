package de.adesso.authentication.client

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pManager
import android.os.Binder
import android.os.IBinder

class P2PService : Service() {

    private val intentFilter = IntentFilter()

    private val myBinder = MyLocalBinder()

    override fun onBind(intent: Intent): IBinder {
        // Indicates a change in the Wi-Fi Direct status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)

        // Indicates the state of Wi-Fi Direct connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        return myBinder
    }

    inner class MyLocalBinder : Binder() {
        fun getService() : P2PService {
            return this@P2PService
        }

    }
}