package com.example.gamevault.home.gameLists

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.gamevault.databinding.FragmentGameListsBinding // Asegúrate de que el nombre coincida con tu XML

class WishlistFragment : Fragment() {

    private var _binding: FragmentGameListsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameListsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Aquí configurarás la lógica del "Empty State" y el RecyclerView más adelante
        setupUI()
    }

    private fun setupUI() {
        // Según el PDF, aquí va el título "Mi lista" y el buscador local [cite: 485, 488]
        // binding.tvTitle.text = "Mi lista"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}