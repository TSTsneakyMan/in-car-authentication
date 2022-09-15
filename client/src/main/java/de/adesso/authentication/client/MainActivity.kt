package de.adesso.authentication.client

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import de.adesso.authentication.client.databinding.ActivityMainBinding
import de.adesso.authentication.client.network.NetworkingService
import java.util.concurrent.Executor


class MainActivity : AppCompatActivity() {

    final object Constants{
        //Constants
        final var AUTHENTICATIONBROADCASTINTENTACTION = "AuthenticationBroadCast"
        final var MESSAGE = "Message"
        final var AUTHENTICATINREQUEST = "AuthenticationRequest"
        final var DRIVINGVIEWREQUEST = "DrivingViewRequest"
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var networkingService: NetworkingService? = null
    private var isBound = false
    private var navController: NavController? = null

    // Lateinit for Biometric Manager etc.
    private lateinit var bm: BiometricManager
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    // Companion object gets initialized onStart()
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(
            className: ComponentName,
            service: IBinder
        ) {
            val binder = service as NetworkingService.MyLocalBinder
            networkingService = binder.getService()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isBound = false
        }
    }

    // handler for received Intents
    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            // Extract data included in the Intent
            val message = intent.getStringExtra(Constants.MESSAGE)
            Log.d("${Constants.AUTHENTICATIONBROADCASTINTENTACTION}receiver", "Got message: $message")
            if (message.equals(Constants.AUTHENTICATINREQUEST)) authenticate()
            if (message.equals(Constants.DRIVINGVIEWREQUEST)) switchToDrivingView()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Set xml files
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Init navController and appBarConfig
        navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController?.graph!!)
        setupActionBarWithNavController(navController!!, appBarConfiguration)

        // Initialize the Biometric Manager
        bm = BiometricManager.from(this@MainActivity)
        when (bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                Log.d(ContentValues.TAG, "App can authenticate using biometrics.")
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Log.e(ContentValues.TAG, "No biometric features available on this device.")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Log.e(ContentValues.TAG, "Biometric features are currently unavailable.")
        }
    }
    fun switchToDrivingView(){
        navController?.navigate(R.id.action_FirstFragment_to_DrivingViewFragment)
    }

    fun waitForHost(){
        // TODO: Scan for host here? Then request shared secret for encryption?
        networkingService?.waitForHost()
    }

    fun authenticate() {
        executor = ContextCompat.getMainExecutor(this@MainActivity)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                        this@MainActivity,
                        "Authentication error: $errString", Toast.LENGTH_SHORT
                    )
                        .show()
                    networkingService?.sendString("Authenticaton Error with code $errString")
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(
                        this@MainActivity,
                        "Authentication succeeded!", Toast.LENGTH_SHORT
                    )
                        .show()
                    networkingService?.sendString("Authentication Succeded")
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        this@MainActivity, "Authentication failed",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    networkingService?.sendString("Authentication failed")
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use account password")
            .build()

        // Consider integrating with the keystore to unlock cryptographic operations
        biometricPrompt.authenticate(promptInfo)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return when (item.itemId) {
            R.id.action_settings -> {
//                navController.navigate(R.id.settings)
                true
            }
            // Add ids with code here
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, NetworkingService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mMessageReceiver, IntentFilter(Constants.AUTHENTICATIONBROADCASTINTENTACTION));
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    fun getHostConnection(): Boolean? {
        return networkingService?.getHostConnection()
    }

    fun getClientSocketStatus(): Boolean? {
        return networkingService?.getClientSocketStatus()
    }
}