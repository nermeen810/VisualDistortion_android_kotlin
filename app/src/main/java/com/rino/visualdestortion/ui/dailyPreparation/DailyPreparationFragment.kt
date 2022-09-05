package com.rino.visualdestortion.ui.dailyPreparation

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.rino.visualdestortion.R
import com.rino.visualdestortion.databinding.FragmentDailyPreparationBinding
import com.rino.visualdestortion.model.pojo.dailyPraperation.GetDailyPraprationData
import com.rino.visualdestortion.ui.home.MainActivity
import com.rino.visualdestortion.utils.NetworkConnection
import java.util.*


class DailyPreparationFragment : Fragment() {
    private lateinit var viewModel: DailyPreparationViewModel
    private lateinit var binding: FragmentDailyPreparationBinding
    private lateinit var addServiceResponse: GetDailyPraprationData
    private lateinit var equipmentList: ArrayList<String>
    private lateinit var workersTypeList: ArrayList<String>
    private lateinit var equipmentsAdapter: EquipmentsAdapter
    private lateinit var workerTypesAdapter: WorkerTypesAdapter
    private lateinit var equipmentsMap: HashMap<String?, Int?>
    private lateinit var workersTypeMap: HashMap<String?, Int?>
    private lateinit var equipmentsCountList: ArrayList<EquipmentItem>
    private lateinit var workerTypesCountList: ArrayList<EquipmentItem>
    private lateinit var equipmentsCountMap: HashMap<Long?, Int?>
    private lateinit var workerTypesCountMap: HashMap<Long?, Int?>
    private var serviceTypeId = ""
    private var serviceName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            serviceName = getArguments()?.get("serviceName").toString()
            serviceTypeId = getArguments()?.get("serviceID").toString()
        }


    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).bottomNavigation.isGone = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = DailyPreparationViewModel(requireActivity().application)
        binding = FragmentDailyPreparationBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    private fun init() {
        setUpUI()
        checkNetwork()
        initLists()
        registerConnectivityNetworkMonitor()
        observeData()
    }

    // setup equipments recycle and workerType recycle and Next onclick
    private fun setUpUI() {
        nextOnClick()
        equipmentsAdapter = EquipmentsAdapter(arrayListOf(), viewModel, requireContext())
        workerTypesAdapter = WorkerTypesAdapter(arrayListOf(), viewModel, requireContext())
        binding.equipmentsRecycle.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = equipmentsAdapter
        }
        binding.workersTypeRecycle.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = workerTypesAdapter
        }

    }

    private fun checkNetwork() {
        if (NetworkConnection.checkInternetConnection(requireContext())) {
            viewModel.getServicesData()
        } else {
            showMessage(getString(R.string.no_internet))
        }
    }

    private fun nextOnClick() {
        binding.nextButton.setOnClickListener {
            if (validateData()) {
                if (NetworkConnection.checkInternetConnection(requireContext())) {
                    viewModel.setDailyPreparation(
                        workerTypesAdapter.getWorkerTypesMap(),
                        equipmentsAdapter.getEquipmentMap()
                    )
                } else {
                    showMessage(getString(R.string.no_internet))
                }
            }
        }
    }

    private fun initLists() {
        equipmentList = ArrayList()
        workersTypeList = ArrayList()
        equipmentsMap = HashMap()
        workersTypeMap = HashMap()
        equipmentsCountList = ArrayList()
        workerTypesCountList = ArrayList()
        equipmentsCountMap = HashMap()
        workerTypesCountMap = HashMap()
    }

    private fun observeData() {
        observeSetDailyPreparation()
        observeGetServicesData()
        observeLoading()
        observeShowError()
        observeDeletedEquipmentItem()
        observeDeletedWorkerTypeItem()
    }

    private fun observeSetDailyPreparation() {
        viewModel.setDailyPreparation.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    navigateToHome()
                }
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

    private fun observeDeletedWorkerTypeItem() {
        viewModel.workerTypeDeleteItem.observe(viewLifecycleOwner) {
            it?.let {
                workerTypesCountList.remove(it)
                workersTypeList.add(it.name)
            }
        }
    }

    private fun observeDeletedEquipmentItem() {
        viewModel.equipmentsDeleteItem.observe(viewLifecycleOwner) {
            it?.let {
                equipmentsCountList.remove(it)
                equipmentList.add(it.name)
            }
        }
    }

    private fun observeGetServicesData() {
        viewModel.getServicesData.observe(viewLifecycleOwner) {
            it.let {
                addServiceResponse = it
                prepareMenues()

            }
        }
    }

    private fun prepareMenues() {
        setEquipmentsMenuItems()
        setWorkersTypeMenuItems()
    }

    private fun setEquipmentsMenuItems() {
        equipmentList.clear()
        var index = 0
        binding.equipmentsTextView.setText(R.string.select)
        addServiceResponse.equipmentList.forEach { equipment ->
            equipmentList.add(equipment.name.toString())
            equipmentsMap[equipment.name] = equipment.id
            index++
        }
        val adapter = ArrayAdapter(
            requireContext(), R.layout.dropdown_item,
            equipmentList
        )
        binding.equipmentsTextView.setAdapter(adapter)
        binding.equipmentsTextView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val selectedItem = parent.getItemAtPosition(position).toString()
                var item =
                    equipmentsMap[selectedItem]?.toLong()
                        ?.let { EquipmentItem(selectedItem, it, 1) }
                if (item != null) {
                    equipmentsCountList.add(item)
                    equipmentList.remove(item.name)
                }
                equipmentsAdapter.updateItems(equipmentsCountList)
            }

    }

    private fun setWorkersTypeMenuItems() {
        workersTypeList.clear()
        var index = 0
        binding.workersTypeTextView.setText(R.string.select)
        addServiceResponse.workerTypesList.forEach { workerType ->
            //        for (workerType in dailyPreparation.workerTypes) {
            workersTypeList.add(workerType.name.toString())
            workersTypeMap[workerType.name] = workerType.id
            index++
        }
        val adapter = ArrayAdapter(
            requireContext(), R.layout.dropdown_item,
            workersTypeList
        )
        binding.workersTypeTextView.setAdapter(adapter)
        binding.workersTypeTextView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val selectedItem = parent.getItemAtPosition(position).toString()
                var item =
                    workersTypeMap[selectedItem]?.toLong()
                        ?.let { EquipmentItem(selectedItem, it, 1) }
                if (item != null) {
                    workerTypesCountList.add(item)
                    workersTypeList.remove(item.name)
                    workerTypesAdapter.updateItems(workerTypesCountList)
                }

            }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun validateData(): Boolean {
        var flagEquipment = true
        var flagWorkerType = true
        if (equipmentsCountList.isEmpty()) {
            binding.equipmentsTextInputLayout.error = getString(R.string.required_field)
            flagEquipment = false
        } else {
            binding.equipmentsTextInputLayout.error = null
            binding.equipmentsTextInputLayout.isErrorEnabled = false
            flagEquipment = true
        }
        if (workerTypesCountList.isEmpty()) {
            binding.workersTypeTextInputLayout.error = getString(R.string.required_field)
            flagWorkerType = false
        } else {
            binding.workersTypeTextInputLayout.error = null
            binding.workersTypeTextInputLayout.isErrorEnabled = false
            flagEquipment = true
        }
        return (flagEquipment && flagWorkerType)
    }

    private fun navigateToHome() {
        val action = DailyPreparationFragmentDirections.actionDailyPreparationToServices()
        findNavController().navigate(action)

    }

// snackBar message
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
// network observer
    private fun registerConnectivityNetworkMonitor() {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val builder = NetworkRequest.Builder()
        connectivityManager.registerNetworkCallback(builder.build(),
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
//                    if (activity != null) {
//                        activity!!.runOnUiThread {
//                            showMessage(getString(R.string.internet))
//                        }
//                    }
                }

                override fun onLost(network: Network) {
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