package com.example.gamevault.home.games

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.gamevault.R
import com.example.gamevault.core.FragmentCommunicator
import com.example.gamevault.core.ResponseService
import com.example.gamevault.databinding.FragmentGamesBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class GamesFragment : Fragment() {

    private var _binding: FragmentGamesBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<GamesViewModel>()
    private lateinit var communicator: FragmentCommunicator

    private val gamesAdapter = GamesAdapter { game ->
        val bundle = bundleOf("game" to game)
        findNavController().navigate(R.id.action_gamesFragment_to_gameDetailFragment, bundle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGamesBinding.inflate(inflater, container, false)
        communicator = requireActivity() as FragmentCommunicator
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        setupSwipeRefresh()
        observeState()
    }

    private fun setupRecyclerView() {
        binding.rvGames.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = gamesAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.searchGames(s?.toString() ?: "")
            }
        })
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeColors(
            requireContext().getColor(R.color.primary)
        )
        binding.swipeRefresh.setProgressBackgroundColorSchemeColor(
            requireContext().getColor(R.color.primary_dark)
        )
        binding.swipeRefresh.setOnRefreshListener {
            binding.etSearch.text?.clear()
            viewModel.loadGames()
            viewModel.cargarDatosUsuario()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // 1. 🚀 Escucha Concurrente: Datos de Usuario y Foto de Perfil
                launch {
                    viewModel.userState.collect { state ->
                        when (state) {
                            is ResponseService.Loading -> {
                                binding.tvUserName.text = "Cargando..."
                            }
                            is ResponseService.Success -> {
                                val datos = state.data
                                binding.tvUserName.text = "${datos.fullName} 👋"

                                // Solución de contexto limpia usando requireContext() e importando Glide
                                Glide.with(requireContext())
                                    .load(android.R.drawable.sym_def_app_icon)
                                    .centerCrop()
                                    .into(binding.imgAvatar)
                            }
                            is ResponseService.Error -> {
                                binding.tvUserName.text = "Gamer 👋"
                                binding.imgAvatar.setImageResource(android.R.drawable.sym_def_app_icon)
                            }
                            null -> {}
                        }
                    }
                }

                // 2. 🎮 Escucha Concurrente: Listado de videojuegos original
                launch {
                    viewModel.gamesState.collect { state ->
                        when (state) {
                            is ResponseService.Loading -> {
                                communicator.manageLoader(true)
                                showEmpty(false)
                            }
                            is ResponseService.Success -> {
                                communicator.manageLoader(false)
                                binding.swipeRefresh.isRefreshing = false
                                val games = state.data
                                gamesAdapter.submitList(games)
                                showEmpty(games.isEmpty())
                            }
                            is ResponseService.Error -> {
                                communicator.manageLoader(false)
                                binding.swipeRefresh.isRefreshing = false
                                showEmpty(false)
                                Snackbar.make(binding.root, state.error, Snackbar.LENGTH_LONG).show()
                            }
                            null -> {}
                        }
                    }
                }

            }
        }
    }

    private fun showEmpty(show: Boolean) {
        binding.layoutEmpty.visibility = if (show) View.VISIBLE else View.GONE
        binding.rvGames.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}