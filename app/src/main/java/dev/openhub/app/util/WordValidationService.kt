package dev.openhub.app.util

import dev.openhub.app.model.Evento
import java.text.Normalizer

object WordValidationService {

    // Lista ampliada de palabras comunes en español
    private val COMMON_WORDS = setOf(
        "AUDIO", "PISTA", "LENTE", "PAPEL", "COSAS", "PERRO", "GATOS", "HACER", "DECIR", "PODER", 
        "TENER", "ESTAR", "SERIA", "AHORA", "ANTES", "DESDE", "HASTA", "DONDE", "QUIEN", "MISMO", 
        "NUEVO", "MAYOR", "MENOR", "MEJOR", "POCOS", "TODOS", "ESTOS", "AQUEL", "ENTRE", "SOBRE", 
        "SEGUN", "LUEGO", "MUCHO", "MUNDO", "RUIDO", "LETRA", "PARTE", "LLAVE", "NOTAS", "SALUD",
        "PUNTO", "LUGAR", "TIEMPO", "FORMA", "GRUPO", "HECHO", "CASAS", "CALLE", "CIUDAD", "PAIS",
        "AGUAS", "AIRE", "FUEGO", "LUZ", "VIDA", "AMOR", "ODIO", "PAZ",
        "MUJER", "JOVEN", "VIEJO", "AMIGO", "PADRE", "MADRE", "HIJO",
        "HIJA", "CASA", "PLAZA", "ARBOL", "FLOR", "GATO", "COCHE", "TREN",
        "AVION", "BARCO", "MOTO", "LIBRO", "LAPIZ", "RADIO", "MUSEO", "ARTE",
        "SALUD", "CITA"
    ).filter { it.length == 5 }.toSet()

    /**
     * Extrae palabras temáticas de 5 letras de los títulos y descripciones de los eventos actuales.
     */
    fun extractThematicWords(eventos: List<Evento>): Set<String> {
        val thematicWords = mutableSetOf<String>()
        val regex = Regex("\\b[a-záéíóúñ]{5}\\b", RegexOption.IGNORE_CASE)
        
        eventos.forEach { evento ->
            val text = "${evento.titulo} ${evento.descripcion}"
            regex.findAll(text).forEach { match ->
                val word = removeAccents(match.value.uppercase())
                thematicWords.add(word)
            }
        }
        
        // Agregar algunas temáticas fijas por si acaso
        thematicWords.addAll(setOf("SISMO", "FUEGO", "GOLPE", "VIRUS", "CALOR", "ARMAS", "BALAS", "LUCHA", "VUELO", "CAIDA", "ROBOS", "PACTO", "VOTOS", "RETOS", "CASOS", "DATOS", "LEYES", "VIAJE"))
        return thematicWords
    }

    /**
     * Valida si una palabra ingresada es válida.
     * Combina palabras extraídas dinámicamente con palabras comunes, pero usando heurística de vocales
     * para rechazar palabras "basura" (ej. "WWWWW"). En un entorno real, aquí se consultaría la API de Firebase.
     */
    fun isValidWord(word: String, thematicWords: Set<String>): Boolean {
        if (word.length != 5) return false
        if (thematicWords.contains(word) || COMMON_WORDS.contains(word)) return true
        
        // Heurística básica: Debe contener al menos una vocal
        val hasVowel = word.any { it in "AEIOU" }
        // Heurística: No debe tener más de 3 consonantes seguidas (ej. "RTSDF")
        val tooManyConsonants = word.contains(Regex("[^AEIOU]{4,}"))
        
        // Si pasa las heurísticas básicas, asumimos que es una palabra "que existe" para no bloquear al usuario,
        // delegando la validación profunda al backend en el futuro.
        // TODO: (Para el equipo backend) Conectar esto con una API de diccionario o colección Firestore de palabras válidas.
        return hasVowel && !tooManyConsonants
    }

    private fun removeAccents(str: String): String {
        val normalized = Normalizer.normalize(str, Normalizer.Form.NFD)
        return normalized.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
    }
}
