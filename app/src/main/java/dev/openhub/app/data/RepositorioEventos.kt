package dev.openhub.app.data

import dev.openhub.app.R
import dev.openhub.app.model.Evento

object RepositorioEventos {

    private val listaEventos = mutableListOf(
        Evento(
            id = 1,
            titulo = "hackathon lima 2026: inteligencia artificial para el cambio social",
            descripcion = "48 horas de innovacion donde equipos multidisciplinarios crearan soluciones con ia para problematicas sociales reales.",
            categoria = "hackathon",
            ubicacion = "lima, peru",
            fecha = "28 jun 2026",
            horaInicio = "09:00",
            horaFin = "21:00",
            organizador = "tech for good peru",
            imagenRes = R.drawable.evento_placeholder_1,
            clips = 6,
            tiempoTexto = "2 hr"
        ),
        Evento(
            id = 2,
            titulo = "android dev summit latam: arquitecturas modernas y jetpack",
            descripcion = "conferencia de dos dias sobre las ultimas novedades en desarrollo android, material design y arquitecturas escalables.",
            categoria = "conferencia",
            ubicacion = "bogota, colombia",
            fecha = "15 jul 2026",
            horaInicio = "10:00",
            horaFin = "18:00",
            organizador = "gdg bogota",
            imagenRes = R.drawable.evento_placeholder_2,
            clips = 12,
            tiempoTexto = "5 hr"
        ),
        Evento(
            id = 3,
            titulo = "taller practico de kotlin multiplatform y compose",
            descripcion = "aprende a compartir logica de negocio entre android, ios y web con kotlin multiplatform en este taller hands-on.",
            categoria = "taller",
            ubicacion = "ciudad de mexico, mexico",
            fecha = "03 ago 2026",
            horaInicio = "14:00",
            horaFin = "19:00",
            organizador = "kotlinlang mx",
            imagenRes = R.drawable.evento_placeholder_3,
            clips = 3,
            tiempoTexto = "1 dia"
        ),
        Evento(
            id = 4,
            titulo = "cybersecurity week: protegiendo el futuro digital de latinoamerica",
            descripcion = "una semana de charlas, workshops y ctf sobre ciberseguridad ofensiva y defensiva con expertos internacionales.",
            categoria = "conferencia",
            ubicacion = "santiago, chile",
            fecha = "20 ago 2026",
            horaInicio = "09:00",
            horaFin = "17:00",
            organizador = "infosec latam",
            imagenRes = R.drawable.evento_placeholder_4,
            clips = 8,
            tiempoTexto = "3 hr"
        ),
        Evento(
            id = 5,
            titulo = "startup weekend edtech: reinventando la educacion con tecnologia",
            descripcion = "54 horas para crear un prototipo funcional de una startup de tecnologia educativa con mentores de la industria.",
            categoria = "hackathon",
            ubicacion = "buenos aires, argentina",
            fecha = "10 sep 2026",
            horaInicio = "18:00",
            horaFin = "21:00",
            organizador = "techstars buenos aires",
            imagenRes = R.drawable.evento_placeholder_5,
            clips = 4,
            tiempoTexto = "6 hr"
        ),
        Evento(
            id = 6,
            titulo = "cloud native day: kubernetes, microservicios y devops en produccion",
            descripcion = "dia completo de charlas tecnicas sobre infraestructura cloud, contenedores y pipelines de ci/cd para equipos de alto rendimiento.",
            categoria = "conferencia",
            ubicacion = "medellin, colombia",
            fecha = "25 sep 2026",
            horaInicio = "08:30",
            horaFin = "18:30",
            organizador = "cncf medellin",
            imagenRes = R.drawable.evento_placeholder_6,
            clips = 15,
            tiempoTexto = "1 dia"
        )
    )

    fun obtenerTodos(): List<Evento> = listaEventos.toList()

    fun obtenerPorId(id: Int): Evento? = listaEventos.find { it.id == id }

    fun obtenerPorCategoria(categoria: String): List<Evento> =
        listaEventos.filter { it.categoria == categoria }

    fun agregar(evento: Evento) {
        listaEventos.add(evento)
    }

    fun eliminar(id: Int) {
        listaEventos.removeAll { it.id == id }
    }
}
