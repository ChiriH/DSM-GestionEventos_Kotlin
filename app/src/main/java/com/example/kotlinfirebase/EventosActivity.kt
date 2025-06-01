package com.example.kotlinfirebase

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kotlinfirebase.databinding.ActivityEventosBinding
import com.example.kotlinfirebase.model.Evento
import com.example.kotlinfirebase.EventoAdapter
import com.google.firebase.firestore.FirebaseFirestore

class EventosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventosBinding
    private val db = FirebaseFirestore.getInstance()
    private val listaEventos = mutableListOf<Evento>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerEventos.layoutManager = LinearLayoutManager(this)

        db.collection("eventos")
            .get()
            .addOnSuccessListener { result ->
                listaEventos.clear()
                for (document in result) {
                    val evento = document.toObject(Evento::class.java).copy(id = document.id)
                    listaEventos.add(evento)
                }

                binding.recyclerEventos.adapter = EventoAdapter(listaEventos) { evento ->

                    val intent = Intent(this, DetalleEventoActivity::class.java)
                    intent.putExtra("eventoId", evento.id)
                    startActivity(intent)
                }
            }
            .addOnFailureListener {

            }
    }
}