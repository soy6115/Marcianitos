package com.juegos.marcianitos

import android.widget.Toast
import com.airbnb.lottie.LottieAnimationView

class Malo(val puntos: Int, val view: LottieAnimationView) {

    val anchoNave = view.layoutParams.width
    val altoNave = view.layoutParams.height

    fun inicioNaveX () : Float{
        return view.x
    }

    fun finNaveX() : Float {
        return inicioNaveX() + anchoNave
    }

    private fun inicioNaveY (): Float {
        return view.y
    }

    private fun finNaveY () : Float {
        return inicioNaveY() + altoNave
    }

    private fun comprobarAltura(disparoInicio: Float, disparoFin : Float) : Boolean {
        // que el inicio este dentro de la nave
        if (disparoInicio >= inicioNaveY() && disparoInicio <=finNaveY() )
            return true
        // que el fin este dentro de la nave
        if (disparoFin >= inicioNaveY() && disparoFin <= finNaveY())
            return true
        // que el disparo sea más grande que la nave
        if (disparoInicio < inicioNaveY() && disparoFin > finNaveY())
            return true
        return false
    }

    fun isImpacto(x : Float, yInicio : Float, yFin: Float) : Boolean{
        if ((x >= inicioNaveX() && x <= finNaveX()) /*&& comprobarAltura(yInicio, yFin)*/)
            return true
        return false
    }

}
