package de.adesso.authentication.client

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import de.adesso.authentication.client.databinding.FragmentConnectionBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ConnectionFragment : Fragment() {

    private var _binding: FragmentConnectionBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentConnectionBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonConnect.setOnClickListener {
            val mainActivity = activity as MainActivity
            if (mainActivity.getHostConnection() == true) Toast.makeText(requireContext(), getString(R.string.ConnectionAlreadyEstablished), Toast.LENGTH_SHORT).show()
            else {
                if (mainActivity.getClientSocketStatus() == true) Toast.makeText(requireContext(), "Already waiting for Host", Toast.LENGTH_SHORT).show()
                else {
                    Toast.makeText(requireContext(), "Waiting for Host now", Toast.LENGTH_SHORT).show()
                    mainActivity.waitForHost()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}