package com.example.gamevault.home.games

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gamevault.core.model.GameItem
import com.example.gamevault.databinding.ItemGameBinding

class GamesAdapter(
    private val onItemClick: (GameItem) -> Unit = {}
) : ListAdapter<GameItem, GamesAdapter.GameViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val binding = ItemGameBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return GameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GameViewHolder(
        private val binding: ItemGameBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(game: GameItem) {
            binding.tvTitle.text = game.name ?: "Sin título"
            binding.tvRating.text = "★ ${"%.1f".format(game.rating ?: 0.0)}"

            val genres = game.genres
                ?.mapNotNull { it.name }
                ?.take(2)
                ?.joinToString(" • ")
                ?: ""
            binding.tvGenres.text = genres

            val platforms = game.platforms
                ?.mapNotNull { it.platform?.name }
                ?.take(3)
                ?.joinToString(" • ")
                ?: ""
            binding.tvPlatforms.text = platforms

            Glide.with(binding.imgCover)
                .load(game.backgroundImage)
                .centerCrop()
                .placeholder(android.R.color.darker_gray)
                .into(binding.imgCover)

            binding.root.setOnClickListener {
                onItemClick(game)
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<GameItem>() {
            override fun areItemsTheSame(oldItem: GameItem, newItem: GameItem) =
                oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: GameItem, newItem: GameItem) =
                oldItem == newItem
        }
    }
}