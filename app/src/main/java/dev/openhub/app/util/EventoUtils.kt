package dev.openhub.app.util

object EventoUtils {

    fun capitalizarPalabras(texto: String): String {
        return texto.split(" ").joinToString(" ") { palabra ->
            palabra.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }

    fun obtenerSaludoDiario(): String {
        val hora = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hora) {
            in 6..11 -> "Buenos días"
            in 12..18 -> "Buenas tardes"
            else -> "Buenas noches"
        }
    }

    fun parseDateToLong(dateStr: String): Long {
        if (dateStr.isBlank()) return 0L
        
        val formats = listOf(
            "dd MMM yyyy",
            "EEEE, MMMM dd, yyyy",
            "EEEE, MMMM dd",
            "EEEE dd/MM",
            "dd/MM/yyyy",
            "dd/MM"
        )
        
        // limpiar la cadena para facilitar el parseo
        val cleanStr = dateStr.replace("  ", " ").trim()
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        
        for (format in formats) {
            try {
                val sdfEs = java.text.SimpleDateFormat(format, java.util.Locale("es", "ES"))
                val date = sdfEs.parse(cleanStr)
                if (date != null) {
                    val cal = java.util.Calendar.getInstance()
                    cal.time = date
                    if (cal.get(java.util.Calendar.YEAR) == 1970) {
                        cal.set(java.util.Calendar.YEAR, currentYear)
                    }
                    return cal.timeInMillis
                }
            } catch (e: Exception) { }
            try {
                val sdfUs = java.text.SimpleDateFormat(format, java.util.Locale.US)
                val date = sdfUs.parse(cleanStr)
                if (date != null) {
                    val cal = java.util.Calendar.getInstance()
                    cal.time = date
                    if (cal.get(java.util.Calendar.YEAR) == 1970) {
                        cal.set(java.util.Calendar.YEAR, currentYear)
                    }
                    return cal.timeInMillis
                }
            } catch (e: Exception) { }
        }
        return 0L
    }
}
