package com.rino.visualdestortion.ui.qrCodeResult

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.SyncStateContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import com.rino.visualdestortion.databinding.FragmentQrCodeResultBinding
import com.rino.visualdestortion.ui.home.MainActivity
import com.rino.visualdestortion.ui.serviceDetails.SliderAdapter
import com.rino.visualdestortion.ui.serviceDetails.SliderData
import com.rino.visualdestortion.utils.Constants
import com.smarteist.autoimageslider.SliderView

class QrCodeResultFragment : Fragment() {
    private lateinit var sliderDataArrayList: ArrayList<SliderData>
    private lateinit var sliderAdapter: SliderAdapter
    private lateinit var binding: FragmentQrCodeResultBinding
    private lateinit var service: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentQrCodeResultBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).bottomNavigation.isGone = true
        binding.imageSlider.startAutoCycle()
    }

    private fun init() {
        arguments?.let {
            service = arguments?.get("scanResult").toString()
            var result = service.split("\r?\n|\r".toRegex()).toTypedArray()
            val resultList = listOf(*result)
            Log.e("scan result", resultList.toString())
            setUpData(resultList)

        }
    }

    @SuppressLint("SetTextI18n")
    private fun setUpData(result: List<String>) {
        val createdBy = result[0].split(":")
        val createdDate = result[3].split(":")
        val serviceName = result[6].split(":")
        val beforeImgUrl = result[9].split(":")
        val duringImgUrl = result[12].split(":")
        val afterImgUrl = result[15].split(":")
        val locationUrl = result[18].split(":")
        binding.menetorNameValue.text = createdBy[1]
        setCreatedDateToUI(createdDate)
        setServiceName(serviceName[1])

        binding.viewLocationBtn.setOnClickListener {
            navToLocationInMap(locationUrl[1].replace(" ", "") + ":" + locationUrl[2])
        }
        setUpSlider(beforeImgUrl, duringImgUrl, afterImgUrl)
    }

    private fun setServiceName(serviceName: String) {
        Log.e("serviceName@", serviceName)
        val serviceNameAr = when (serviceName.replace(" ", "")) {
            "StreetVendors" -> "الباعة الجائلين"
            "BannedSunshades" -> "المظلات المخالفة"
            "BannedSigns" -> "اللوحات المخالفة"
            "BannedPosters" -> "الملصقات الدعائية"
            "BannedGraffiti" -> "الكتابات المشوهة"
            "DemolitionWaste" -> "مخلفات الهدم"
            else -> ""
        }
        binding.serviceNumValue.text = serviceNameAr
    }

    private fun setCreatedDateToUI(createdDate: List<String>) {
        val createdDateAr =
            Constants.convertNumsToArabic("${createdDate[1]} : ${createdDate[2]} : ${createdDate[3]}")
        binding.addressValue.text = createdDateAr.reversed()
    }


    private fun setUpSlider(
        beforeImgUrl: List<String>,
        duringImgUrl: List<String>,
        afterImgUrl: List<String>
    ) {
        sliderDataArrayList = arrayListOf(
            SliderData(
                beforeImgUrl[1].replace(" ", "") + ":" + beforeImgUrl[2],
                "الصورة قبل المهمة"
            ),
            SliderData(
                duringImgUrl[1].replace(" ", "") + ":" + duringImgUrl[2],
                "الصورة أثناء المهمة"
            ),
            SliderData(afterImgUrl[1].replace(" ", "") + ":" + afterImgUrl[2], "الصورة بعد المهمة")
        )
        binding.imageSlider.autoCycleDirection = SliderView.LAYOUT_DIRECTION_RTL
        sliderAdapter = SliderAdapter(requireContext(), sliderDataArrayList)
        binding.imageSlider.setSliderAdapter(sliderAdapter)
        binding.imageSlider.scrollTimeInSec = 3
        binding.imageSlider.isAutoCycle = true
        binding.imageSlider.setCurrentPageListener {
            val currentPos = binding.imageSlider.currentPagePosition
            when (currentPos) {
                0 -> binding.titleTxt.text = "الصورة قبل المهمة"
                1 -> binding.titleTxt.text = "الصورة أثناء المهمة"
                2 -> binding.titleTxt.text = "الصورة بعد المهمة"
            }
        }
    }

    private fun navToLocationInMap(locationUrl: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(locationUrl))
        requireActivity().startActivity(browserIntent)
    }
}