package dev.openhub.app.util

import dev.openhub.app.R
import dev.openhub.app.model.Evento

object EventoUtils {

    fun capitalizarPalabras(texto: String): String {
        return texto.split(" ").joinToString(" ") { palabra ->
            palabra.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }

    fun capitalizarPrimeraLetra(texto: String): String {
        return texto.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    fun colorChipParaCategoria(categoria: String): Int {
        return when (categoria.lowercase()) {
            "hackathon" -> R.color.color_chip_hackathon
            "conferencia" -> R.color.color_chip_conferencia
            "taller" -> R.color.color_chip_taller
            else -> R.color.color_etiqueta_fondo
        }
    }

    fun capitalizarUbicacion(ubicacion: String): String {
        return ubicacion.split(",").joinToString(", ") { capitalizarPalabras(it.trim()) }
    }
}
