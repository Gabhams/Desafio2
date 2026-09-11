package com.example.servicios_autenticacion

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.servicios_autenticacion.databinding.ActivityInicioSesionBinding
import com.google.firebase.auth.FirebaseAuth

class InicioSesion : AppCompatActivity() {
    private lateinit var binding: ActivityInicioSesionBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInicioSesionBinding.inflate(layoutInflater)

        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnIniciar.setOnClickListener {
            inciarSesion()
        }

    }

    fun inciarSesion() {
        val correo = binding.etCorreo.text.toString();
        val contrasena = binding.etContrasena.text.toString()

        auth.signInWithEmailAndPassword(correo, contrasena)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val inicio = Intent(this, MainActivity::class.java)
                    startActivity(inicio)
                    finish()
                }
            }.addOnFailureListener { task ->
                Toast.makeText(applicationContext, task.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }
}