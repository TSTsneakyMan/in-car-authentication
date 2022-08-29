package de.adesso.authentication.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import de.adesso.authentication.client.databinding.FragmentFirstBinding
import de.adesso.authentication.client.network.P2PService

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    var isBound = false
    private var _binding: FragmentFirstBinding? = null
    var p2pService: P2PService? = null

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

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_DrivingViewFragment)
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        p2pService = null
        isBound = false
    }
}