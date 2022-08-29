package de.adesso.authentication.client.network

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.ActionListener
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.util.*
import android.content.ContentValues.TAG
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pInfo
import android.widget.Toast

class P2PService : Service() {

    private val intentFilter = IntentFilter()
    private val myBinder = MyLocalBinder()
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var manager: WifiP2pManager
    private var receiver: WiFiDirectBroadcastReceiver? = null

    private val peers = mutableListOf<WifiP2pDevice>()

    private val peerListListener = WifiP2pManager.PeerListListener { peerList ->
        val refreshedPeers = peerList.deviceList
        if (refreshedPeers != peers) {
            peers.clear()
            peers.addAll(refreshedPeers)

            // If an AdapterView is backed by this data, notify it
            // of the change. For instance, if you have a ListView of
            // available peers, trigger an update.
//            (listAdapter as WiFiPeerListAdapter).notifyDataSetChanged()

            // Perform any other updates needed based on the new list of
            // peers connected to the Wi-Fi P2P network.
        }

        if (peers.isEmpty()) {
            Log.d(TAG, "No devices found")
            return@PeerListListener
        }
    }

    private object listener: ActionListener {

        override fun onSuccess() {
            // Code for when the discovery initiation is successful goes here.
            // No services have actually been discovered yet, so this method
            // can often be left blank. Code for peer discovery goes in the
            // onReceive method, detailed below.
            Log.d(TAG, "Found peers")
        }

        override fun onFailure(reasonCode: Int) {
            // Code for when the discovery initiation fails goes here.
            // Alert the user that something went wrong.
            Log.e(TAG, "Failure while looking for peers")
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return myBinder
    }

    override fun onCreate() {
        super.onCreate()
        // Indicates a change in the Wi-Fi Direct status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)

        // Indicates the state of Wi-Fi Direct connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)

        manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(this, mainLooper, null)

        // register the BroadcastReceiver with the intent values to be matched
        receiver = WiFiDirectBroadcastReceiver(manager, channel, this, peerListListener)
        registerReceiver(receiver, intentFilter)

        // fill peer list
        refreshPeerList()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("MissingPermission")
    //TODO: Check for permissions
    fun refreshPeerList(){
        manager.discoverPeers(channel, listener)
    }

    @SuppressLint("MissingPermission")
    fun connect() {
        refreshPeerList()
        // Picking the first device found on the network.
        //TODO: Look for correct device
        val device = peers[0]

        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
            wps.setup = WpsInfo.PBC
        }

        Log.i(TAG, "device adress: " + device.deviceAddress + device.deviceName + device.primaryDeviceType)

        manager.connect(channel, config, object : WifiP2pManager.ActionListener {

            override fun onSuccess() {
                // WiFiDirectBroadcastReceiver notifies us. Ignore for now.
            }

            @SuppressLint("MissingPermission")
            override fun onFailure(reason: Int) {
                Toast.makeText(
                    this@P2PService,
                    "Connect failed. Retry.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    inner class MyLocalBinder : Binder() {
        fun getService() : P2PService {
            return this@P2PService
        }
    }
}