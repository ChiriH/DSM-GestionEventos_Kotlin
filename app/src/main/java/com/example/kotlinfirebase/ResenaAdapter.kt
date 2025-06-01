package com.example.kotlinfirebase

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinfirebase.databinding.ItemResenaBinding
import com.example.kotlinfirebase.ResenaCompleta

class ResenaAdapter(private val listaResenas: List<ResenaCompleta>) :
    RecyclerView.Adapter<ResenaAdapter.ResenaViewHolder>() {

    inner class ResenaViewHolder(private val binding: ItemResenaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(resena: ResenaCompleta) {
            binding.nombreEvento.text = "Evento: ${resena.nombreEvento}"
            binding.nombreUsuario.text = "Usuario: ${resena.nombreUsuario}"
            binding.puntuacion.text = "Puntuación: ${resena.puntuacion}"

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResenaViewHolder {
        val binding = ItemResenaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ResenaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResenaViewHolder, position: Int) {
        holder.bind(listaResenas[position])
    }

    override fun getItemCount(): Int = listaResenas.size
}
