package com.rino.visualdestortion.ui.qrCode

import com.rino.visualdestortion.R
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.rino.visualdestortion.databinding.FragmentQRCodeBinding
import com.rino.visualdestortion.utils.NetworkConnection
import com.squareup.picasso.Callback

import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*


class QRCodeFragment : Fragment() {
    private lateinit var binding: FragmentQRCodeBinding
    private lateinit var qrCodeImg: Bitmap
    private var url = ""
    private var serviceId = ""
    private var serviceName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            @SuppressLint("ResourceType")
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentQRCodeBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    private fun init() {
        registerConnectivityNetworkMonitor()
        if (getArguments() != null) {
            // The getPrivacyPolicyLink() method will be created automatically.
            url = getArguments()?.get("QRCodeURL").toString()
            serviceId = getArguments()?.get("serviceID").toString()
            serviceName = getArguments()?.get("serviceName").toString()
            val source = arguments?.get("source").toString()
            if (source.equals("history")) {
                binding.backImg.visibility = View.GONE
            }
            Log.e("QRCodeURL", url)
            if (NetworkConnection.checkInternetConnection(requireContext())) {
                downloadQRCode(url)
            } else {
                showMessage(getString(R.string.no_internet))
            }
        }
        binding.backImg.setOnClickListener {
            navTAddService()
        }
        binding.navigateToHome.setOnClickListener {
            navigateToHome()
        }
        binding.shareInWhatsapp.setOnClickListener {
            shareQRCodeInWhatsapp()
        }
    }

    private fun navTAddService() {
        val action = QRCodeFragmentDirections.actionQRCodeToAddForm(serviceName, serviceId)
        findNavController().navigate(action)
    }

    private fun shareQRCodeInWhatsapp() {
        val bitmap = (binding.qrCodeImg.drawable.toBitmap())
        val imgUri: Uri = getImageUri(bitmap)
        var whatsappIntent = Intent(Intent.ACTION_SEND)
        whatsappIntent.setPackage("com.whatsapp")
        whatsappIntent.putExtra(Intent.EXTRA_TEXT, "\n ${getString(R.string.QRCode)} \n" + url)
        whatsappIntent.putExtra(Intent.EXTRA_STREAM, imgUri)
        whatsappIntent.type = "image/*"
        whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            requireActivity()!!.startActivity(whatsappIntent)
        } catch (ex: ActivityNotFoundException) {
            showWarningDialog(getString(R.string.whatsApp_not_installed))

        }
    }

    private fun showWarningDialog(message: String) {
        AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setNegativeButton(getString(R.string.cancel),
                DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
            .setCancelable(true)
            .create().show()
    }

    fun getImageUri(inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val currentDate = sdf.format(Date())
        val path = MediaStore.Images.Media.insertImage(
            requireContext().contentResolver,
            inImage,
            "QRCode_IMG_" + currentDate.toString().replace(" ", ""),
            null
        )
//        val path = MediaStore.Images.Media.insertImage(
//            inContext.contentResolver,
//            inImage,
//            "After_IMG_",
//            null
//        )
        return Uri.parse(path)
    }

    private fun navigateToHome() {
        val action = QRCodeFragmentDirections.actionQRCodeToServices()
        findNavController().navigate(action)
    }

    private fun downloadQRCode(url: String) {

        Picasso.with(requireContext()).load(url)
            .into(binding.qrCodeImg, object : Callback {
                override fun onSuccess() {
                    if (binding.progress != null) {
                        binding.progress.setVisibility(View.GONE)
                    }
                }

                override fun onError() {}
            })
    }

    private fun showMessage(msg: String) {
        lifecycleScope.launchWhenResumed {
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
    }

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