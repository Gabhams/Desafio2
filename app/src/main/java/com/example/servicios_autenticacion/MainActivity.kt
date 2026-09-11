package com.example.servicios_autenticacion

import android.R
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.servicios_autenticacion.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import java.util.TimeZone

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var auth: FirebaseAuth

    // Inicializamos directamente la lista
    private val articulos = mutableListOf<Articulo>()

    private lateinit var mesa: Mesa

    private lateinit var pedido: Pedido

    private lateinit var database: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // Firebase Authentication
        auth = FirebaseAuth.getInstance()

        database = FirebaseDatabase.getInstance().getReference("mesa")

        configurarSpinner()
        configurarBotones()
    }

    override fun onStart() {
        super.onStart()

        cargarMesas()
    }

    // =========================================================
    // SPINNER
    // =========================================================

    private fun configurarSpinner() {

        val estados = listOf(
            "servido",
            "en_progreso"
        )

        val adapter = ArrayAdapter(
            this,
            R.layout.simple_spinner_item,
            estados
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        binding.spPlatillo.adapter = adapter
    }


    // =========================================================
    // BOTONES
    // =========================================================

    private fun configurarBotones() {

        binding.btnAbrirMesa.setOnClickListener {
            abrirMesa()
        }

        binding.btnAgregarArticulo.setOnClickListener {
            agregarPlatillo()
        }

        binding.btnGuardarMesa.setOnClickListener {
            guardarMesa()
        }
    }


    // =========================================================
    // ABRIR MESA
    // =========================================================

    private fun abrirMesa() {

        // =========================
        // ID DE LA MESA
        // =========================

        val id = binding.etMesaId.text
            .toString()
            .trim()

        if (id.isEmpty()) {
            Toast.makeText(
                this,
                "Ingresa el ID de la mesa",
                Toast.LENGTH_SHORT
            ).show()

            binding.etMesaId.requestFocus()
            return
        }

        // =========================
        // COMENSALES
        // =========================

        val comensalesTexto = binding.etComensales.text
            .toString()
            .trim()

        if (comensalesTexto.isEmpty()) {
            Toast.makeText(
                this,
                "Ingresa la cantidad de comensales",
                Toast.LENGTH_SHORT
            ).show()

            binding.etComensales.requestFocus()
            return
        }

        val comensales = comensalesTexto.toIntOrNull()

        if (comensales == null) {
            Toast.makeText(
                this,
                "La cantidad de comensales debe ser un número",
                Toast.LENGTH_SHORT
            ).show()

            binding.etComensales.requestFocus()
            return
        }

        if (comensales <= 0) {
            Toast.makeText(
                this,
                "Debe haber al menos 1 comensal",
                Toast.LENGTH_SHORT
            ).show()

            binding.etComensales.requestFocus()
            return
        }

        // =========================
        // MESERO
        // =========================

        val mesero = auth.currentUser?.displayName
            ?: "no registrado"

        // =========================
        // LIMPIAR ARTÍCULOS
        // =========================

        articulos.clear()

        // =========================
        // HORA DE APERTURA
        // =========================

        val formato = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            Locale.US
        )

        formato.timeZone = TimeZone.getTimeZone("UTC")

        val horaApertura = formato.format(Date())

        // =========================
        // CREAR PEDIDO
        // =========================

        pedido = Pedido(
            hora_apertura = horaApertura,
            articulos = emptyList(),
            total_parcial = 0.0
        )

        // =========================
        // CREAR MESA
        // =========================

        mesa = Mesa(
            mesa_id = id,
            mesero = mesero,
            estado = "ocupada",
            comensales = comensales,
            pedido = pedido,
            cuenta_impresa = false
        )

        actualizarTotal()

        Toast.makeText(
            this,
            "Mesa $id abierta correctamente",
            Toast.LENGTH_SHORT
        ).show()

        println("Mesa abierta:")
        println(mesa)
    }


    // =========================================================
    // AGREGAR PLATILLO
    // =========================================================

    private fun agregarPlatillo() {

        // Verificamos que exista una mesa
        if (!::mesa.isInitialized) {
            Toast.makeText(
                this,
                "Primero debes abrir una mesa",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // =========================
        // PLATILLO
        // =========================

        val platillo = binding.etPlatillo.text
            .toString()
            .trim()

        if (platillo.isEmpty()) {
            Toast.makeText(
                this,
                "Ingresa el nombre del platillo",
                Toast.LENGTH_SHORT
            ).show()
            binding.etPlatillo.requestFocus()
            return
        }

        // =========================
        // CANTIDAD
        // =========================

        val cantidadTexto = binding.etCantidad.text
            .toString()
            .trim()

        if (cantidadTexto.isEmpty()) {
            Toast.makeText(
                this,
                "Ingresa la cantidad",
                Toast.LENGTH_SHORT
            ).show()
            binding.etCantidad.requestFocus()
            return
        }

        val cantidad = cantidadTexto.toIntOrNull()

        if (cantidad == null) {
            Toast.makeText(
                this,
                "La cantidad debe ser un número entero",
                Toast.LENGTH_SHORT
            ).show()
            binding.etCantidad.requestFocus()
            return
        }

        if (cantidad <= 0) {
            Toast.makeText(
                this,
                "La cantidad debe ser mayor que 0",
                Toast.LENGTH_SHORT
            ).show()
            binding.etCantidad.requestFocus()
            return
        }

        // =========================
        // PRECIO
        // =========================

        val precioTexto = binding.etPrecio.text
            .toString()
            .trim()

        if (precioTexto.isEmpty()) {
            Toast.makeText(
                this,
                "Ingresa el precio del platillo",
                Toast.LENGTH_SHORT
            ).show()
            binding.etPrecio.requestFocus()
            return
        }

        val precio = precioTexto.toDoubleOrNull()

        if (precio == null) {
            Toast.makeText(
                this,
                "El precio debe ser un número válido",
                Toast.LENGTH_SHORT
            ).show()
            binding.etPrecio.requestFocus()
            return
        }

        if (precio <= 0) {
            Toast.makeText(
                this,
                "El precio debe ser mayor que 0",
                Toast.LENGTH_SHORT
            ).show()
            binding.etPrecio.requestFocus()
            return
        }

        // =========================
        // NOTAS
        // =========================

        val notas = binding.etNotas.text
            .toString()
            .trim()

        // Las notas pueden quedar vacías,
        // por lo que no necesitan validación.

        // =========================
        // ESTADO
        // =========================

        val estado = binding.spPlatillo
            .selectedItem
            .toString()
            .trim()

        if (estado.isEmpty()) {
            Toast.makeText(
                this,
                "Selecciona un estado para el platillo",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // =========================
        // CREAR ARTÍCULO
        // =========================

        val articulo = Articulo(
            platillo = platillo,
            cantidad = cantidad,
            precio_unitario = precio,
            notas = notas,
            estado = estado
        )

        // Agregamos el artículo a la lista
        articulos.add(articulo)

        // Actualizamos el pedido y la mesa
        actualizarPedido()

        // Limpiamos los campos
        limpiarCamposArticulo()

        // Confirmación
        Toast.makeText(
            this,
            "Platillo agregado correctamente",
            Toast.LENGTH_SHORT
        ).show()
    }





    // =========================================================
    // ACTUALIZAR PEDIDO
    // =========================================================

    private fun actualizarPedido() {

        val total = articulos.sumOf {
            it.cantidad * it.precio_unitario
        }


        pedido = Pedido(
            hora_apertura = pedido.hora_apertura,
            articulos = articulos.toList(),
            total_parcial = total
        )


        // Actualizamos el pedido dentro de la mesa
        mesa = mesa.copy(
            pedido = pedido
        )


        actualizarTotal()
    }


    // =========================================================
    // ACTUALIZAR TOTAL EN LA INTERFAZ
    // =========================================================

    private fun actualizarTotal() {

        val total = articulos.sumOf {
            it.cantidad * it.precio_unitario
        }


        binding.tvTotalParcial.text =
            "Total parcial: $%.2f".format(total)
    }


    // =========================================================
    // LIMPIAR CAMPOS
    // =========================================================

    private fun limpiarCamposArticulo() {

        binding.etPlatillo.text.clear()
        binding.etCantidad.text.clear()
        binding.etPrecio.text.clear()
        binding.etNotas.text.clear()
    }


    // =========================================================
    // GUARDAR MESA
    // =========================================================


    private fun guardarMesa() {

        // Verificar que exista una mesa abierta
        if (!::mesa.isInitialized) {
            Toast.makeText(
                this,
                "No hay ninguna mesa abierta",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // =========================
        // ID DE LA MESA
        // =========================

        val mesaId = binding.etMesaId.text
            .toString()
            .trim()

        if (mesaId.isEmpty()) {
            Toast.makeText(
                this,
                "Ingresa el ID de la mesa",
                Toast.LENGTH_SHORT
            ).show()
            binding.etMesaId.requestFocus()
            return
        }

        // =========================
        // MESERO
        // =========================


        // =========================
        // COMENSALES
        // =========================

        val comensalesTexto = binding.etComensales.text
            .toString()
            .trim()

        if (comensalesTexto.isEmpty()) {
            Toast.makeText(
                this,
                "Ingresa la cantidad de comensales",
                Toast.LENGTH_SHORT
            ).show()
            binding.etComensales.requestFocus()
            return
        }

        val comensales = comensalesTexto.toIntOrNull()

        if (comensales == null) {
            Toast.makeText(
                this,
                "La cantidad de comensales debe ser un número",
                Toast.LENGTH_SHORT
            ).show()
            binding.etComensales.requestFocus()
            return
        }

        if (comensales <= 0) {
            Toast.makeText(
                this,
                "Debe haber al menos un comensal",
                Toast.LENGTH_SHORT
            ).show()
            binding.etComensales.requestFocus()
            return
        }

        // =========================
        // CREAR REFERENCIA FIREBASE
        // =========================

        val nuevaMesaRef = database
            .child("mesas")
            .push()

        val firebaseId = nuevaMesaRef.key

        if (firebaseId == null) {
            Toast.makeText(
                this,
                "No se pudo generar el ID de Firebase",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // =========================
        // ACTUALIZAR MESA
        // =========================

        val mesero = auth.currentUser?.displayName ?: "descnocido"
        mesa = mesa.copy(
            mesa_id = mesaId,
            mesero = mesero,
            comensales = comensales,
            cuenta_impresa = binding.swCuenta.isChecked
        )

        // =========================
        // GUARDAR EN FIREBASE
        // =========================

        nuevaMesaRef.setValue(mesa)
            .addOnSuccessListener {

                Toast.makeText(
                    this,
                    "Mesa guardada correctamente",
                    Toast.LENGTH_SHORT
                ).show()

                println(
                    "Mesa guardada correctamente. " +
                            "ID Firebase: $firebaseId | ID Mesa: $mesaId"
                )

                cargarMesas()
            }
            .addOnFailureListener { error ->

                Toast.makeText(
                    this,
                    "Error al guardar la mesa: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun cargarMesas() {

        val referencia = database.child("mesas")

        referencia.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                val texto = StringBuilder()

                for (mesaSnapshot in snapshot.children) {

                    val mesaId = mesaSnapshot
                        .child("mesa_id")
                        .getValue(String::class.java) ?: ""

                    val mesero = mesaSnapshot
                        .child("mesero")
                        .getValue(String::class.java) ?: ""

                    val comensales = mesaSnapshot
                        .child("comensales")
                        .getValue(Long::class.java) ?: 0L

                    val estado = mesaSnapshot
                        .child("estado")
                        .getValue(String::class.java) ?: ""

                    texto.append("Mesa: $mesaId\n")
                    texto.append("Mesero: $mesero\n")
                    texto.append("Comensales: $comensales\n")
                    texto.append("Estado: $estado\n")
                    texto.append("--------------------\n")
                }

                binding.tvMesasGuardadas.text =
                    if (texto.isNotEmpty()) {
                        texto.toString()
                    } else {
                        "No hay mesas."
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.tvMesasGuardadas.text =
                    "Error: ${error.message}"
            }
        })
    }
}