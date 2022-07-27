package com.rino.visualdestortion.ui.fingerPrint

import android.Manifest
import android.app.KeyguardManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES.P
import android.os.Bundle
import android.os.CancellationSignal
import android.provider.Settings
import android.provider.Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.rino.visualdestortion.R


class FingerPrintFragment : Fragment() {
    private lateinit var viewModel: FingerPrintViewModel
    private var  cancellationSignal: CancellationSignal? = null
    private val  authenticationCallback: BiometricPrompt.AuthenticationCallback
        get() =
            @RequiresApi(P)
            object: BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                    super.onAuthenticationError(errorCode, errString)
                    this@FingerPrintFragment.notifyErrorToUser(getString(R.string.FP_error))
    //                enablePermission()
                    viewModel.logout()
                    navgateToLogin()
                }
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                    super.onAuthenticationSucceeded(result)
                    notifyMsgToUser(getString(R.string.FP_success))
//                    if(findNavController().currentDestination?.id == R.id.loginFragment2)
                    if(viewModel.isLogin())
                    {
                        findNavController().popBackStack()
                    //    navgateToHome()
                    }
                    else {
                        navgateToLogin()
                    }
                }
            }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    private  fun  navgateToLogin(){
        val action = FingerPrintFragmentDirections.actionFingerPrintToLogin()
        findNavController().navigate(action)
    }
    private  fun  navgateToHome(){
        val action = FingerPrintFragmentDirections.actionFingerPrintToServices()
        findNavController().navigate(action)
    }
    private fun enablePermission() {
        val intent = if (Build.VERSION.SDK_INT >= P) {
            Intent(Settings.ACTION_FINGERPRINT_ENROLL)
        } else {
            Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", requireContext().packageName, null)

            }
        }
            startActivity(intent)

    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.USE_BIOMETRIC,
            ),
            1
        )
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewModel = FingerPrintViewModel(requireActivity().application)
        checkBiometricSupport()
        val biometricPrompt = BiometricPrompt.Builder(requireActivity())
            .setTitle("معالجة التشوه البصرى")
            .setDescription("برجاء وضع بصمة الاصبع")
            .setNegativeButton("الغاء", requireActivity().mainExecutor, DialogInterface.OnClickListener { dialog, which ->
                notifyErrorToUser(getString(R.string.Authentication_Cancelled))
                viewModel.logout()
                navgateToLogin()
            }).build()

        // start the authenticationCallback in mainExecutor
        biometricPrompt.authenticate(getCancellationSignal(), requireActivity().mainExecutor, authenticationCallback)
        return inflater.inflate(R.layout.fragment_finger_print, container, false)

    }

    private fun notifyErrorToUser(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_INDEFINITE)
            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).setBackgroundTint(
                resources.getColor(
                    R.color.teal
                )
            )
            .setActionTextColor(resources.getColor(R.color.white)).setAction(getString(R.string.dismiss))
            {
            }.show()
    }
    private fun notifyMsgToUser(message: String) {
        Toast.makeText(requireContext(),message, Toast.LENGTH_SHORT).show()
    }
    private fun getCancellationSignal(): CancellationSignal {
        cancellationSignal = CancellationSignal()
        cancellationSignal?.setOnCancelListener {
            notifyMsgToUser(getString(R.string.Authentication_Cancelled))
            viewModel.logout()
            navgateToLogin()
        }
        return cancellationSignal as CancellationSignal
    }
    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkBiometricSupport(): Boolean {
        val keyguardManager = requireActivity().getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (!keyguardManager.isDeviceSecure) {
            notifyMsgToUser("برجاء السماح لصلاحية ستخدام بصمة الاصبع")
            enablePermission()
            return false
        }
        if (ActivityCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.USE_BIOMETRIC) != PackageManager.PERMISSION_GRANTED) {
            notifyMsgToUser("رجاء السماح لصلاحية ستخدام بصمة الاصبع")
            requestPermission()
            return false
        }
        return if (requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            true
        } else true
    }
}