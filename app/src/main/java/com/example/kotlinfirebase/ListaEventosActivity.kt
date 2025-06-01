package com.example.kotlinfirebase


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kotlinfirebase.databinding.ActivityListaEventosBinding
import com.example.kotlinfirebase.model.Evento
import com.google.firebase.firestore.FirebaseFirestore

class ListaEventosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListaEventosBinding
    private val db = FirebaseFirestore.getInstance()
    private val eventos = mutableListOf<Evento>()
    private lateinit var adapter: EventoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaEventosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = EventoAdapter(eventos) { evento ->
            val intent = Intent(this, DetalleEventoActivity::class.java)
            intent.putExtra("eventoId", evento.id)
            startActivity(intent)
        }

        binding.eventosRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.eventosRecyclerView.adapter = adapter

        cargarEventos()
    }
    override fun onResume() {
        super.onResume()
        cargarEventos() // vuelve a obtener datos de Firestore
    }
    private fun cargarEventos() {
        db.collection("eventos")
            .get()
            .addOnSuccessListener { result ->
                eventos.clear()
                for (document in result) {
                    val evento = document.toObject(Evento::class.java).copy(id = document.id)
                    eventos.add(evento)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar eventos", Toast.LENGTH_SHORT).show()
            }
    }
}
