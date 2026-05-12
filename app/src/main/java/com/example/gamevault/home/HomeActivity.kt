package com.example.gamevault.home

import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.example.gamevault.R
import com.example.gamevault.databinding.ActivityHomeBinding
import com.example.gamevault.home.dashboard.CarouselAdapter
import com.example.gamevault.home.dashboard.MostPlayedAdapter // Asegúrate de importar tu nuevo adaptador
import com.google.android.material.tabs.TabLayoutMediator

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar los Insets para que no choque con la barra de estado
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- AQUÍ SÍ DEBES LLAMAR A LAS FUNCIONES ---
        binding.btnMenu.setOnClickListener { showCategoriesMenu(it) }
        setupCarousel()
        setupMostPlayed()
    }

    private fun setupCarousel() {
        val images = listOf(R.drawable.animal_crossing, R.drawable.zelda, R.drawable.mario_kart)
        val names = listOf("Animal Crossing", "The Legend of Zelda", "Mario Kart 8")

        binding.viewPagerCarousel.adapter = CarouselAdapter(images, names)

        val transformer = CompositePageTransformer().apply {
            addTransformer(MarginPageTransformer(40))
            addTransformer { page, position ->
                val r = 1 - Math.abs(position)
                page.scaleY = 0.85f + r * 0.15f
            }
        }
        binding.viewPagerCarousel.setPageTransformer(transformer)
        TabLayoutMediator(binding.carouselIndicator, binding.viewPagerCarousel) { _, _ -> }.attach()
    }

    private fun setupMostPlayed() {
        val mpImages = listOf(R.drawable.fortnite, R.drawable.minecraft, R.drawable.valorant, R.drawable.fifa)
        val mpNames = listOf("Fortnite", "Minecraft", "Valorant", "FIFA 24")

        binding.rvMostPlayed.apply {
            adapter = MostPlayedAdapter(mpImages, mpNames)
            layoutManager = LinearLayoutManager(this@HomeActivity, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun showCategoriesMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.menu_categories, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            Toast.makeText(this, "Filtrando: ${item.title}", Toast.LENGTH_SHORT).show()
            true
        }
        popup.show()
    }
}