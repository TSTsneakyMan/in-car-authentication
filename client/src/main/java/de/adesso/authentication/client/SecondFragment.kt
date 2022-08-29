package de.adesso.authentication.client

import android.content.Context
import android.content.Context.BIOMETRIC_SERVICE
import android.content.Intent
import android.os.Bundle
import android.provider.Settings.ACTION_BIOMETRIC_ENROLL
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.fragment.findNavController
import de.adesso.authentication.client.databinding.FragmentSecondBinding
import java.util.concurrent.Executors.newSingleThreadExecutor


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)

        // Initialize the Biometric Manager
        var bm: BiometricManager = activity?.getSystemService(BIOMETRIC_SERVICE) as BiometricManager
        when(bm.canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK)){
            BiometricManager.BIOMETRIC_SUCCESS ->
                Log.d("MY_APP_TAG", "App can authenticate using biometrics.")
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Log.e("MY_APP_TAG", "No biometric features available on this device.")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.")
            //TODO: Ask User to set up credentials
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
        binding.buttonAuthenticate.setOnClickListener{
            authenticate()
        }
    }

    private fun authenticate() {
        TODO("Not yet implemented")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}