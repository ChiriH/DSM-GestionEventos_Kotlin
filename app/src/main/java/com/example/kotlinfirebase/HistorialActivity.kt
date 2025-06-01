package com.example.kotlinfirebase

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kotlinfirebase.databinding.ActivityHistorialBinding
import com.example.kotlinfirebase.model.Evento
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HistorialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistorialBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid ?: ""
    private val listaHistorial = mutableListOf<Evento>()
    private lateinit var adapter: EventoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = EventoAdapter(listaHistorial) { evento ->
            val intent = Intent(this, DetalleEventoActivity::class.java)
            intent.putExtra("eventoId", evento.id)
            startActivity(intent)
        }

        binding.recyclerHistorial.layoutManager = LinearLayoutManager(this)
        binding.recyclerHistorial.adapter = adapter

        cargarHistorial()
    }
    //mostrar historial
    private fun cargarHistorial() {
        db.collection("eventos")
            .whereEqualTo("estado", "cerrado")
            .get()
            .addOnSuccessListener { result ->
                listaHistorial.clear()
                for (doc in result) {
                    val evento = doc.toObject(Evento::class.java)
                    evento.id = doc.id
                    if (evento.confirmados.contains(userId)) {
                        listaHistorial.add(evento)
                    }
                }
                if (listaHistorial.isEmpty()) {
                    binding.txtHistorialVacio.visibility = View.VISIBLE
                } else {
                    binding.txtHistorialVacio.visibility = View.GONE
                }
                adapter.notifyDataSetChanged()
            }
    }
}
