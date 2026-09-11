package com.example.servicios_autenticacion

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.servicios_autenticacion.databinding.ActivityMetodosAutenticacionBinding
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class MetodosAutenticacion : AppCompatActivity() {
    private lateinit var binding: ActivityMetodosAutenticacionBinding

    private lateinit var credentialManager: CredentialManager

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMetodosAutenticacionBinding.inflate(layoutInflater)

        auth = FirebaseAuth.getInstance()

        credentialManager = CredentialManager.create(this)

        setContentView(binding.root)

        binding.btnGoogle.setOnClickListener {
            solicitarAutenticacionGoogle()
        }

    }

    fun solicitarAutenticacionGoogle() {
        val googleIDOption = GetGoogleIdOption.Builder()
            .setServerClientId(getString(R.string.default_web_client_id))
            .setFilterByAuthorizedAccounts(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIDOption)
            .build()

        lifecycleScope.launch {
            try {

                val result = credentialManager.getCredential(
                    context = this@MetodosAutenticacion,
                    request = request
                )

                generarIdTokenGoogle(result.credential)

            } catch (e: GetCredentialException) {
                Log.w("google", e.message.toString())
            }
        }
    }

    fun generarIdTokenGoogle(crednecial: Credential) {
        if (crednecial is CustomCredential && crednecial.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(crednecial.data)

            autenticarGoogleFirebase(googleIdTokenCredential.idToken)
        }
    }

    fun autenticarGoogleFirebase(idToken: String) {
        val credencial = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credencial)
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