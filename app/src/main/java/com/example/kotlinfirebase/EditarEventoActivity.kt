package com.example.kotlinfirebase

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlinfirebase.databinding.ActivityEditarEventoBinding
import com.google.firebase.firestore.FirebaseFirestore

class EditarEventoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditarEventoBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var eventoId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarEventoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        eventoId = intent.getStringExtra("eventoId") ?: ""

        cargarDatosEvento()

        binding.btnGuardarCambios.setOnClickListener {
            guardarCambios()
        }
    }

    private fun cargarDatosEvento() {
        db.collection("eventos").document(eventoId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    binding.editNombre.setText(document.getString("nombre"))
                    binding.editFecha.setText(document.getString("fecha"))
                    binding.editUbicacion.setText(document.getString("ubicacion"))
                    binding.editHora.setText(document.getString("hora"))
                } else {
                    Toast.makeText(this, "Evento no encontrado", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar evento", Toast.LENGTH_SHORT).show()
            }
    }

    private fun guardarCambios() {
        val nombre = binding.editNombre.text.toString()
        val fecha = binding.editFecha.text.toString()
        val ubicacion = binding.editUbicacion.text.toString()
        val hora = binding.editHora.text.toString()

        if (nombre.isBlank() || fecha.isBlank() || ubicacion.isBlank()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val cambios = mapOf(
            "nombre" to nombre,
            "fecha" to fecha,
            "ubicacion" to ubicacion,
            "hora" to hora
        )

        db.collection("eventos").document(eventoId)
            .update(cambios)
            .addOnSuccessListener {
                Toast.makeText(this, "Evento actualizado", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al actualizar evento", Toast.LENGTH_SHORT).show()
            }
        val resultIntent = Intent()
        resultIntent.putExtra("cambiosGuardados", true)
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}
