package com.example.kotlinfirebase

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kotlinfirebase.databinding.ActivityEstadisticasBinding
import com.example.kotlinfirebase.ResenaCompleta
import com.google.firebase.firestore.FirebaseFirestore

class EstadisticasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEstadisticasBinding
    private val db = FirebaseFirestore.getInstance()
    private val listaResenas = mutableListOf<ResenaCompleta>()
    private lateinit var adapter: ResenaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEstadisticasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ResenaAdapter(listaResenas)
        binding.recyclerEstadisticas.layoutManager = LinearLayoutManager(this)
        binding.recyclerEstadisticas.adapter = adapter

        cargarEstadisticas()
    }

    private fun cargarEstadisticas() {
        db.collection("resenas")
            .get()
            .addOnSuccessListener { resenas ->
                listaResenas.clear()

                var totalEventos = 0
                var totalConfirmados = 0
                var sumaPuntuacion = 0L
                var totalEncuestas = 0

                val eventosProcesados = mutableSetOf<String>()

                for (resena in resenas) {
                    val eventoId = resena.getString("eventoId") ?: continue
                    val usuarioId = resena.getString("usuarioId") ?: "Anónimo"
                    val puntuacion = resena.getLong("puntuacion") ?: 0

                    sumaPuntuacion += puntuacion
                    totalEncuestas++

                    // Solo obtener el evento si no lo hemos procesado antes
                    db.collection("eventos").document(eventoId).get()
                        .addOnSuccessListener { eventoDoc ->
                            val nombreEvento = eventoDoc.getString("nombre") ?: "Sin nombre"

                            if (!eventosProcesados.contains(eventoId)) {
                                eventosProcesados.add(eventoId)
                                totalEventos++
                                val confirmados = eventoDoc.get("confirmados") as? List<*> ?: emptyList<Any>()
                                totalConfirmados += confirmados.size
                            }

                            val comentario = resena.getString("comentario") ?: ""

                            listaResenas.add(
                                ResenaCompleta(
                                    nombreEvento = nombreEvento,
                                    nombreUsuario = usuarioId, // ID de usuario para reseña
                                    puntuacion = puntuacion,

                                )
                            )
                            adapter.notifyDataSetChanged()

                            // Actualizar estadísticas en pantalla
                            binding.totalEventos.text = "Eventos evaluados: $totalEventos"
                            binding.totalConfirmados.text = "Usuarios confirmados: $totalConfirmados"
                            val promedio = if (totalEncuestas > 0) sumaPuntuacion / totalEncuestas else 0
                            binding.promedioEncuestas.text = "Promedio encuestas: $promedio"
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar estadísticas", Toast.LENGTH_SHORT).show()
            }
    }
}
