package com.rino.visualdestortion.ui.filteredHistory

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.core.view.isGone
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.rino.visualdestortion.R
import com.rino.visualdestortion.databinding.FragmentFilteredHistotyBinding
import com.rino.visualdestortion.databinding.FragmentHistoryByServiceTypeBinding
import com.rino.visualdestortion.model.pojo.history.*
import com.rino.visualdestortion.ui.historyByServiceType.HistoryByServiceAdapter
import com.rino.visualdestortion.ui.historyByServiceType.HistoryByServiceTypeFragmentDirections
import com.rino.visualdestortion.ui.historyByServiceType.HistoryByServiceViewModel
import com.rino.visualdestortion.ui.home.MainActivity
import com.rino.visualdestortion.utils.Constants
import com.rino.visualdestortion.utils.NetworkConnection


class FilteredHistotyFragment : Fragment() {
    private lateinit var viewModel: FilteredHistoryViewModel
    private lateinit var binding: FragmentFilteredHistotyBinding
    private lateinit var historyAdapter: FilteredHistoryAdapter
    private lateinit var historyList: ArrayList<Data>
    private lateinit var searchHistoryAdapter: SubItemFilteredHistoryAdapter
    private lateinit var periodAdapter: PeriodAdapter

    private lateinit var searchHistoryList: ArrayList<ServiceData>
    private lateinit var periodTimeList_ar: ArrayList<String>
    private lateinit var periodTimeList_en: ArrayList<String>
    private lateinit var historyByServiceIdResponse: FilteredHistoryResponse
    private val mainPeriod = "all"
    private var serviceId = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FilteredHistoryViewModel.lastSelectedPos = FilteredHistoryViewModel.periodTimeList_en.size -1
        arguments?.let {
            serviceId = arguments?.get("serviceID").toString().toInt()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFilteredHistotyBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    private fun init() {
        periodTimeList_ar = arrayListOf(" السنة السابقة "," السنة الحالية "," الشهر السابق "," الشهر الحالى "," الاسبوع السابق "," الاسبوع الحالى "," الكل ")
        periodTimeList_en = arrayListOf("lastyear","year","lastmonth","month","lastweek","week","all")
        binding.shimmer.startShimmer()
        binding.historyRecycle.visibility = View.GONE
        viewModel = FilteredHistoryViewModel(requireActivity().application)
        viewModel.serviceId = serviceId
        historyList = arrayListOf()
        searchHistoryList = arrayListOf()
        historyAdapter = FilteredHistoryAdapter(historyList, viewModel,requireContext())
        periodAdapter = PeriodAdapter(periodTimeList_ar,viewModel)
        historyAdapter.updateItems(historyList)
        searchHistoryAdapter = SubItemFilteredHistoryAdapter(searchHistoryList, viewModel,requireContext())
        searchHistoryAdapter.updateItems(searchHistoryList)
        setUpUI()
//        checkNetwork(serviceId)
        registerConnectivityNetworkMonitor()
        observeData()
        historyAdapter.updateItems(emptyList())
    }

    private fun observeData() {
        observeHistoryData()
        observeSearchHistoryData()
        observeNavToService()
        observeNavToServiceDetails()
        observeLoading()
        observeShowError()
    }


    private fun observeHistoryData() {
        //   viewModel.getHistoryData(serviceId)
        viewModel.getHistoryData.observe(viewLifecycleOwner) {
            it?.let {
                historyByServiceIdResponse = it
                    historyAdapter.updateItems( it.data)
                    historyList = it.data
                }
                binding.shimmer.stopShimmer()
                binding.shimmer.visibility = View.GONE
                binding.searchHistoryRecycle.visibility = View.GONE
                binding.historyRecycle.visibility = View.VISIBLE
                binding.animationView.visibility = View.GONE
                binding.textNoData.visibility = View.GONE
            }
        }

    private fun observeSearchHistoryData() {
        viewModel.getSearchHistoryData.observe(viewLifecycleOwner) {
            it?.let {
                binding.historyRecycle.visibility = View.GONE
                binding.searchHistoryRecycle.visibility = View.VISIBLE
                searchHistoryAdapter.updateItems(listOf(ServiceData(it)))
                binding.shimmer.stopShimmer()
                binding.shimmer.visibility = View.GONE
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
        viewModel.navToSeeAll.observe(viewLifecycleOwner) {
            it?.let {
                navToSeeAll(it)
            }
        }
    }

    private fun observeNavToServiceDetails() {
        viewModel.navToTaskDetails.observe(viewLifecycleOwner) {
            it?.let {
                navToServiceDetails(it)
            }
        }
    }

    private fun navToServiceDetails(serviceData: ServiceData) {
        val action = HistoryByServiceTypeFragmentDirections.actionHistoryByIDToServiceDetails(serviceData)
        findNavController().navigate(action)
    }


    private fun navToSeeAll(period: String) {
        val action = FilteredHistotyFragmentDirections.actionFilteredHistoryToHistoryByService(serviceId.toString(),period)
        findNavController().navigate(action)
    }

    private fun observeShowError() {
        viewModel.setError.observe(viewLifecycleOwner) {
            it?.let {
                if(it.equals("No content")||it.equals("Bad Request")) {
                    binding.shimmer.stopShimmer()
                    binding.shimmer.visibility = View.GONE
                    binding.historyRecycle.visibility = View.GONE
                    binding.searchHistoryRecycle.visibility = View.GONE
                    binding.animationView.visibility = View.VISIBLE
                    binding.textNoData.visibility = View.VISIBLE
                }
                else{
                    Snackbar.make(requireView(), it, Snackbar.LENGTH_INDEFINITE)
                        .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).setBackgroundTint(
                            resources.getColor(
                                R.color.teal
                            )
                        )
                        .setActionTextColor(resources.getColor(R.color.white)).setAction(getString(R.string.cancel))
                        {
                        }.show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).bottomNavigation.isGone = true
        //  checkNetwork(serviceId)
        registerConnectivityNetworkMonitor()
      //  setPeriodTimeMenuItems()
    }

    private fun setUpUI() {
        //     binding.mSearch.setQueryHint(getString(R.string.search_hint));
        binding.historyRecycle.visibility = View.VISIBLE
        binding.historyRecycle.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }
        val linearLayoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        linearLayoutManager.reverseLayout = false
        linearLayoutManager.stackFromEnd = true
        binding.periodRecycle.apply {
            layoutManager = linearLayoutManager
            adapter = periodAdapter
        }
        binding.searchHistoryRecycle.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = searchHistoryAdapter
        }
        binding.serviceTitle.text = Constants.getServaceNameAr(serviceId)
        binding.mSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    val taskNum = Constants.convertNumsToEnglish(query).toInt()
                    viewModel.searchHistoryDataByService(SearchRequest(query.toInt(),serviceId))
                }
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {

                return false
            }
        })
       //       setPeriodTimeMenuItems()
    }


