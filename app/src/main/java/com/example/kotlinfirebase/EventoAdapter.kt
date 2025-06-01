package com.example.kotlinfirebase

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinfirebase.databinding.ItemEventoBinding
import com.example.kotlinfirebase.model.Evento

class EventoAdapter(
    private val listaEventos: List<Evento>,
    private val onItemClick: (Evento) -> Unit
) : RecyclerView.Adapter<EventoAdapter.EventoViewHolder>() {

    inner class EventoViewHolder(private val binding: ItemEventoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(evento: Evento) {
            binding.nombreEvento.text = evento.nombre
            binding.fechaEvento.text = "Fecha: ${evento.fecha}, hora: ${evento.hora}"
            binding.ubicacionEvento.text = "Lugar: ${evento.ubicacion}"

            if(evento.estado == "cerrado"){
                binding.estadoEvento.text = "Evento ${evento.estado}"
                binding.estadoEvento.setTextColor(itemView.context.getColor(R.color.rojo2))
            }else{
                binding.estadoEvento.text = "Evento disponible"
                binding.estadoEvento.setTextColor(itemView.context.getColor(R.color.verde))
            }


            binding.root.setOnClickListener {
                onItemClick(evento)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val binding = ItemEventoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        holder.bind(listaEventos[position])
    }

    override fun getItemCount(): Int = listaEventos.size
}
