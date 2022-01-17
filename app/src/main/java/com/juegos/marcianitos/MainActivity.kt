package com.juegos.marcianitos

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Point
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.juegos.marcianitos.databinding.ActivityMainBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var divContenedor: FrameLayout
    private lateinit var divGuardar: LinearLayout
    private lateinit var divFondoActual : ImageView
    private lateinit var divFondoSiguiente : ImageView
    private lateinit var btnIzq : ImageButton
    private lateinit var btnDer : ImageButton
    private lateinit var btnFuego : ImageButton
    private lateinit var btnSonido : ImageButton
    private lateinit var btnGuardar : Button
    private lateinit var cajaPuntos : TextView

    private var bgActual = 0
    private var bgSiguiente = 0

    private var maximoX = 0f
    private var maximoY = 0f
    private var anchoNave = 0
    private var posicionY = 0f
    private var posicionX = 0f

    private lateinit var nave: LottieAnimationView
    private lateinit var disparos : ArrayList<Disparo>
    private lateinit var malos: ArrayList<Malo>
    private var contadorDisparos = 0
    //private var contadorMalos = 0

    //private var swDisparar = true
    private var swVivo = true
    private var nivel = Constantes.NIVEL_FACIL
    private var puntos = 0

    private lateinit var mpDisparo: MediaPlayer
    private lateinit var mpMalo: MediaPlayer
    private lateinit var mpMuerte: MediaPlayer
    private var swSonido = true

    private val scope = MainScope()

    override fun onDestroy() {
        super.onDestroy()
        // esto la hacemos para parar la rutina
        scope.cancel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        inicializarVariables()

        // si los llamo desde el mismo scope.launch no se ejecuta porque espera a que termine el
        // anterior y como en mi caso dura toda la aplicación no se llegaría a ejecutar
        scope.launch {
            delay(1000)
            crearEnemigos()
        }

        scope.launch {
            animarFondo()
        }

        activarBotones(true)
    }

    private fun inicializarVariables() {
        disparos = arrayListOf()
        malos = arrayListOf()
        divContenedor = binding.divContenedor
        divGuardar = binding.lldivGuardarPuntos
        divFondoActual = binding.bg1
        divFondoSiguiente = binding.bg2

        mpMalo = MediaPlayer.create(this, R.raw.explosion_malo)
        mpMuerte = MediaPlayer.create(this, R.raw.muerte)
        mpDisparo = MediaPlayer.create(this, R.raw.disparos_fuerte)

        btnIzq = binding.btnMovIzq
        btnDer = binding.btnMovDer
        btnFuego = binding.btnFuego
        btnGuardar = binding.btnGuardar
        btnSonido = binding.btnSonido

        cajaPuntos = binding.tvContadorPuntos
        actualizarPuntos(0)

        nave = binding.vNave
        anchoNave = nave.layoutParams.width

        val display = windowManager.defaultDisplay
        val size = Point()
        display.getRealSize(size)
        maximoX = size.x.toFloat()
        maximoY = size.y.toFloat()

        // OJO A ESTA POSICION INICIAL, HAY QUE CONTAR EL INICIO DE LA POSICION EN EL XML (si lo
        // tienes centrado, contaría desde el medio, hay que empezar siempre a 0)
        val posicionInicial = ((maximoX - anchoNave) / 2)
        posicionX = posicionInicial
        nave.x = posicionX
        posicionY = nave.layoutParams.height.toFloat() + 1400
        //posicionY =  maximoY - nave.layoutParams.height.toFloat()

    }

    private fun activarBotones(swActivar : Boolean) {
        if (swActivar) {
            btnIzq.setOnClickListener {
                moverNaveEjeX(Constantes.MOV_IZQUIERDA)
            }

            btnDer.setOnClickListener {
                moverNaveEjeX(Constantes.MOV_DERECHA)
            }

            btnFuego.setOnClickListener {
                fuego()
            }
            btnGuardar.setOnClickListener {
                irAPuntos()
            }

            btnSonido.setOnClickListener {
                cambiarSonido()
            }
        } else {
            btnIzq.isEnabled = false
            btnDer.isEnabled = false
            btnFuego.isEnabled = false
            btnSonido.isEnabled = false
        }
    }

    private fun moverNaveEjeX(lado: Int) {
        if (lado == Constantes.MOV_IZQUIERDA) {
            val nuevaPosicion = posicionX - Constantes.MOV_DISTANCIA
            if (nuevaPosicion  > 0)
                posicionX -= Constantes.MOV_DISTANCIA
        } else if (lado == Constantes.MOV_DERECHA) {
            val nuevaPosicion = posicionX + Constantes.MOV_DISTANCIA
            if (nuevaPosicion < (maximoX - anchoNave))
                posicionX += Constantes.MOV_DISTANCIA
        }
        nave.x = posicionX

        // ANIMACION DE LA NAVE, PERO NO ME GUSTA EL EFECTO
        /*ObjectAnimator.ofFloat(nave,"translationX", positicionX).apply {
            duration = 500
            start()
        }*/
    }

    private fun fuego(){
        //if (swDisparar) {
        //swDisparar = false
        //}
        val ancho = Constantes.DISPARO_ANCHO
        val alto = Constantes.DISPARO_ALTO
        val caja = View(this)
        caja.layoutParams = LinearLayout.LayoutParams(ancho, alto)
        caja.x = posicionX + (anchoNave / 2) - (ancho/2)
        caja.y = posicionY
        caja.setBackgroundResource(R.drawable.disparo)
        divContenedor.addView(caja)

        val nuevoDisparo = Disparo(contadorDisparos++, Constantes.MOV_DISPARO, caja)
        disparos.add(nuevoDisparo)

        sonido(mpDisparo)

        if (disparos.size == 1) {
            scope.launch {
                actualizarDisparos()
            }
        }
    }

    private suspend fun actualizarDisparos() {
        while (disparos.size > 0) {
            delay(300)
            val disparosAEliminar = arrayListOf<Disparo>()
            for (disparo in disparos) {
                val nuevoEjeY = disparo.view.y - Constantes.MOV_DISPARO
                if (nuevoEjeY > 0) {
                    disparo.view.y = nuevoEjeY
                    // TODO comprobar esto
                    //if (nuevoEjeY <= 100) {
                        if (malos.size > 0) {
                            if (comprobarDisparo(disparo)) {
                                divContenedor.removeView(disparo.view)
                                disparosAEliminar.add(disparo)
                            }
                        }
                    //}
                } else {
                    divContenedor.removeView(disparo.view)
                    disparosAEliminar.add(disparo)
                }
            }
            for (disparoAEliminar in disparosAEliminar) {
                disparos.remove(disparoAEliminar)
            }
        }
    }

    private fun comprobarDisparo (disparo: Disparo) : Boolean {
        var acierto = false
        val posicionDisparo = disparo.view.x
        val inicioDisparoY = disparo.view.y
        val finDisparoY = inicioDisparoY + disparo.view.layoutParams.height
        val auxMalosAEliminar = arrayListOf<Malo>()
        for (malo in malos) {
            if (malo.isImpacto(posicionDisparo, inicioDisparoY, finDisparoY)) {
                actualizarPuntos(malo.puntos)
                actualizarNivel()
                explotarMalo(malo.view)
                auxMalosAEliminar.add(malo)
                acierto = true
            }
        }
        for (maloAEliminar in auxMalosAEliminar)
            malos.remove(maloAEliminar)
        return acierto
    }

    // toda la parte de la animación viene de aqui
    // https://www.youtube.com/watch?v=h3ppaE8fBsQ
    // y aquí
    // https://github.com/airbnb/lottie-android
    private fun explotarMalo(naveMalo : LottieAnimationView){
        sonido(mpMalo)
        naveMalo.setAnimation(R.raw.explosion)
        naveMalo.background = null
        naveMalo.repeatCount = 1
        naveMalo.setMaxFrame(3)
        naveMalo.playAnimation()
        naveMalo.animate()
                .alpha(100f)
                .setDuration(900)
                .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            super.onAnimationEnd(animation)
                            divContenedor.removeView(naveMalo)
                        }
                    }
                )
    }

    private suspend fun crearEnemigos() {
        if (swVivo) {
            var tiempo = 0
            var numeroNaves = 1
            if (nivel == Constantes.NIVEL_DIFICIL)
                numeroNaves = 2
            for (i in 0 until numeroNaves) {
                var ancho = 0
                var alto = 0
                var puntos = 0
                if (nivel == Constantes.NIVEL_FACIL) {
                    tiempo = Constantes.ENEMIGO_FACIL_RESPAWN
                    ancho = Constantes.ENEMIGO_FACIL_ANCHO
                    alto = Constantes.ENEMIGO_FACIL_ALTO
                    puntos = Constantes.ENEMIGO_FACIL_PUNTOS
                } else if (nivel == Constantes.NIVEL_MEDIO) {
                    tiempo = Constantes.ENEMIGO_MEDIO_RESPAWN
                    ancho = Constantes.ENEMIGO_MEDIO_ANCHO
                    alto = Constantes.ENEMIGO_MEDIO_ALTO
                    puntos = Constantes.ENEMIGO_MEDIO_PUNTOS
                } else if (nivel == Constantes.NIVEL_INTERESANTE) {
                    tiempo = Constantes.ENEMIGO_INTERESANTE_RESPAWN
                    ancho = Constantes.ENEMIGO_INTERESANTE_ANCHO
                    alto = Constantes.ENEMIGO_INTERESANTE_ALTO
                    puntos = Constantes.ENEMIGO_INTERESANTE_PUNTOS
                } else if (nivel == Constantes.NIVEL_DIFICIL) {
                    tiempo = Constantes.ENEMIGO_DIFICIL_RESPAWN
                    ancho = Constantes.ENEMIGO_DIFICIL_ANCHO
                    alto = Constantes.ENEMIGO_DIFICIL_ALTO
                    puntos = Constantes.ENEMIGO_DIFICIL_PUNTOS
                }
                val cajaMalo = LottieAnimationView(this)
                cajaMalo.layoutParams = LinearLayout.LayoutParams(ancho, alto)
                cajaMalo.setBackgroundResource(R.drawable.nave_malo_f)
                cajaMalo.x = getXParaMalos(ancho)
                cajaMalo.y = Constantes.DISTANCIA_MALO
                divContenedor.addView(cajaMalo)
                val maloNuevo = Malo(puntos, cajaMalo)
                malos.add(maloNuevo)
            }


            if (malos.size == 1) {
                scope.launch {
                    actualizarEnemigos()
                }
            } else if (malos.size == 2 && nivel == Constantes.NIVEL_DIFICIL) {
                scope.launch {
                    actualizarEnemigos()
                }
            }

            delay(tiempo.toLong())
            crearEnemigos()
        }
    }

    private suspend fun actualizarEnemigos() {
        while (malos.size > 0) {
            var movimiento = 0
            var velocidad = 0
            if (nivel == Constantes.NIVEL_FACIL) {
                movimiento = Constantes.ENEMIGO_FACIL_MOVIMIENTO
                velocidad = Constantes.ENEMIGO_FACIL_VELOCIDAD
            } else if (nivel == Constantes.NIVEL_MEDIO) {
                movimiento = Constantes.ENEMIGO_MEDIO_MOVIMIENTO
                velocidad = Constantes.ENEMIGO_MEDIO_VELOCIDAD
            } else if (nivel == Constantes.NIVEL_INTERESANTE) {
                movimiento = Constantes.ENEMIGO_INTERESANTE_MOVIMIENTO
                velocidad = Constantes.ENEMIGO_INTERESANTE_VELOCIDAD
            } else if (nivel == Constantes.NIVEL_DIFICIL) {
                movimiento = Constantes.ENEMIGO_DIFICIL_MOVIMIENTO
                velocidad = Constantes.ENEMIGO_DIFICIL_VELOCIDAD
            }
            delay (velocidad.toLong())
            val malosAEliminar = arrayListOf<Malo>()
            for (malo in malos) {
                val nuevaPosicion = malo.view.y + movimiento
                if (nuevaPosicion < nave.y) {
                    malo.view.y = nuevaPosicion
                } else {
                    if (comprobarMuerte(malo.inicioNaveX(), malo.finNaveX()))
                        muerte()
                    divContenedor.removeView(malo.view)
                    malosAEliminar.add(malo)
                }
            }
            for (maloAEliminar in malosAEliminar) {
                malos.remove(maloAEliminar)
            }

        }
    }

    private fun comprobarMuerte(maloInicioX: Float, maloFinX: Float) : Boolean{
        val inicioNaveX = nave.x
        val finNaveX = nave.x + anchoNave

        // que el inicio este dentro de la nave
        if (maloInicioX >= inicioNaveX && maloInicioX <=finNaveX )
            return true
        // que el fin este dentro de la nave
        if (maloFinX >= inicioNaveX && maloFinX <= finNaveX)
            return true
        // que el disparo sea más grande que la nave
        if (maloInicioX < inicioNaveX && maloFinX > finNaveX)
            return true
        return false
    }

    private suspend fun muerte() {1
        if (swVivo) {
            swVivo = false
            sonido(mpMuerte)
            val divSangre = binding.divSangre
            divSangre.setAnimation(R.raw.sangre)
            // probado a mano hasta que el 31 era el que más me gustaba
            divSangre.setMaxFrame(31)
            divSangre.playAnimation()
            naveEnLlamas()
            for (malo in malos)
                divContenedor.removeView(malo.view)
            activarBotones(false)
            delay(500)
            animacionVisibility(divGuardar, View.VISIBLE, 1000)
        }
    }

    // TODO REVISAR EL Y DE DONDE SE CREA EL DISPARO

    private fun naveEnLlamas() {
        nave.setAnimation(R.raw.explosion)
        nave.repeatCount = LottieDrawable.INFINITE
        nave.playAnimation()
    }

    private suspend fun animarFondo (){
        if (bgActual == 0)
            bgActual = getNumeroFondo(bgActual, bgSiguiente)
        bgSiguiente = getNumeroFondo(bgActual, bgSiguiente)
        divFondoActual.setBackgroundResource(getFondo(bgActual))
        divFondoSiguiente.setBackgroundResource(getFondo(bgSiguiente))
        val bajarSalida = AnimationUtils.loadAnimation(this, R.anim.mover_abajo_saliendo)
        val bajarEntrada = AnimationUtils.loadAnimation(this, R.anim.mover_abajo_entrando)
        // el duration puede ir en el xml, pero para tener en todos los sitios junto
        bajarSalida.duration = Constantes.MOV_BG.toLong()
        bajarEntrada.duration = Constantes.MOV_BG.toLong()
        divFondoActual.startAnimation(bajarSalida)
        divFondoSiguiente.startAnimation(bajarEntrada)
        bgActual = bgSiguiente
        // pongo este tiempo porque es el mismo que hay en los archivos de
        delay(Constantes.MOV_BG.toLong())
        animarFondo()
    }

    private fun getXParaMalos(anchoMalo : Int): Float {
        val auxMaximo = ((maximoX - anchoMalo) - (anchoNave/2) ).toInt()
        val limite = 0..auxMaximo
        val x = limite.random()

        return x.toFloat()
    }

    private fun getNumeroFondo(actual: Int, siguiente: Int): Int {
        var posible : Int
        val random = Random()
        do {
            posible = random.nextInt(5 - 1) + 1
        } while (posible == actual || posible == siguiente)
        return posible
    }

    private fun getFondo (numero: Int): Int {
        val recurso = when (numero) {
            1 -> R.drawable.bg_uno
            2 -> R.drawable.bg_dos
            3 -> R.drawable.bg_tres
            4 -> R.drawable.bg_cuatro
            else -> R.color.black
        }
        return recurso
    }

    private fun actualizarPuntos(nuevo: Int) {
        puntos += nuevo
        val auxPuntos = String.format("%05d", puntos)
        cajaPuntos.text = auxPuntos
    }

    private fun actualizarNivel() {
        when (nivel) {
            Constantes.NIVEL_FACIL -> {
                if (puntos >= Constantes.PUNTOS_NIVEL_FACIL) {
                    nivel = Constantes.NIVEL_MEDIO
                    //Toast.makeText(this, "HAS LLEGADO A MEDIO", Toast.LENGTH_SHORT).show()
                }
            }
            Constantes.NIVEL_MEDIO -> {
                if (puntos >= Constantes.PUNTOS_NIVEL_MEDIO) {
                    nivel = Constantes.NIVEL_INTERESANTE
                    //Toast.makeText(this, "HAS LLEGADO A INTERESANTE", Toast.LENGTH_SHORT).show()
                }
            }
            Constantes.NIVEL_INTERESANTE -> {
                if (puntos >= Constantes.PUNTOS_NIVEL_INTERMEDIO) {
                    nivel = Constantes.NIVEL_DIFICIL
                    //Toast.makeText(this, "HAS LLEGADO A DIFICIL", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun animacionVisibility(view: View, tipo: Int, tiempo: Long) {
        val tipoAnimacion : Animation
        if (tipo == View.VISIBLE) {
            view.visibility = View.VISIBLE
            tipoAnimacion = AnimationUtils.loadAnimation(this, R.anim.visibivility_visible)
        } else {
            if (tipo == View.INVISIBLE)
                view.visibility = View.INVISIBLE
            else if (tipo == View.GONE)
                view.visibility = View.GONE
            tipoAnimacion = AnimationUtils.loadAnimation(this, R.anim.visibivility_invisible)
        }
        tipoAnimacion.duration = tiempo
        view.startAnimation(tipoAnimacion)
    }

    private fun sonido (mp : MediaPlayer){
        if (swSonido)
            mp.start()
    }

    private fun cambiarSonido() {
        swSonido = !swSonido
        if (swSonido) {
            btnSonido.background = getDrawable(R.drawable.btn_sonido_on)
        } else {
            btnSonido.background = getDrawable(R.drawable.btn_sonido_of)
        }
    }

    private fun irAPuntos() {
        val intent = Intent(this, APuntos::class.java)
        var nombre = binding.etNombre.text.toString()
        if (nombre.isEmpty())
            nombre = "NSNC"
        val registro = GuardarPuntos(nombre, puntos)
        ConexionTXT(baseContext).comprobarRegistro(registro)
        intent.putExtra(Constantes.REGISTRO, registro)
        startActivity(intent)
        finish()
    }

}