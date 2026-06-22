package dev.openhub.app.data.remote
import com.google.firebase.firestore.FirebaseFirestore
import dev.openhub.app.model.Evento
import kotlinx.coroutines.tasks.await

class FirebaseEventosRepository {

    private val db = FirebaseFirestore.getInstance()
    private val eventosRef = db.collection("events")

    suspend fun obtenerEventos(): List<Evento> {
        val snapshot = eventosRef.get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toEvento()
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toEvento(): Evento? {
        val data = data ?: return null
        return Evento(
            id = id,
            titulo = data["titulo"] as? String ?: "",
            descripcion = data["descripcion"] as? String ?: "",
            categoria = data["categoria"] as? String ?: "",
            ubicacion = data["ubicacion"] as? String ?: "",
            fecha = data["fecha"] as? String ?: "",
            horaInicio = data["horaInicio"] as? String ?: "",
            horaFin = data["horaFin"] as? String ?: "",
            organizador = data["organizador"] as? String ?: "",
            imagenUrl = data["imagenUrl"] as? String ?: "",
            url = data["url"] as? String ?: "",
            source = data["source"] as? String ?: "",
            clips = (data["clips"] as? Long)?.toInt() ?: 0,
            tiempoTexto = data["tiempoTexto"] as? String ?: ""
        )
    }
}
