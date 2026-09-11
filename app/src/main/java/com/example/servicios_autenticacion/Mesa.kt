package com.example.servicios_autenticacion

data class Mesa(
    val mesa_id: String,
    val mesero: String,
    val estado: String,
    val comensales: Int,
    val pedido: Pedido,
    val cuenta_impresa: Boolean
)
