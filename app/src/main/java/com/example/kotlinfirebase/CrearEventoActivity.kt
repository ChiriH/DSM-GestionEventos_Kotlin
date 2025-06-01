package com.example.kotlinfirebase

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kotlinfirebase.databinding.ActivityCrearEventoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CrearEventoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrearEventoBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearEventoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCrearEvento.setOnClickListener {
            val nombre = binding.nombreEvento.text.toString().trim()
            val descripcion = binding.descripcionEvento.text.toString().trim()
            val fecha = binding.fechaEvento.text.toString().trim()
            val hora = binding.horaEvento.text.toString().trim()
            val ubicacion = binding.ubicacionEvento.text.toString().trim()

            if (nombre.isEmpty() || descripcion.isEmpty() || fecha.isEmpty() || hora.isEmpty() || ubicacion.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val evento = hashMapOf(
                "nombre" to nombre,
                "descripcion" to descripcion,
                "fecha" to fecha,
                "hora" to hora,
                "ubicacion" to ubicacion,
                "organizadorId" to auth.currentUser?.uid,
                "confirmados" to emptyList<String>(), // lista vacía para los que confirmen asistencia
                "estado" to "activo"
            )

            db.collection("eventos")
                .add(evento)
                .addOnSuccessListener {
                    Toast.makeText(this, "Evento creado correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al crear el evento", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
