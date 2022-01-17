package com.juegos.marcianitos

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList

// Clase creada para gestionar desde un único archivo la gestión de jugadores
class ConexionTXT(private val contextWrapper: Context) {

    val nombreArchivo = Constantes.ARCHIVO

    // comprobamos si el resultado mejora los anteriores y lo guardamos
    fun comprobarRegistro(registroNuevo : GuardarPuntos) {
        val mejoresMarcas = obtenerMejoresMarcas()
        if (mejoresMarcas.size == 10) {
            val puntoMasBajo = mejoresMarcas.last().puntos
            if (registroNuevo.puntos > puntoMasBajo) {
                mejoresMarcas.remove(mejoresMarcas.last())
                mejoresMarcas.add(registroNuevo)
            }
        } else
            mejoresMarcas.add(registroNuevo)

        var cadena = ""
        for (registro in mejoresMarcas)
            cadena += registro.paraGuardar()
        cadena = cadena.substring(0, (cadena.length - 1))
        guardarArchivo(cadena)
    }

    // convertimos el contenido del archivo txt en un array de registros
    fun obtenerMejoresMarcas() : ArrayList<GuardarPuntos>{
        val cadena = obtenerTextoArchivo()
        var array = arrayListOf<GuardarPuntos>()
        if (cadena.isNotEmpty()) {
            // Separamos las cadenas de texto que van a representar a cada registro
            val registros = cadena.split(Constantes.REGISTRO_DELIMITADOR)
            for (registro in registros) {
                val datos = registro.split(Constantes.REGISTRO_SEPARADOR)
                val nombre = datos[0]
                val puntos = datos[1].toInt()
                val registroArchivo = GuardarPuntos(nombre, puntos)
                array.add(registroArchivo)
            }
        }
        val listaOrdenada =  array.sortedByDescending { it.puntos }
        val arrayAux = arrayListOf<GuardarPuntos>()
        for (lista in listaOrdenada)
            arrayAux.add(lista)
        return arrayAux
    }

    // obtenemos el contenido del archivo txt y lo devolvemos como cadena para que se pueda
    // transformar en el listado de jugadores
    private fun obtenerTextoArchivo(): String {
        val cadena = StringBuilder()
        try {
            if (contextWrapper.fileList().contains(nombreArchivo)){
                val archivo = InputStreamReader(contextWrapper.openFileInput((nombreArchivo)))
                val br = BufferedReader(archivo)
                var linea = br.readLine()
                while(linea!=null){
                    cadena.append(linea)
                    linea = br.readLine()
                }
                br.close()
                archivo.close()
            }
        } catch (e: IOException) {
            Log.e("ARCHIVO", "Errror al recuperar el archivo")
        }
        return cadena.toString()
    }

    // guardar en el archivo el listado completo de registros
    private fun guardarArchivo(cadena : String) {
        try {
            val archivo = OutputStreamWriter(contextWrapper.openFileOutput(nombreArchivo, AppCompatActivity.MODE_PRIVATE))
            archivo.write(cadena)
            archivo.flush()
            archivo.close()
        } catch (e: IOException){
            Log.e("ARCHIVO", "HA HABIDO UN PROBLEMA EN LA GRABACIÓN DEL ARCHIVO")
        }
    }



}