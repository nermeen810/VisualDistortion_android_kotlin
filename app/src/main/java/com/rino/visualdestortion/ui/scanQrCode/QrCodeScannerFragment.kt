package com.rino.visualdestortion.ui.scanQrCode

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.budiyev.android.codescanner.*
import com.google.android.material.snackbar.Snackbar
import com.rino.visualdestortion.R
import com.rino.visualdestortion.databinding.FragmentQrCodeScannerBinding
import com.rino.visualdestortion.ui.home.MainActivity
import com.rino.visualdestortion.ui.serviceDetails.ServiceDetailsFragmentDirections
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher


class QrCodeScannerFragment : Fragment() {
    private lateinit var codeScanner: CodeScanner
    private lateinit var binding: FragmentQrCodeScannerBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentQrCodeScannerBinding.inflate(inflater, container, false)
        init()
        return binding.root


    }

    private fun init() {
        codeScanner = CodeScanner(requireContext(), binding.scannerView)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not
        // Callbacks
        if (isCameraPermissionGranted()) {
            codeScanner.decodeCallback = DecodeCallback {
                requireActivity().runOnUiThread {
                    if (it.text.contains("Created by") && it.text.contains("Service name") &&
                        it.text.contains("Date created") && it.text.contains("Locations") &&
                        it.text.contains("Before image") && it.text.contains("After image")
                    ) {
                        navToQrCodeResult(it.text)
                    } else {
                        codeScanner.releaseResources()
                        codeScanner.stopPreview()
                        codeScanner.startPreview()
                        showMessage(getString(R.string.wrong_qr_code))
                    }
                }
            }
            codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
                requireActivity().runOnUiThread {
                    codeScanner.releaseResources()
                    showMessage(getString(R.string.wrong_qr_code))
                }


            }
        } else {
            navtoAppSetting()
        }
        binding.scannerView.setOnClickListener {
            codeScanner.startPreview()
        }

    }

    private fun navtoAppSetting() {
        startActivity(Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", requireContext().packageName, null)
        })
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).bottomNavigation.isGone = false
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun navToQrCodeResult(qrCodeUrl: String) {
        val action = QrCodeScannerFragmentDirections.actionScannerToResultQrCode(qrCodeUrl)
        findNavController().navigate(action)
    }

    private fun showMessage(msg: String) {
        Snackbar.make(requireView(), msg, Snackbar.LENGTH_INDEFINITE)
            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).setBackgroundTint(
                resources.getColor(
                    R.color.teal
                )
            )
            .setActionTextColor(resources.getColor(R.color.white)).setAction("Ok")
            {
            }.show()
    }

    private fun isCameraPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireActivity().application,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
}