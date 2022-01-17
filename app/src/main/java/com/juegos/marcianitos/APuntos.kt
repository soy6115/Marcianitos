package com.juegos.marcianitos

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.juegos.marcianitos.databinding.ActivityPuntosBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class APuntos : AppCompatActivity() {

    private lateinit var binding : ActivityPuntosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPuntosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mostrarDatos()

        binding.btnVolverAJugar.setOnClickListener {
            volverAJugar()
        }

    }

    private fun mostrarDatos() {
        val bundle = intent.extras
        val registro = bundle!!.getSerializable(Constantes.REGISTRO) as GuardarPuntos
        val tops = ConexionTXT(baseContext).obtenerMejoresMarcas()
        val divListado = binding.lldivCompleto
        val divsFilas = divListado.children
        for ((i,divFila) in divsFilas.withIndex()) {
            divFila as LinearLayout
            if (i <= tops.size-1) {
                divFila.visibility = View.VISIBLE
                val registroAux = tops[i]
                val hijos = divFila.children
                for (hijo in hijos) {
                    hijo as TextView
                    if (hijo.tag == "tvNombre")
                        hijo.text = registroAux.nombre
                    else if (hijo.tag == "tvPuntos")
                        hijo.text = registroAux.puntos.toString()
                    if (registro == registroAux)
                        hijo.setTextColor(getColor(R.color.naranja))
                }
            }
        }

    }

    private fun volverAJugar() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}