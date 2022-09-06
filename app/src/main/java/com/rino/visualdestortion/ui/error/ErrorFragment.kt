package com.rino.visualdestortion.ui.error

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.rino.visualdestortion.R
import com.rino.visualdestortion.databinding.FragmentErrorBinding
import com.rino.visualdestortion.ui.home.MainActivity
import com.rino.visualdestortion.utils.NetworkConnection


class ErrorFragment : Fragment() {

    private lateinit var binding: FragmentErrorBinding
    private lateinit var viewModel: ErrorViewModel

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).bottomNavigation.isGone = true

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ErrorViewModel(requireActivity().application)
        binding = FragmentErrorBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    private fun init() {
        registerConnectivityNetworkMonitor()
        tryAgainOnClick()
        observeIsPrepared()

    }

    private fun tryAgainOnClick() {
        binding.textTryAgain.text = getString(R.string.try_again)
        binding.tryAgainImg.setOnClickListener {
            if (NetworkConnection.checkInternetConnection(requireContext())) {
                binding.textTryAgain.text = getString(R.string.connecting)
                viewModel.isTodayPrepared()
            } else {
                showMessage(getString(R.string.no_internet))
            }
        }
    }

    private fun showMessage(msg: String) {
        Snackbar.make(requireView(), msg, Snackbar.LENGTH_INDEFINITE)
            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).setBackgroundTint(
                resources.getColor(
                    R.color.teal
                )
            )
            .setActionTextColor(resources.getColor(R.color.white)).setAction(
                getString(
                    R.string.dismiss
                )
            )
            {
            }.show()
    }

    private fun observeIsPrepared() {
        viewModel.isPrepared.observe(viewLifecycleOwner) {
            if (!viewModel.isLogin()) {
                navToWelcome()
            } else {
                if (it) {
                    navToHome()
                } else {
                    navigateToDailyPreparation()
                }
            }
        }
    }

    private fun navToHome() {
        val action = ErrorFragmentDirections.actionErrorToServiceFragment()
        findNavController().navigate(action)
    }

    private fun navigateToDailyPreparation() {
        val action = ErrorFragmentDirections.actionErrorToDailyPreparation()
        findNavController().navigate(action)
    }

    private fun navToWelcome() {
        val action = ErrorFragmentDirections.actionErrorToWelcome()
        lifecycleScope.launchWhenResumed {
            findNavController().navigate(action)
        }
    }

    private fun registerConnectivityNetworkMonitor() {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val builder = NetworkRequest.Builder()
        connectivityManager.registerNetworkCallback(builder.build(),
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    binding.textTryAgain.text = getString(R.string.connecting)
                    if (activity != null) {
                        activity!!.runOnUiThread {
                            if (viewModel.isLogin()) {
                                viewModel.isTodayPrepared()
                            } else {
                                navToWelcome()
                            }
                        }
                    }
                }

                override fun onLost(network: Network) {
                    binding.textTryAgain.text = getString(R.string.try_again)
                    super.onLost(network)
                    if (activity != null) {
                        activity!!.runOnUiThread {
                            showMessage(getString(R.string.no_internet))
                        }
                    }
                }
            }
        )
    }
}