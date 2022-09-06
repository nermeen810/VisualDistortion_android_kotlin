package com.rino.visualdestortion.ui.setting

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.rino.visualdestortion.R
import com.rino.visualdestortion.databinding.FragmentSettingBinding


class settingFragment : Fragment() {
    private lateinit var viewModel: SettingViewModel
    private lateinit var binding: FragmentSettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            @SuppressLint("ResourceType")
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = SettingViewModel(requireActivity().application)
        binding = FragmentSettingBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    private fun init() {
        binding.logoutTxt.setOnClickListener {
            viewModel.logout()
            navToLogin()
        }
    }

    private fun navToLogin() {
        val action = settingFragmentDirections.actionSettingToLogin()
        findNavController().navigate(action)
    }


}