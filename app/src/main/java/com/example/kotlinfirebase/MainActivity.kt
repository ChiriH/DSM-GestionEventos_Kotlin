package com.example.kotlinfirebase

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlinfirebase.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val userId = auth.currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Mensaje de bienvenida
        val user = Firebase.auth.currentUser
        if (user != null) {
            val displayName = user.displayName
            val email = user.email
            binding.textView.text = "Bienvenido, ${displayName ?: email}"
        }

        // Ocultar botones de admin por defecto
        binding.btnCrearEvento.visibility = View.GONE
        binding.btnEstadisticas.visibility = View.GONE
        binding.btnHistorial.visibility = View.GONE

        verificarRol()
        verificarEventosCambiados()

        binding.btnEventos.setOnClickListener {
            startActivity(Intent(this, ListaEventosActivity::class.java))
        }

        binding.btnHistorial.setOnClickListener {
            startActivity(Intent(this, HistorialActivity::class.java))
        }

        binding.btnCerrarSesion.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Botones exclusivos para admin
        binding.btnCrearEvento.setOnClickListener {
            startActivity(Intent(this, CrearEventoActivity::class.java))
        }
        binding.btnEstadisticas.setOnClickListener {
            startActivity(Intent(this, EstadisticasActivity::class.java))
        }


    }

    private fun verificarRol() {
        db.collection("usuarios").document(userId).get()
            .addOnSuccessListener { document ->
                val rol = document.getString("rol")
                if (rol == "admin") {
                    binding.btnCrearEvento.visibility = View.VISIBLE
                    binding.btnEstadisticas.visibility = View.VISIBLE

                }else{
                    binding.btnHistorial.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al verificar rol", Toast.LENGTH_SHORT).show()
            }
    }

    //Verificar si un evento fue modificado o cerrado para mostrar notificacion
    private fun verificarEventosCambiados() {
        db.collection("eventos")
            .whereArrayContains("confirmados", userId)
            .get()
            .addOnSuccessListener { documentos ->
                for (doc in documentos) {
                    val mensaje = doc.getString("mensajeCambio")
                    if (!mensaje.isNullOrEmpty()) {
                        val nombreEvento = doc.getString("nombre") ?: "evento"
                        val mensajeFinal = "🔔 $nombreEvento: $mensaje"
                        binding.mensajeEvento.text = mensajeFinal
                        binding.mensajeEvento.visibility = View.VISIBLE

                        // Limpiar mensaje para no mostrarlo otra vez
                        db.collection("eventos").document(doc.id)
                            .update("mensajeCambio", "")
                            .addOnFailureListener {
                                Toast.makeText(this, "Error al limpiar mensaje de cambio", Toast.LENGTH_SHORT).show()
                            }
                        break
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al verificar cambios en eventos", Toast.LENGTH_SHORT).show()
            }
    }

}


// Botón para cerrar sesión
// binding.logoutBtn.setOnClickListener {
//     Firebase.auth.signOut()
//   val intent = Intent(this, LoginActivity::class.java)
//   intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//   startActivity(intent)
//     finish()
// }