package com.rino.visualdestortion.ui.resetPassward

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
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.rino.visualdestortion.R
import com.rino.visualdestortion.ui.home.MainActivity
import com.rino.visualdestortion.databinding.FragmentResetPasswordBinding
import com.rino.visualdestortion.utils.NetworkConnection

class ResetPasswordFragment : Fragment() {
    private lateinit var viewModel: ResetPasswordViewModel
    private lateinit var binding: FragmentResetPasswordBinding
    private var email = ""
    private var newPass = ""
    private var newPassCongirm = ""
    private var otp = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ResetPasswordViewModel(requireActivity().application)
        binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    private fun init() {
        requestOTPButtonOnClick()
        saveNewPassOnClick()
        observeData()
        registerConnectivityNetworkMonitor()
    }

    private fun requestOTPButtonOnClick() {

        binding.reuestOtpButton.setOnClickListener {
            email = binding.editTextEmail.text.toString()
            if (validateEmail()) {
                if (NetworkConnection.checkInternetConnection(requireContext())) {
                    viewModel.requestOTP(email)
                } else {
                    showMessage(getString(R.string.no_internet))
                }
            }
        }

    }

    private fun saveNewPassOnClick() {
        binding.saveButton.setOnClickListener {
            getDataFromUI()
            validateData()
        }

    }

    private fun getDataFromUI() {
        email = binding.editTextEmail.text.toString()
        newPass = binding.editTextPassword.text.toString()
        newPassCongirm = binding.editTextConfirmPassword.text.toString()
        otp = binding.editTextOTPCode.text.toString()
    }

    private fun observeData() {
        observeSuccessOTP()
        observeSuccessResetPass()
        observeLoading()
        observeShowError()
    }

    private fun observeSuccessResetPass() {
        viewModel.resetPass.observe(viewLifecycleOwner) {
            it.let {
                Toast.makeText(
                    requireActivity(),
                    it?.status,
                    Toast.LENGTH_SHORT
                ).show()
                navigateToLogin()

            }
        }
    }

    private fun observeSuccessOTP() {
        viewModel.getOTP.observe(viewLifecycleOwner) {
            it.let {
                binding.otpCodeTextInput.visibility = View.VISIBLE
                binding.textInputPassword.visibility = View.VISIBLE
                binding.textInputConfirmPassword.visibility = View.VISIBLE
                binding.saveButton.visibility = View.VISIBLE
                binding.reuestOtpButton.visibility = View.GONE
                Toast.makeText(
                    requireActivity(),
                    it?.status,
                    Toast.LENGTH_SHORT
                ).show()

            }
        }
    }

    private fun navigateToLogin() {
        val action = ResetPasswordFragmentDirections.actionResetPassToLogin()
        findNavController().navigate(action)
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

    private fun validateData() {
        validateEmail()
        validatPassword()
        validatConfirmPassword()
        validateOTP()
        if (validateEmail() && validatPassword() && validateOTP() && validatConfirmPassword()) {
            if (NetworkConnection.checkInternetConnection(requireContext())) {
                viewModel.resetPass(email, otp, newPass)
            } else {
                showMessage(getString(R.string.no_internet))
            }

        }
    }

    private fun validateOTP(): Boolean {
        return if (otp.isEmpty()) {
            binding.otpCodeTextInput.error = getString(R.string.required_field)
            false
        } else if (otp.length != 4) {
            binding.otpCodeTextInput.error = getString(R.string.qrCode_must_4number)
            false
        } else {
            binding.otpCodeTextInput.error = null
            binding.otpCodeTextInput.isErrorEnabled = false
            true
        }
    }

    private fun validateEmail(): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex()
        return if (email.isEmpty()) {
            binding.textInputEmail.error = getString(R.string.required_field)
            false
        } else if (email.length > 50) {
            binding.textInputEmail.error = getString(R.string.email_must_less_than50)
            false
        } else if (!email.matches(emailPattern)) {
            binding.textInputEmail.error = getString(R.string.invalid_email)
            false
        } else {
            binding.textInputEmail.error = null
            binding.textInputEmail.isErrorEnabled = false
            true
        }
    }

    private fun validatPassword(): Boolean {
        val passwordVal = "^" + "(?=.*[0-9])" +         //at least 1 digit
                "(?=.*[a-z])" +         //at least 1 lower case letter
                "(?=.*[A-Z])" +         //at least 1 upper case letter
                //   "(?=.*[a-zA-Z])" +  //any letter
                "(?=.*[@#$%^&+=])" +  //at least 1 special character
                "(?=\\S+$)" +  //no white spaces
                ".{8,}"  //at least 8 characters
//                "$"
        return if (newPass.isEmpty()) {
            binding.textInputPassword.error = getString(R.string.required_field)
            false
        } else if (!newPass.matches(passwordVal.toRegex())) {
            binding.textInputPassword.error = getString(R.string.weak_password)
            false
        } else {
            binding.textInputPassword.error = null
            binding.textInputPassword.isErrorEnabled = false
            true
        }

    }

    private fun validatConfirmPassword(): Boolean {
        return if (newPassCongirm.isEmpty()) {
            binding.textInputConfirmPassword.error = getString(R.string.required_field)
            false
        } else if (newPass != newPassCongirm) {
            //  Toast.makeText(requireActivity(), "newPass :" + newPass == newPassCongirm, Toast.LENGTH_SHORT).show()
            binding.textInputConfirmPassword.error = getString(R.string.pass_not_matched)
            false
        } else {
            binding.textInputConfirmPassword.error = null
            binding.textInputConfirmPassword.isErrorEnabled = false
            true
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).bottomNavigation.isGone = true
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