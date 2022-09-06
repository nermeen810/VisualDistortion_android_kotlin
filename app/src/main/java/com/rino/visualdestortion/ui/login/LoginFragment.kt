package com.rino.visualdestortion.ui.login

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.rino.visualdestortion.R
import com.rino.visualdestortion.databinding.FragmentLoginBinding
import com.rino.visualdestortion.model.pojo.login.LoginRequest
import com.rino.visualdestortion.ui.home.MainActivity
import com.rino.visualdestortion.utils.NetworkConnection


class LoginFragment : Fragment() {

    private lateinit var viewModel: LoginFragmentViewModel
    private lateinit var binding: FragmentLoginBinding
    private var email = ""
    private var pass = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = LoginFragmentViewModel(requireActivity().application)
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    private fun init() {
        loginButtonOnClick()
        resetPassOnClick()
        privacyPolicyOnClick()
        observeData()
        registerConnectivityNetworkMonitor()
    }

    private fun privacyPolicyOnClick() {
        val text = getString(R.string.privacy_policy)
        val content = SpannableString(text)
        content.setSpan(UnderlineSpan(), 0, text.length, 0)
        binding.privacyPolicyTxt.text = content
        binding.privacyPolicyTxt.setOnClickListener {
            navtoPrivacyPolicyUrl()
        }
    }

    private fun navtoPrivacyPolicyUrl() {
        val url = "https://amanat-jeddah-staging.azurewebsites.net/Home/PrivacyPolicy"
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }

    private fun loginButtonOnClick() {
        binding.loginButton.setOnClickListener {
            //   binding.progress.visibility = View.VISIBLE
            email = binding.editTextEmail.text.toString()
            pass = binding.editTextPassword.text.toString()
            validateData()
        }
    }

    private fun resetPassOnClick() {
        binding.resetPassTxt.setOnClickListener {
            navigateToResetPass()
        }
    }

    private fun navigateToResetPass() {
        val action = LoginFragmentDirections.actionLoginToResetPass()
        findNavController().navigate(action)
    }

    private fun observeData() {
        observeSuccessLogin()
        observeIsPrepared()
        observeLoading()
        observeShowError()

    }

    private fun observeSuccessLogin() {
        viewModel.isLogin.observe(viewLifecycleOwner) {
            if (it) {
                //   binding.progress.visibility = View.GONE
                Toast.makeText(
                    requireActivity(),
                    getString(R.string.success_login),
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.isTodayPrepared()
                //   navigateToHome()

            } else {
                Toast.makeText(
                    requireActivity(),
                    getString(R.string.invalid_email_or_pass),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun observeIsPrepared() {
        viewModel.isPrepared.observe(viewLifecycleOwner) {
            if (it) {
                navigateToHome()
                //   navigateToDailyPreparation()
            } else {
                navigateToDailyPreparation()
                //   navigateToHome()
            }
        }
    }

    private fun navigateToHome() {
        val action = LoginFragmentDirections.actionLoginToServiceFragment()
        findNavController().navigate(action)
    }

    private fun navigateToDailyPreparation() {
        val action = LoginFragmentDirections.actionLoginToDailyPreparation()
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
                showMessage(it)
            }
        }
    }

    private fun validateData() {
        validateEmail()
        validatPassword()
        if (validateEmail() && validatPassword()) {
            if (NetworkConnection.checkInternetConnection(requireContext())) {
                viewModel.login(LoginRequest(email, pass))
            } else {
                showMessage(getString(R.string.no_internet))
            }
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
        return if (pass.isEmpty()) {
            binding.textInputPassword.error = getString(R.string.required_field)
            false
        }
//        else if (!pass.matches(passwordVal.toRegex())) {
//            binding.textInputPassword.error = "كلمة المرور ضعيفة"
//            false
//        }
        else {
            binding.textInputPassword.error = null
            binding.textInputPassword.isErrorEnabled = false
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