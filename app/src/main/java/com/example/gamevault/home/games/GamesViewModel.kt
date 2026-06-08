package com.example.gamevault.home.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamevault.core.ResponseService
import com.example.gamevault.core.model.GameItem
import com.example.gamevault.core.model.network.GameService
import com.example.gamevault.core.repositories.GameRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Estructura de datos específica para el saludo superior
data class UserHeaderData(
    val fullName: String
)

class GamesViewModel(
    private val service: GameService = GameRepository()
) : ViewModel() {

    // Instancias de Firebase integradas
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Flujo 1: Videojuegos (original)
    private val _gamesState = MutableStateFlow<ResponseService<List<GameItem>>?>(null)
    val gamesState: StateFlow<ResponseService<List<GameItem>>?> = _gamesState.asStateFlow()

    // Flujo 2: Encabezado del perfil del usuario (nuevo)
    private val _userState = MutableStateFlow<ResponseService<UserHeaderData>?>(null)
    val userState: StateFlow<ResponseService<UserHeaderData>?> = _userState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadGames()
        // Cargamos los datos en cuanto nace el ciclo de vida del ViewModel
        cargarDatosUsuario()
    }

    fun loadGames() {
        viewModelScope.launch {
            _gamesState.value = ResponseService.Loading
            _gamesState.value = service.listGames(page = 1)
        }
    }

    fun searchGames(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _gamesState.value = ResponseService.Loading
            _gamesState.value = if (query.isBlank()) {
                service.listGames(page = 1)
            } else {
                service.listGames(page = 1, search = query)
            }
        }
    }

    // Consulta exacta a la colección unificada de tu equipo
    fun cargarDatosUsuario() {
        val uid = auth.currentUser?.uid ?: return
        _userState.value = ResponseService.Loading

        firestore.collection("users1").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val firstName = document.getString("firstName") ?: document.getString("name") ?: ""
                    val lastName = document.getString("lastName") ?: ""

                    val nombreCompleto = if (lastName.isNotEmpty()) "$firstName $lastName".trim() else firstName

                    _userState.value = ResponseService.Success(UserHeaderData(nombreCompleto))
                } else {
                    _userState.value = ResponseService.Error("No se encontró el documento")
                }
            }
            .addOnFailureListener { exception ->
                _userState.value = ResponseService.Error(exception.localizedMessage ?: "Error de red")
            }
    }
}