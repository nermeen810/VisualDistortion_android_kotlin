package com.rino.visualdestortion.ui.history

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
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.rino.visualdestortion.R
import com.rino.visualdestortion.databinding.FragmentHistoryBinding
import com.rino.visualdestortion.model.pojo.history.Data
import com.rino.visualdestortion.model.pojo.history.ServiceTypeData
import com.rino.visualdestortion.ui.home.MainActivity
import com.rino.visualdestortion.utils.NetworkConnection


class HistoryFragment : Fragment() {
    private lateinit var viewModel: HistoryViewModel
    private lateinit var binding: FragmentHistoryBinding
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var historyList: List<ServiceTypeData>

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
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    private fun init() {
        binding.shimmer.startShimmer()
        binding.historyRecycle.visibility = View.GONE
        viewModel = HistoryViewModel(requireActivity().application)
        historyAdapter = HistoryAdapter(arrayListOf(), viewModel)
        setUpUI()
        observeData()
        historyAdapter.updateItems(emptyList())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        checkNetwork()
//        registerConnectivityNetworkMonitor()
    }

    private fun observeData() {
        observeHistoryData()
        observeNavToService()
        observeLoading()
        observeShowError()
    }

    private fun observeHistoryData() {
        //   viewModel.getHistoryData()
        viewModel.getHistoryData.observe(viewLifecycleOwner) {
            it?.let {
                it.data?.let { it1 ->
                    historyAdapter.updateItems(it1)
                    historyList = it1
                }
                binding.shimmer.stopShimmer()
                binding.shimmer.visibility = View.GONE
                binding.historyRecycle.visibility = View.VISIBLE
                binding.animationView.visibility = View.GONE

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

    private fun observeNavToService() {
        viewModel.navToHistoryOfTask.observe(viewLifecycleOwner) {
            it?.let {
                navToHistoryById(it)
            }
        }
    }

    private fun navToHistoryById(id: Int) {
        val action = HistoryFragmentDirections.actionHistoryToHistoryByID(id.toString())
        findNavController().navigate(action)
    }

    private fun observeShowError() {
        viewModel.setError.observe(viewLifecycleOwner) {
            it?.let {
//                Snackbar.make(requireView(), it, Snackbar.LENGTH_INDEFINITE)
//                    .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).setBackgroundTint(
//                        resources.getColor(
//                            R.color.teal
//                        )
//                    )
//                    .setActionTextColor(resources.getColor(R.color.white)).setAction("Ok")
//                    {
//                    }.show()
                binding.shimmer.stopShimmer()
                binding.shimmer.visibility = View.GONE
                binding.historyRecycle.visibility = View.GONE
                binding.animationView.visibility = View.VISIBLE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).bottomNavigation.isGone = false
        checkNetwork()
        registerConnectivityNetworkMonitor()
    }

    private fun setUpUI() {
        binding.historyRecycle.visibility = View.VISIBLE
        binding.historyRecycle.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
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
                .setAction(getString(R.string.dismiss))
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
                                binding.noInternetLayout.visibility = View.GONE
                                binding.linearLayout.visibility = View.VISIBLE
                                viewModel.getHistoryData()
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
                                showMessage(getString(R.string.no_internet))
                            }
                        }
                    }
                }
            )
        }
    }

    private fun checkNetwork() {
        if (NetworkConnection.checkInternetConnection(requireContext())) {
            viewModel.getHistoryData()
        } else {
            showMessage(getString(R.string.no_internet))
            binding.noInternetLayout.visibility = View.VISIBLE
            binding.linearLayout.visibility = View.GONE

        }
    }

}