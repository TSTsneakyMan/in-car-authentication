package de.adesso.authentication.client

import android.content.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.adesso.authentication.client.databinding.FragmentDrivingViewBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class DrivingViewFragment : Fragment() {

    private var _binding: FragmentDrivingViewBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentDrivingViewBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_DrivingViewFragment_to_FirstFragment)
        }
        binding.buttonAuthenticate.setOnClickListener {
            //TODO: Ask the main activity to authenticate
            (activity as MainActivity).authenticate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}