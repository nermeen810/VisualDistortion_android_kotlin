package com.rino.visualdestortion.ui.services

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.rino.visualdestortion.R
import com.rino.visualdestortion.databinding.FragmentServicesBinding
import com.rino.visualdestortion.model.pojo.home.ServiceTypes
import com.rino.visualdestortion.ui.home.MainActivity
import com.rino.visualdestortion.ui.splash.SplashFragmentDirections
import com.rino.visualdestortion.utils.NetworkConnection


class ServicesFragment : Fragment() {
    private lateinit var viewModel: ServiceViewModel
    private lateinit var binding: FragmentServicesBinding
    private lateinit var serviceAdapter: ServiceAdapter
    private lateinit var servicesList: List<ServiceTypes>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            @SuppressLint("ResourceType")
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentServicesBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    private fun init() {
        viewModel = ServiceViewModel(requireActivity().application)
        serviceAdapter = ServiceAdapter(arrayListOf(), viewModel, requireActivity())
        setUpUI()
        checkNetwork()
        registerConnectivityNetworkMonitor()
        observeData()
        serviceAdapter.updateServices(emptyList())

    }

    private fun observeData() {
        observeNavToAddService()
        observeNavToDailyPreparation()
        observeService()
        observeLoading()
        observeShowError()
    }

    private fun observeService() {
        //  viewModel.getServicesData()
        viewModel.getServicesData.observe(viewLifecycleOwner) {
            it?.let {
                serviceAdapter.updateServices(it.serviceTypes)
                servicesList = it.serviceTypes
                binding.serviceRecycle.visibility = View.VISIBLE

            }
        }
    }

    private fun setUpUI() {
        binding.serviceRecycle.visibility = View.VISIBLE
        binding.serviceRecycle.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = serviceAdapter
        }
        binding.editDailyPrepImg.setOnClickListener {
            navigateToEditDailyPreparation()
        }

    }

    private fun navigateToEditDailyPreparation() {
        val action = ServicesFragmentDirections.actionServiceToEditDailyprepration()
        findNavController().navigate(action)
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).bottomNavigation.isGone = false
        (activity as MainActivity).bottomNavigation.show(1, false)
    }

    private fun observeNavToAddService() {
        viewModel.navToAddService.observe(viewLifecycleOwner) {
            it?.let {
                val action = ServicesFragmentDirections.actionServiceToAddService(
                    it.name,
                    it.id.toString()
                )
                findNavController().navigate(action)
            }
        }
    }

    private fun observeNavToDailyPreparation() {
        viewModel.navToDailyPreparation.observe(viewLifecycleOwner) {
            it?.let {
                val action = ServicesFragmentDirections.actionServiceToDailyprepration(
                    it.name,
                    it.id.toString()
                )
                findNavController().navigate(action)
            }
        }
    }

    private fun observeLoading() {
        viewModel.loading.observe(viewLifecycleOwner) {
            it?.let {
                binding.progress.visibility = it
            }
        }
    }

    private fun observeShowError() {
        viewModel.setError.observe(viewLifecycleOwner) {
            it?.let {
                showMessage(it)
            }
        }
    }

    private fun showMessage(msg: String) {
        lifecycleScope.launchWhenResumed {
            Snackbar.make(requireView(), msg, Snackbar.LENGTH_INDEFINITE)
                .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).setBackgroundTint(
                    resources.getColor(
                        R.color.teal
                    )
                )
                .setActionTextColor(resources.getColor(R.color.white))
                .setAction(getString(R.string.close))
                {
                }.show()
        }
    }

    private fun registerConnectivityNetworkMonitor() {
        if (requireContext() != null) {
            val connectivityManager =
                requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val builder = NetworkRequest.Builder()
            connectivityManager.registerNetworkCallback(builder.build(),
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        super.onAvailable(network)
                        if (activity != null) {
                            activity!!.runOnUiThread {
                                binding.textNoInternet.visibility = View.GONE
                                binding.noNetworkResult.visibility = View.GONE
                                binding.linearLayout.visibility = View.VISIBLE
                                viewModel.getServicesData()
                            }
                        }
                    }

                    override fun onLost(network: Network) {
                        super.onLost(network)
                        if (activity != null) {
                            activity!!.runOnUiThread {
//                                Toast.makeText(
//                                    context, "لا يوجد انترنت ",
//                                    Toast.LENGTH_SHORT
//                                ).show()
                                showMessage("لا يوجد انترنت")
                            }
                        }
                    }
                }
            )
        }
    }

    private fun checkNetwork() {
        if (NetworkConnection.checkInternetConnection(requireContext())) {
            viewModel.getServicesData()
        } else {
            binding.textNoInternet.visibility = View.VISIBLE
            binding.noNetworkResult.visibility = View.VISIBLE
            binding.linearLayout.visibility = View.GONE

        }
    }
}