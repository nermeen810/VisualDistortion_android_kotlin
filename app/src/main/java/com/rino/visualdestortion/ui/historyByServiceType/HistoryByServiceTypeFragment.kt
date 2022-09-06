package com.rino.visualdestortion.ui.historyByServiceType

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.core.view.isGone
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.rino.visualdestortion.R
import com.rino.visualdestortion.databinding.FragmentHistoryByServiceTypeBinding
import com.rino.visualdestortion.model.pojo.history.HistoryByServiceIdResponse
import com.rino.visualdestortion.model.pojo.history.SearchRequest
import com.rino.visualdestortion.model.pojo.history.ServiceData
import com.rino.visualdestortion.ui.AddService.StreetAdapter
import com.rino.visualdestortion.ui.home.MainActivity
import com.rino.visualdestortion.utils.Constants
import com.rino.visualdestortion.utils.NetworkConnection


class HistoryByServiceTypeFragment : Fragment() {
    private lateinit var viewModel: HistoryByServiceViewModel
    private lateinit var binding: FragmentHistoryByServiceTypeBinding
    private lateinit var historyAdapter: HistoryByServiceAdapter
    private lateinit var historyList: ArrayList<ServiceData>
    private lateinit var historyByServiceIdResponse: HistoryByServiceIdResponse
    private var selectedPeriod = ""
    private var serviceId = 1
    private var page = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            serviceId = arguments?.get("serviceID").toString().toInt()
            selectedPeriod = arguments?.get("period").toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryByServiceTypeBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    private fun init() {
        binding.shimmer.startShimmer()
        binding.historyRecycle.visibility = View.GONE
        viewModel = HistoryByServiceViewModel(requireActivity().application)
        historyList = arrayListOf()
        historyAdapter = HistoryByServiceAdapter(historyList, viewModel, requireContext())
        historyAdapter.updateItems(historyList)
        setUpUI()
        observeData()
        historyAdapter.updateItems(emptyList())

    }

    private fun observeData() {
        observeHistoryData()
        observeSearchHistoryData()
        observeNavToService()
        observeLoading()
        observeShowError()
    }


    private fun observeHistoryData() {
        viewModel.getHistoryData.observe(viewLifecycleOwner) {
            it?.let {
                historyByServiceIdResponse = it
                Log.e("hasNextPage", it.hasNextPage.toString())
                Log.e("totalPages", it.totalPages.toString())
                it.data.let { it1 ->
                    historyAdapter.updateItems(it1)
                    historyList = it1
                }
                binding.shimmer.stopShimmer()
                binding.shimmer.visibility = View.GONE
                binding.historyRecycle.visibility = View.VISIBLE
                binding.animationView.visibility = View.GONE
                binding.textNoData.visibility = View.GONE
            }
        }
    }

    private fun observeSearchHistoryData() {
        viewModel.getSearchHistoryData.observe(viewLifecycleOwner) {
            it?.let {
                historyAdapter.clearList()
                historyAdapter.updateItems(arrayListOf(ServiceData(it)))
                historyList = arrayListOf(ServiceData(it))
                binding.shimmer.stopShimmer()
                binding.shimmer.visibility = View.GONE
                binding.historyRecycle.visibility = View.VISIBLE
                binding.animationView.visibility = View.GONE
                binding.textNoData.visibility = View.GONE
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
        viewModel.navToTaskDetails.observe(viewLifecycleOwner) {
            it?.let {
                navToServiceDetails(it)
            }
        }
    }

    private fun navToServiceDetails(serviceData: ServiceData) {
        val action =
            HistoryByServiceTypeFragmentDirections.actionHistoryByIDToServiceDetails(serviceData)
        findNavController().navigate(action)
    }

    private fun observeShowError() {
        viewModel.setError.observe(viewLifecycleOwner) {
            it?.let {
                if (it.equals("Bad Request")) {
                    binding.shimmer.stopShimmer()
                    binding.shimmer.visibility = View.GONE
                    binding.historyRecycle.visibility = View.GONE
                    binding.animationView.visibility = View.VISIBLE
                    binding.textNoData.visibility = View.VISIBLE
                } else {
                    Snackbar.make(requireView(), it, Snackbar.LENGTH_INDEFINITE)
                        .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).setBackgroundTint(
                            resources.getColor(
                                R.color.teal
                            )
                        )
                        .setActionTextColor(resources.getColor(R.color.white)).setAction("Ok")
                        {
                        }.show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).bottomNavigation.isGone = true
        registerConnectivityNetworkMonitor()
    }

    private fun setUpUI() {
        //     binding.mSearch.setQueryHint(getString(R.string.search_hint));
        binding.historyRecycle.visibility = View.VISIBLE
        binding.historyRecycle.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }
        binding.serviceTitle.text = Constants.getServaceNameAr(serviceId)
        binding.periodTxt.text = Constants.convertNumsToArabic(selectedPeriod)
        binding.mSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    val taskNum = Constants.convertNumsToEnglish(query).toInt()
                    viewModel.searchHistoryDataByService(
                        SearchRequest(
                            taskNum,
                            serviceId,
                            selectedPeriod
                        )
                    )
                }
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {

                return false
            }
        })
        //      setPeriodTimeMenuItems()
        setupPagination()

    }


    private fun setupPagination() {
        binding.idNestedSV.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            // on scroll change we are checking when users scroll as bottom.
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                // in this method we are incrementing page number,
                // making progress bar visible and calling get data method.
                Log.e("historyByServiceIdResponse", historyByServiceIdResponse.toString())
                if (historyByServiceIdResponse.hasNextPage == true) {
                    page++
                    Log.e("pageNermeen", page.toString())
                    Log.e("hasNext", historyByServiceIdResponse.hasNextPage.toString())
                    viewModel.viewLoading(View.VISIBLE)
                    if (NetworkConnection.checkInternetConnection(requireContext())) {
                        viewModel.getHistoryData(serviceId, page, selectedPeriod)
                    } else {
                        viewModel.viewLoading(View.GONE)
                        showMessage()
                    }
                } else {
                    viewModel.viewLoading(View.GONE)
                }
            }
        })
    }


    private fun showMessage() {
        lifecycleScope.launchWhenResumed {
            Snackbar.make(
                requireView(),
                getString(R.string.no_internet),
                Snackbar.LENGTH_INDEFINITE
            )
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
                            historyAdapter.clearList()
                            viewModel.getHistoryData(serviceId, 1, selectedPeriod)
                        }
                    }
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    if (activity != null) {
                        activity!!.runOnUiThread {
                            showMessage()
                            binding.noInternetLayout.visibility = View.VISIBLE
                            binding.linearLayout.visibility = View.GONE

                        }
                    }
                }
            }
        )
    }

    private fun checkNetwork(id: Int, page: Int = 1) {
        lifecycleScope.launchWhenResumed {
            if (NetworkConnection.checkInternetConnection(requireContext())) {
                viewModel.getHistoryData(id, page)
            } else {
                binding.textNoInternet.visibility = View.VISIBLE
                binding.noNetworkResult.visibility = View.VISIBLE
                binding.linearLayout.visibility = View.GONE

            }
        }
    }

}