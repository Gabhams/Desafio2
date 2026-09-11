package com.example.servicios_autenticacion

data class Pedido(
    val hora_apertura: String,
    val articulos: List<Articulo>,
    val total_parcial: Double
)
