package com.rino.visualdestortion.ui.serviceDetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rino.visualdestortion.databinding.FragmentServiceDetailsBinding
import com.rino.visualdestortion.model.pojo.history.ServiceData
import com.rino.visualdestortion.ui.AddService.AddServiceFragmentDirections
import com.rino.visualdestortion.ui.qrCode.QRCodeFragmentDirections
import com.rino.visualdestortion.utils.Constants
import com.smarteist.autoimageslider.SliderView
import com.squareup.picasso.Picasso


class ServiceDetailsFragment : Fragment() {

    private lateinit var viewModel: ServiceDetailsViewModel
    private lateinit var binding: FragmentServiceDetailsBinding
    private lateinit var sliderDataArrayList: ArrayList<SliderData>
    private lateinit var sliderAdapter :SliderAdapter
    private lateinit var equipmentsAdapter :EquipmentListAdapter
    private lateinit var workerTypesAdapter :WorkerTypeListAdapter

    //equipmentsAdapter
    private  lateinit var service: ServiceData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
             service = arguments?.get("serviceObj") as ServiceData
            sliderDataArrayList= arrayListOf(SliderData(service.beforeImg,"الصورة قبل المهمة"),
                                             SliderData(service.duringImg,"الصورة أثناء المهمة"),
                                             SliderData(service.afterImg,"الصورة بعد المهمة")

                                             )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ServiceDetailsViewModel(requireActivity().application)
        binding = FragmentServiceDetailsBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.imageSlider.startAutoCycle()
    }
    private  fun init(){
        binding.imageSlider.autoCycleDirection = SliderView.LAYOUT_DIRECTION_RTL
        sliderAdapter = SliderAdapter(requireContext(),sliderDataArrayList)
        binding.imageSlider.setSliderAdapter(sliderAdapter)
        binding.imageSlider.scrollTimeInSec = 3
        binding.imageSlider.isAutoCycle = true


        binding.dateFromTxt.text = Constants.convertNumsToArabic(service.createdDate?:"")
        binding.addressValue.text = service.fullLocation
        binding.serviceNumValue.text = Constants.convertNumsToArabic(service.serviceNumber.toString())
        if(service.notes!=null && (service.notes?:"").isEmpty()) {
            binding.notesValue.text = service.notes
        }
        else{
            binding.notesValue.visibility = View.GONE
            binding.notesTxt.visibility = View.GONE
        }
  //      Picasso.with(context).load(service.qrCodeImg).into(binding.qrCodeImg)
        binding.viewQRCodeBtn.setOnClickListener{
            navToQrCode(service.qrCodeImg)
        }
        binding.imageSlider.setCurrentPageListener {
            val currentPos = binding.imageSlider.currentPagePosition
            when(currentPos){
                0->binding.titleTxt.text ="الصورة قبل المهمة"
                1->binding.titleTxt.text ="الصورة أثناء المهمة"
                2->binding.titleTxt.text ="الصورة بعد المهمة"
            }
        }
        equipmentsAdapter = EquipmentListAdapter(service.equipmentList, requireContext())
        workerTypesAdapter = WorkerTypeListAdapter(service.workerstList, requireContext())
        binding.equipmentsRecycle.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = equipmentsAdapter
        }
        binding.workerTypeRecycle.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = workerTypesAdapter
        }
    }

    private fun navToQrCode(qrCodeUrl: String?) {
        val action = ServiceDetailsFragmentDirections.actionServiceDetailsToQrCode(qrCodeUrl?:"","history")
        findNavController().navigate(action)
    }

}