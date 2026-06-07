package com.example.gamevault.home.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gamevault.R

class MostPlayedAdapter(
    private val images: List<Int>,
    private val names: List<String>
) : RecyclerView.Adapter<MostPlayedAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.ivGameSmall)
        val txt: TextView = view.findViewById(R.id.tvGameTitleSmall)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_most_played, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.img.setImageResource(images[position])
        holder.txt.text = names[position]
    }

    override fun getItemCount() = images.size
}