package com.example.kotlinfirebase.model

data class Evento(
    var id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val fecha: String = "",
    val hora: String = "",
    val ubicacion: String = "",
    val organizadorId: String = "",
    val confirmados: List<String> = emptyList(),
    val estado: String = "activo"


)
