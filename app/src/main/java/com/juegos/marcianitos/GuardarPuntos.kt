package com.juegos.marcianitos

import java.io.Serializable

data class GuardarPuntos(val nombre: String, val puntos: Int) : Serializable {

    fun paraGuardar() : String{
        return "$nombre;${puntos.toString()}-"
    }
}