//    private fun setPeriodTimeMenuItems() {
//        binding.periodTimeTextView.setText(R.string.period_time)
//
//        val adapter = ArrayAdapter(
//            requireContext(), R.layout.dropdown_item,
//            periodTimeList_ar
//        )
//        binding.periodTimeTextView.setAdapter(adapter)
//        binding.periodTimeTextView.onItemClickListener =
//            AdapterView.OnItemClickListener { parent, _, position, id ->
//                val selectedItem = parent.getItemAtPosition(position).toString()
//                historyAdapter.clearList()
//                val index = periodTimeList_ar.indexOf(selectedItem)
//                selectedPeriod = periodTimeList_en[index]
//                if (NetworkConnection.checkInternetConnection(requireContext())) {
//                    viewModel.getHistoryData(serviceId, periodTimeList_en[index])
//                }
//                else{
//                    showMessage()
//                }
//            }
//    }

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
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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
                            viewModel.getHistoryData(serviceId,FilteredHistoryViewModel.periodTimeList_en[FilteredHistoryViewModel.lastSelectedPos])
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

    private fun checkNetwork(id: Int ) {
        lifecycleScope.launchWhenResumed {
            if (NetworkConnection.checkInternetConnection(requireContext())) {
                viewModel.getHistoryData(id)
            } else {
                binding.textNoInternet.visibility = View.VISIBLE
                binding.noNetworkResult.visibility = View.VISIBLE
                binding.linearLayout.visibility = View.GONE
            }
        }
    }


}