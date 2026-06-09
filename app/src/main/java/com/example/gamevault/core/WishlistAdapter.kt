package com.example.gamevault.core

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gamevault.core.database.WishEntity
import com.example.gamevault.databinding.ItemWishlistBinding

class WishlistAdapter(
    private val onDelete: ((WishEntity) -> Unit)? = null
) : ListAdapter<
        WishEntity,
        WishlistAdapter.ViewHolder>(
    DiffCallback()
) {

    class ViewHolder(
        private val binding:
        ItemWishlistBinding,
        private val onDelete:
        ((WishEntity) -> Unit)?
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        fun bind(game: WishEntity) {
            binding.tvTitle.text = game.name

            // 1. Vincula el RatingBar (la calificación visual)
            binding.rbRating.rating = game.rating.toFloat()

            // 2. Vincula el TextView con el número
            binding.tvRating.text = game.rating.toString()

            Glide.with(binding.root)
                .load(game.image)
                .centerCrop()
                .into(binding.ivGame)

            binding.btnDelete.setOnClickListener {
                onDelete?.invoke(game)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        return ViewHolder(

            ItemWishlistBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            ),

            onDelete
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        holder.bind(
            getItem(position)
        )
    }

    class DiffCallback :
        DiffUtil.ItemCallback<
                WishEntity>() {

        override fun areItemsTheSame(
            oldItem: WishEntity,
            newItem: WishEntity
        ): Boolean {

            return oldItem.id ==
                    newItem.id
        }

        override fun areContentsTheSame(
            oldItem: WishEntity,
            newItem: WishEntity
        ): Boolean {

            return oldItem ==
                    newItem
        }
    }
}