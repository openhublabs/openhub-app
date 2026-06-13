package dev.openhub.app.model

data class Evento(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val ubicacion: String,
    val fecha: String,
    val horaInicio: String,
    val horaFin: String,
    val organizador: String,
    val imagenRes: Int,
    val clips: Int = 0,
    val tiempoTexto: String = ""
)

data class Categoria(
    val id: Int,
    val nombre: String,
    val icono: String
)

data class Usuario(
    val id: Int,
    val nombre: String,
    val correo: String
)

data class Organizador(
    val id: Int,
    val nombre: String,
    val correo: String
)

data class Inscripcion(
    val id: Int,
    val usuarioId: Int,
    val eventoId: Int,
    val fechaInscripcion: String
)
