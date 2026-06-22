package dev.openhub.app.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import dev.openhub.app.data.local.OpenHubDBHelper
import dev.openhub.app.data.remote.FirebaseEventosRepository
import dev.openhub.app.model.Evento

class EventRepository private constructor(context: Context) {

    private val dbHelper = OpenHubDBHelper(context.applicationContext)
    private val firebaseRepo = FirebaseEventosRepository()

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    companion object {
        private const val TAG = "EventRepository"

        @Volatile
        private var INSTANCE: EventRepository? = null

        fun getInstance(context: Context): EventRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: EventRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    suspend fun obtenerEventos(): List<Evento> {
        return if (isOnline()) {
            try {
                val eventos = firebaseRepo.obtenerEventos()
                if (eventos.isNotEmpty()) {
                    dbHelper.eliminarEInsertar(eventos)
                }
                eventos
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching from Firestore: ${e.message}", e)
                dbHelper.obtenerTodos()
            }
        } else {
            dbHelper.obtenerTodos()
        }
    }

    fun obtenerDeSQLite(): List<Evento> {
        return dbHelper.obtenerTodos()
    }

    fun obtenerPorId(id: String): Evento? {
        return dbHelper.obtenerPorId(id)
    }

    fun seedIfEmpty() {
        if (dbHelper.contar() > 0) return
        Log.d(TAG, "SQLite empty, inserting seed data...")
        val seed = listOf(
            Evento(id = "seed-1", titulo = "Hackathon Lima 2026: Inteligencia Artificial para el Cambio Social", descripcion = "48 horas de innovacion donde equipos multidisciplinarios crearan soluciones con IA para problematicas sociales reales.", categoria = "hackathon", ubicacion = "Lima, Peru", fecha = "28 jun 2026", horaInicio = "09:00", organizador = "Tech for Good Peru", source = "seed"),
            Evento(id = "seed-2", titulo = "Android Dev Summit Latam: Arquitecturas Modernas y Jetpack", descripcion = "Conferencia de dos dias sobre las ultimas novedades en desarrollo Android, Material Design y arquitecturas escalables.", categoria = "conferencia", ubicacion = "Bogota, Colombia", fecha = "15 jul 2026", horaInicio = "10:00", organizador = "GDG Bogota", source = "seed"),
            Evento(id = "seed-3", titulo = "Taller Practico de Kotlin Multiplatform y Compose", descripcion = "Aprende a compartir logica de negocio entre Android, iOS y Web con Kotlin Multiplatform en este taller hands-on.", categoria = "taller", ubicacion = "Ciudad de Mexico, Mexico", fecha = "03 ago 2026", horaInicio = "14:00", organizador = "KotlinLang MX", source = "seed"),
            Evento(id = "seed-4", titulo = "Cybersecurity Week: Protegiendo el Futuro Digital de Latinoamerica", descripcion = "Una semana de charlas, workshops y CTF sobre ciberseguridad ofensiva y defensiva con expertos internacionales.", categoria = "seguridad", ubicacion = "Santiago, Chile", fecha = "20 ago 2026", horaInicio = "09:00", organizador = "InfoSec Latam", source = "seed"),
            Evento(id = "seed-5", titulo = "Build with AI: The Final Prompt", descripcion = "Cerramos la temporada Build with AI junto a grandes ponentes, Google Developer Experts y la comunidad.", categoria = "inteligencia artificial", ubicacion = "Lima, Peru", fecha = "27 jun 2026", horaInicio = "09:30", organizador = "GDG Callao", source = "seed"),
            Evento(id = "seed-6", titulo = "SecOps Days Lima 2026", descripcion = "SecOps Days Lima es la cumbre de ciberseguridad mas importante de Latinoamerica.", categoria = "seguridad", ubicacion = "Centro de Convenciones ESAN, Lima", fecha = "25 jun 2026", horaInicio = "08:30", organizador = "SecOps Days Lima", source = "seed"),
        )
        dbHelper.insertarEventos(seed)
        Log.d(TAG, "Inserted ${seed.size} seed events")
    }

    private fun isOnline(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
