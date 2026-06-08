package com.example.gamevault.home.account

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.gamevault.R
import com.example.gamevault.core.FragmentCommunicator
import com.example.gamevault.core.ResponseService
import kotlinx.coroutines.launch

class AccountFragment : Fragment() {

    private val viewModel by viewModels<AccountViewModel>()
    private lateinit var communicator: FragmentCommunicator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        communicator = requireActivity() as FragmentCommunicator

        // Escuchar los estados emitidos por el ViewModel
        setupObservers()

        // Redirección a la pantalla de Edición de Perfil
        view.findViewById<View>(R.id.cardPersonalInfo)?.setOnClickListener {
            try {
                val intent = Intent(requireContext(), Class.forName("com.example.gamevault.onboarding.personal.EditarPerfilActivity"))
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Pantalla de edición en desarrollo", Toast.LENGTH_SHORT).show()
            }
        }

        // Redirección a la Configuración de la App
        view.findViewById<View>(R.id.btnConfiguracion)?.setOnClickListener {
            try {
                val intent = Intent(requireContext(), Class.forName("com.example.gamevault.home.account.ConfiguracionActivity"))
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Pantalla de configuración en desarrollo", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón de cierre de sesión
        view.findViewById<View>(R.id.btnCerrarSesion)?.setOnClickListener {
            mostrarDialogoCierreSesion()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is ResponseService.Loading -> {
                            communicator.manageLoader(true)
                        }
                        is ResponseService.Success -> {
                            communicator.manageLoader(false)
                            val datos = state.data

                            // Vincular datos del objeto UserProfileData con los componentes del XML
                            view?.findViewById<TextView>(R.id.tvUserName)?.text = datos.fullName
                            view?.findViewById<TextView>(R.id.tvUserEmail)?.text = datos.email
                            view?.findViewById<TextView>(R.id.tvNombreCompletoInfo)?.text = datos.fullName
                            view?.findViewById<TextView>(R.id.tvCelularInfo)?.text = datos.phone
                            view?.findViewById<TextView>(R.id.tvWishlistCount)?.text = "${datos.wishlistCount} títulos"
                        }
                        is ResponseService.Error -> {
                            communicator.manageLoader(false)
                            Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                        }
                        null -> {}
                    }
                }
            }
        }
    }

    private fun mostrarDialogoCierreSesion() {
        AlertDialog.Builder(requireContext())
            .setTitle("¿Seguro que quieres salir?")
            .setMessage("Cerrarás tu sesión actual en GameVault.")
            .setPositiveButton("Salir") { _, _ ->
                viewModel.cerrarSesion {
                    // 🚀 Redirección limpia al MainActivity del flujo inicial destruyendo el árbol del Home
                    val intent = Intent(
                        requireContext(),
                        com.example.gamevault.onboarding.MainActivity::class.java
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    activity?.finish()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}