package com.example.servicios_autenticacion

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.servicios_autenticacion.databinding.ActivityInicioSesionBinding

class InicioSesion : AppCompatActivity() {
    private lateinit var binding: ActivityInicioSesionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInicioSesionBinding.inflate(layoutInflater)
    }
}