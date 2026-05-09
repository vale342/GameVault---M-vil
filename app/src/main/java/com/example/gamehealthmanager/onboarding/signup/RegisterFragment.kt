package com.example.gamehealthmanager.onboarding.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.gamehealthmanager.R
import androidx.navigation.fragment.findNavController
import com.example.gamehealthmanager.core.FragmentCommunicator
import com.example.gamehealthmanager.core.ResponseService
import com.example.gamehealthmanager.databinding.FragmentRegisterBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {
    private var _binding : FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<RegisterViewModel>()
    private lateinit var communicator: FragmentCommunicator


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        communicator = requireActivity() as FragmentCommunicator
        setupValidation()
        setupClickListeners()
        observeState()
        return binding.root
    }

    private fun setupValidation() {
        binding.signInButton.isEnabled = false
        val watcher = { validateAndEnable() }
        binding.emailTiet.addTextChangedListener { validateAndEnable() }
        binding.passwordTiet.addTextChangedListener { validateAndEnable() }
        binding.confirmPasswordTiet.addTextChangedListener { validateAndEnable() }
    }

    private fun validateAndEnable() {
        val email = binding.emailTiet.text.toString().trim()
        val pass = binding.passwordTiet.text.toString().trim()
        val confirm = binding.confirmPasswordTiet.text.toString().trim()

        binding.emailTil.error = viewModel.validateEmail(email)
        binding.passwordTil.error = viewModel.validatePassword(pass)
        binding.confirmPasswordTil.error =
            viewModel.validateConfirmPassword(pass, confirm)

        binding.signInButton.isEnabled =
            viewModel.isRegisterFormValid(email, pass, confirm)
    }

    private fun setupClickListeners() {
        binding.signInButton.setOnClickListener {
            val email = binding.emailTiet.text.toString().trim()
            val password = binding.passwordTiet.text.toString().trim()
            viewModel.requestSignUp(email, password)
        }
        binding.registerText.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registerState.collect { state ->
                    when (state) {
                        is ResponseService.Loading -> {
                            communicator.manageLoader(true)
                            binding.signInButton.isEnabled = false
                        }
                        is ResponseService.Success -> {
                            communicator.manageLoader(false)
                            findNavController().navigate(R.id.action_registerFragment_to_personalInfoFragment)
                        }
                        is ResponseService.Error -> {
                            communicator.manageLoader(false)
                            binding.signInButton.isEnabled = true
                            Snackbar.make(binding.root, state.error,
                                Snackbar.LENGTH_LONG).show()
                        }
                        null -> Unit
                    }
                }
            }
        }
    }
}