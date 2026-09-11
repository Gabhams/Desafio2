package com.example.servicios_autenticacion

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.servicios_autenticacion.databinding.ActivityRegistroBinding
import com.google.firebase.auth.FirebaseAuth

class Registro : AppCompatActivity() {
    private lateinit var binding: ActivityRegistroBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        binding = ActivityRegistroBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.btnRegistrar.setOnClickListener {
            registrarse()
        }

        binding.btnOtros.setOnClickListener {
            otrosMetodos()
        }

    }

    fun registrarse() {
        val correo = binding.etCorreo.text.toString();
        val contrasena = binding.etContrasena.text.toString()

        auth.createUserWithEmailAndPassword(correo, contrasena)
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

    private fun otrosMetodos() {
        val metodos = Intent(this, MetodosAutenticacion::class.java)

        startActivity(metodos)
    }
}