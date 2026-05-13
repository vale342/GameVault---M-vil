package com.example.gamevault.home

import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.example.gamevault.R
import com.example.gamevault.databinding.ActivityHomeBinding
import com.example.gamevault.home.dashboard.CarouselAdapter
import com.example.gamevault.home.dashboard.MostPlayedAdapter
import com.google.android.material.tabs.TabLayoutMediator

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ajuste de paddings para evitar colisiones con barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Aplicamos padding superior para el status bar,
            // pero dejamos que el BottomNav maneje su propio espacio inferior
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)

            // Opcional: Si quieres que el BottomNav tenga el padding correcto:
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            binding.bottomNav.setPadding(0, 0, 0, navBars.bottom)

            insets
        }

        // 1. Configuración de Identidad (Design Doc)
        setupHeader()

        // 2. Configuración de Secciones
        setupCarousel()      // Carrusel "Explorar Juegos"
        setupMostPlayed()    // Lista horizontal "Lo más jugado"
        setupTendencies()    // Grid de 2 columnas "Tendencia ahora"

        // 3. Navegación Inferior
        setupBottomNavigation()
    }

    private fun setupHeader() {
        // Valores extraídos del Design Doc
        binding.tvGreeting.text = "Hola,"
        binding.tvUserName.text = "Diego 👋"

        // El avatar ya está configurado en el XML con el estilo CircleImage
        binding.imgAvatar.setOnClickListener {
            Toast.makeText(this, "Perfil de usuario", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCarousel() {
        val images = listOf(R.drawable.animal_crossing, R.drawable.zelda, R.drawable.mario_kart)
        val names = listOf("Animal Crossing", "The Legend of Zelda", "Mario Kart 8")

        binding.viewPagerCarousel.adapter = CarouselAdapter(images, names)

        // Efecto visual de Zoom (Design Doc: 180dp de alto)
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
            setHasFixedSize(true)
        }
    }

    private fun setupTendencies() {
        // Datos basados en la API RAWG mencionada en el Design Doc
        val games = listOf("Elden Ring", "Hollow Knight", "Cyberpunk 2077", "Hades")

        binding.rvGamesHome.apply {
            // Requerimiento: GridLayoutManager con 2 columnas
            layoutManager = GridLayoutManager(this@HomeActivity, 2)

            // Aquí asignarás tu GameAdapter para la cuadrícula
            // adapter = GameAdapter(games)

            setHasFixedSize(true)
            isNestedScrollingEnabled = false // Optimización para scroll fluido dentro de NestedScrollView
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> true
                R.id.nav_lista -> {
                    Toast.makeText(this, "Lista de deseos", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_cuenta -> {
                    Toast.makeText(this, "Mi Cuenta", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }
}