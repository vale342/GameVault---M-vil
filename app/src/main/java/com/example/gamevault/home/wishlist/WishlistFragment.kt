package com.example.gamevault.home.wishlist

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gamevault.R
import com.example.gamevault.core.WishlistAdapter
import com.example.gamevault.databinding.FragmentWishlistBinding
import kotlinx.coroutines.launch

class WishlistFragment :
    Fragment(R.layout.fragment_wishlist) {

    private var _binding:
            FragmentWishlistBinding? = null

    private val binding
        get() = _binding!!

    private val viewModel:
            WishlistViewModel by viewModels()

    private val adapter =
        WishlistAdapter { game ->

            viewModel.deleteGame(
                game.id
            )
        }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(
            view,
            savedInstanceState
        )

        _binding =
            FragmentWishlistBinding.bind(
                view
            )

        binding.rvWishlist.apply {

            layoutManager =
                LinearLayoutManager(
                    requireContext()
                )

            adapter =
                this@WishlistFragment.adapter
        }

        observeWishlist()

        binding.btnExplorar.setOnClickListener {

            requireActivity()
                .onBackPressedDispatcher
                .onBackPressed()
        }

        viewModel.loadWishlist()
    }

    override fun onResume() {

        super.onResume()

        viewModel.loadWishlist()
    }

    private fun observeWishlist() {

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                viewModel.games.collect { games ->

                    adapter.submitList(
                        games
                    )

                    binding.layoutEmpty.isVisible =
                        games.isEmpty()

                    binding.rvWishlist.isVisible =
                        games.isNotEmpty()

                    binding.tvCount.text =
                        "${games.size} juegos guardados"
                }
            }
        }
    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null
    }
}