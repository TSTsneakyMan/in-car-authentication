package de.adesso.authentication.client

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.content.Intent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import de.adesso.authentication.client.databinding.FragmentDrivingViewBinding
import de.adesso.authentication.client.network.P2PService
import java.util.concurrent.Executor
import android.content.ContentValues.TAG

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class DrivingViewFragment : Fragment() {

    var isBound = false
    private var _binding: FragmentDrivingViewBinding? = null
    var p2pService: P2PService? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // Lateinit for Biometric Manager etc.
    private lateinit var bm: BiometricManager
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName,
                                        service: IBinder
        ) {
            val binder = service as P2PService.MyLocalBinder
            p2pService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isBound = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentDrivingViewBinding.inflate(inflater, container, false)

        // Initialize the Biometric Manager
        bm = BiometricManager.from(requireContext())
        when(bm.canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK)){
            BiometricManager.BIOMETRIC_SUCCESS ->
                Log.d(TAG, "App can authenticate using biometrics.")
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Log.e(TAG, "No biometric features available on this device.")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Log.e(TAG, "Biometric features are currently unavailable.")
            //TODO: Ask User to set up credentials
        }
        return binding.root
    }

//    override fun onStart() {
//        super.onStart()
//        requireActivity().bindService(
//            Intent(activity, P2PService::class.java),
//            connection,
//            Context.BIND_AUTO_CREATE
//        )
//    }

    override fun onResume() {
        super.onResume()
        requireActivity().bindService(
            Intent(activity, P2PService::class.java),
            connection,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun onPause() {
        super.onPause()
        requireActivity().unbindService(connection)
    }

//    override fun onStop() {
//        super.onStop()
//        requireActivity().unbindService(connection)
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_DrivingViewFragment_to_FirstFragment)
        }
        binding.buttonAuthenticate.setOnClickListener{
            authenticate()
        }
    }

    private fun authenticate() {
        executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                //TODO: Send information to the host on automotive device
                override fun onAuthenticationError(errorCode: Int,
                                                   errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(requireContext(),
                        "Authentication error: $errString", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(requireContext(),
                        "Authentication succeeded!", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(requireContext(), "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use account password")
            .build()

        // Prompt appears when user clicks "Log in".
        // Consider integrating with the keystore to unlock cryptographic operations,
        // if needed by your app.
        biometricPrompt.authenticate(promptInfo)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        p2pService = null
        isBound = false
    }
}