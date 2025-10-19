package com.javierf.pitstopsparcial.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
data class PitStop(
    val id: Long = 0L,               // 0 = nuevo; >0 = existente (cuando editemos)
    val piloto: String,
    val escuderia: String,
    val tiempoSegundos: Double,      // ejemplo: 2.35
    val compuesto: String,           // Soft, Medium, Hard, Intermediate, Wet
    val neumaticosCambiados: Int,    // 0..4
    val estado: String,              // "OK" | "Fallido"
    val motivoFallo: String?,        // obligatorio si estado = "Fallido"
    val mecanicoPrincipal: String,
    val fechaHora: LocalDateTime     // fecha y hora del pit stop
) : Parcelable
