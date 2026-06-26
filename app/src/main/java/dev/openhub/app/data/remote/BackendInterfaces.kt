package dev.openhub.app.data.remote

import dev.openhub.app.model.Evento

interface IEventosRepository {
    suspend fun obtenerEventos(): List<Evento>
    suspend fun obtenerEventoPorId(id: String): Evento?
    suspend fun crearEvento(evento: Evento)
    suspend fun actualizarEvento(evento: Evento)
    suspend fun eliminarEvento(id: String)
}

interface IAuthRepository {
    suspend fun login(email: String, pass: String): Boolean
    suspend fun register(email: String, pass: String): Boolean
    fun logout()
    fun getCurrentUserId(): String?
}

interface IWordleRepository {
    suspend fun validateWord(word: String): Boolean
    suspend fun getDailyTargetWord(): String
    suspend fun saveUserScore(userId: String, attempts: Int)
}
