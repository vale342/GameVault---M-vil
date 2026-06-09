package com.example.gamevault.home.gameDetail

import android.graphics.Outline
import android.os.Bundle
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.gamevault.R
import com.example.gamevault.core.FragmentCommunicator
import com.example.gamevault.core.ResponseService
import com.example.gamevault.core.model.Screenshot
import com.example.gamevault.databinding.FragmentGameDetailBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class GameDetailFragment : Fragment(R.layout.fragment_game_detail) {

    private var _binding: FragmentGameDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GameDetailViewModel by viewModels()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        _binding =
            FragmentGameDetailBinding.bind(view)

        setupListeners()
        observeState()
        observeWishlist()

        arguments
            ?.getString("gameId")
            ?.let {
                viewModel.fetchGameDetail(it)
            }
    }

    private fun setupListeners() {

        binding.btnBack.setOnClickListener {
            requireActivity()
                .onBackPressedDispatcher
                .onBackPressed()
        }

        val saveGame = {

            val game =
                (viewModel.detailState.value
                        as? ResponseService.Success)
                    ?.data

            game?.let {
                viewModel.toggleWishlist(it)
            }
        }

        binding.btnWishToggle.setOnClickListener {
            saveGame()
        }

        binding.btnWishlist.setOnClickListener {
            saveGame()
        }
    }

    private fun observeWishlist() {

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                viewModel.isSaved.collect { saved ->

                    binding.btnWishToggle.setImageResource(
                        if (saved)
                            R.drawable.ic_heart
                        else
                            R.drawable.ic_heart
                    )
                }
            }
        }
    }

    private fun observeState() {

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                viewModel.detailState.collect { response ->

                    when (response) {

                        is ResponseService.Loading -> {

                            (activity as? FragmentCommunicator)
                                ?.manageLoader(true)
                        }

                        is ResponseService.Success -> {

                            (activity as? FragmentCommunicator)
                                ?.manageLoader(false)

                            val game =
                                response.data
                                    ?: return@collect

                            Glide
                                .with(this@GameDetailFragment)
                                .load(game.backgroundImage)
                                .centerCrop()
                                .into(binding.ivGameCover)

                            binding.tvTitle.text =
                                game.name

                            binding.tvAbout.text =
                                game.descriptionRaw
                                    ?: "Sin descripción"

                            binding.tvDeveloper.text =
                                game.developers
                                    ?.firstOrNull()
                                    ?.name
                                    ?: "N/A"

                            binding.tvPlaytime.text =
                                "${game.playtime ?: 0} hrs"

                            binding.rbRating.rating =
                                game.rating
                                    ?.toFloat()
                                    ?: 0f

                            binding.tvRating.text =
                                game.rating
                                    ?.toString()
                                    ?: "0.0"

                            binding.tvReviews.text =
                                "(${game.metacritic ?: 0} votos)"

                            loadChips(
                                game.genres
                                    ?.map {
                                        it.name ?: ""
                                    },
                                binding.cgGenres
                            )

                            loadChips(
                                game.platforms
                                    ?.map {
                                        it.platform?.name ?: ""
                                    },
                                binding.cgPlatforms
                            )

                            loadGallery(
                                game.shortScreenshots
                            )
                        }

                        is ResponseService.Error -> {

                            (activity as? FragmentCommunicator)
                                ?.manageLoader(false)

                            Toast
                                .makeText(
                                    requireContext(),
                                    response.error,
                                    Toast.LENGTH_LONG
                                )
                                .show()
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun loadGallery(
        screenshots: List<Screenshot>?
    ) {

        binding.llGallery.removeAllViews()

        if (screenshots.isNullOrEmpty())
            return

        val density =
            resources.displayMetrics.density

        screenshots.forEach { screenshot ->

            val image =
                ImageView(requireContext()).apply {

                    layoutParams =
                        LinearLayout.LayoutParams(
                            (250 * density).toInt(),
                            (150 * density).toInt()
                        ).apply {

                            marginEnd =
                                (12 * density).toInt()
                        }

                    scaleType =
                        ImageView.ScaleType.CENTER_CROP

                    outlineProvider =
                        object : ViewOutlineProvider() {

                            override fun getOutline(
                                view: View,
                                outline: Outline
                            ) {

                                outline.setRoundRect(
                                    0,
                                    0,
                                    view.width,
                                    view.height,
                                    (12 * density)
                                )
                            }
                        }

                    clipToOutline = true
                }

            Glide
                .with(this)
                .load(screenshot.image)
                .into(image)

            binding.llGallery.addView(image)
        }
    }

    private fun loadChips(
        items: List<String>?,
        group: com.google.android.material.chip.ChipGroup
    ) {

        group.removeAllViews()

        items?.forEach { text ->

            val chip =
                Chip(requireContext()).apply {

                    this.text = text

                    setChipBackgroundColor(
                        ContextCompat.getColorStateList(
                            requireContext(),
                            R.color.purple_500
                        )
                    )

                    setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            android.R.color.white
                        )
                    )
                }

            group.addView(chip)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}