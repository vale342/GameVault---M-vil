package com.example.gamevault.home.account

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.gamevault.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import com.bumptech.glide.Glide
import java.io.File

class AccountFragment : Fragment() {

    // Cambiamos a una estrategia de inflado directo para saltarnos cualquier bug del ViewBinding autogenerado
    private var _view: View? = null

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private var wishlistListener: ListenerRegistration? = null
    private var tempCameraUri: Uri? = null

    private val pickMediaLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { subirFotoAFirebase(it) }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            tempCameraUri?.let { subirFotoAFirebase(it) }
        }
    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        if (cameraGranted) {
            abrirCamara()
        } else {
            Toast.makeText(requireContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflamos la vista tradicional usando R.layout para blindar el compilador
        _view = inflater.inflate(R.layout.fragment_account, container, false)
        return _view!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cargarDatosPerfilDinamicos()
        activarEscuchaWishlist()

        // Redirección segura a edición usando findViewById directo
        _view?.findViewById<View>(R.id.cardPersonalInfo)?.setOnClickListener {
            try {
                val intent = Intent(requireContext(), Class.forName("com.example.gamevault.onboarding.personal.EditarPerfilActivity"))
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Pantalla de edición en desarrollo por tu equipo", Toast.LENGTH_SHORT).show()
            }
        }

        // Redirección segura a configuración
        _view?.findViewById<View>(R.id.btnConfiguracion)?.setOnClickListener {
            try {
                val intent = Intent(requireContext(), Class.forName("com.example.gamevault.home.account.ConfiguracionActivity"))
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Pantalla de configuración en desarrollo por tu equipo", Toast.LENGTH_SHORT).show()
            }
        }

        _view?.findViewById<View>(R.id.btnEditPhoto)?.setOnClickListener {
            mostrarOpcionesImagen()
        }

        _view?.findViewById<View>(R.id.btnCerrarSesion)?.setOnClickListener {
            mostrarDialogoCierreSesion()
        }
    }

    private fun cargarDatosPerfilDinamicos() {
        val uid = auth.currentUser?.uid ?: return

        _view?.findViewById<TextView>(R.id.tvUserEmail)?.text = auth.currentUser?.email

        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val firstName = document.getString("firstName") ?: ""
                    val lastName = document.getString("lastName") ?: ""
                    val phone = document.getString("phone") ?: ""
                    val avatarUrl = document.getString("avatar")

                    val nombreCompleto = "$firstName $lastName".trim()

                    _view?.findViewById<TextView>(R.id.tvUserName)?.text = if (nombreCompleto.isNotEmpty()) nombreCompleto else "Usuario"
                    _view?.findViewById<TextView>(R.id.tvNombreCompletoInfo)?.text = if (nombreCompleto.isNotEmpty()) nombreCompleto else "No registrado"
                    _view?.findViewById<TextView>(R.id.tvCelularInfo)?.text = if (phone.isNotEmpty()) phone else "No registrado"

                    val imgAvatar = _view?.findViewById<com.google.android.material.imageview.ShapeableImageView>(R.id.imgAvatar)
                    if (!avatarUrl.isNullOrEmpty() && isAdded && imgAvatar != null) {
                        Glide.with(requireContext())
                            .load(avatarUrl)
                            .placeholder(android.R.drawable.sym_def_app_icon)
                            .centerCrop()
                            .into(imgAvatar)
                    }
                }
            }
    }

    private fun activarEscuchaWishlist() {
        val uid = auth.currentUser?.uid ?: return

        wishlistListener = firestore.collection("favoritos")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener
                if (snapshots != null) {
                    val count = snapshots.size()
                    _view?.findViewById<TextView>(R.id.tvWishlistCount)?.text = "$count títulos"
                }
            }
    }

    private fun mostrarOpcionesImagen() {
        val dialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_select_photo, null)

        val btnCamara = dialogView.findViewById<LinearLayout>(R.id.lnrCamara)
        val btnGaleria = dialogView.findViewById<LinearLayout>(R.id.lnrGaleria)

        btnCamara.setOnClickListener {
            dialog.dismiss()
            verificarPermisosCamara()
        }

        btnGaleria.setOnClickListener {
            dialog.dismiss()
            pickMediaLauncher.launch("image/*")
        }

        dialog.setContentView(dialogView)
        dialog.show()
    }

    private fun verificarPermisosCamara() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            abrirCamara()
        } else {
            requestPermissionsLauncher.launch(arrayOf(Manifest.permission.CAMERA))
        }
    }

    private fun abrirCamara() {
        val file = File(requireContext().cacheDir, "temp_avatar.jpg")
        tempCameraUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )
        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(android.provider.MediaStore.EXTRA_OUTPUT, tempCameraUri)
        }
        takePictureLauncher.launch(intent)
    }

    private fun subirFotoAFirebase(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        Toast.makeText(requireContext(), "Subiendo imagen...", Toast.LENGTH_SHORT).show()

        val ref = storage.reference.child("avatars/$uid.jpg")

        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUri ->
                    val url = downloadUri.toString()

                    firestore.collection("users").document(uid)
                        .update("avatar", url)
                        .addOnSuccessListener {
                            val imgAvatar = _view?.findViewById<com.google.android.material.imageview.ShapeableImageView>(R.id.imgAvatar)
                            if (isAdded && imgAvatar != null) {
                                Glide.with(requireContext()).load(url).centerCrop().into(imgAvatar)
                                Toast.makeText(requireContext(), "Foto actualizada con éxito", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al subir imagen a Storage", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarDialogoCierreSesion() {
        AlertDialog.Builder(requireContext())
            .setTitle("¿Seguro que quieres salir?")
            .setMessage("Cerrarás tu sesión actual en GameVault.")
            .setPositiveButton("Salir") { _, _ ->
                auth.signOut()
                try {
                    val intent = Intent(requireContext(), Class.forName("com.example.gamevault.onboarding.signin.LoginActivity")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    activity?.finish()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error al redirigir a Login", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        wishlistListener?.remove()
        _view = null
    }
}