package com.example.gamevault.home.dashboard // Asegúrate de que este sea tu paquete real

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gamevault.R

class CarouselAdapter(
    private val images: List<Int>,
    private val names: List<String>
) : RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder>() {

    // 1. Esta es la clase que sostiene la vista
    class CarouselViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.ivCarouselItem)
        val textView: TextView = view.findViewById(R.id.tvGameName)
    }

    // 2. Aquí es donde se "infla" el diseño de cada item (el que te faltaba)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carousel, parent, false)
        return CarouselViewHolder(view)
    }

    // 3. Aquí se conecta la información (imagen y nombre) con la vista
    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        holder.imageView.setImageResource(images[position])
        holder.textView.text = names[position]
    }

    // 4. Indica cuántos elementos hay
    override fun getItemCount(): Int = images.size
}