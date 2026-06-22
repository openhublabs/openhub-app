package dev.openhub.app.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import dev.openhub.app.model.Evento

class OpenHubDBHelper(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        const val DB_NAME = "openhub.db"
        const val DB_VERSION = 2
        const val TABLE_EVENTOS = "eventos"

        const val COL_ID = "id"
        const val COL_TITULO = "titulo"
        const val COL_DESCRIPCION = "descripcion"
        const val COL_CATEGORIA = "categoria"
        const val COL_UBICACION = "ubicacion"
        const val COL_FECHA = "fecha"
        const val COL_HORA_INICIO = "horaInicio"
        const val COL_HORA_FIN = "horaFin"
        const val COL_ORGANIZADOR = "organizador"
        const val COL_IMAGEN_URL = "imagenUrl"
        const val COL_URL = "url"
        const val COL_SOURCE = "source"
        const val COL_CLIPS = "clips"
        const val COL_TIEMPO_TEXTO = "tiempoTexto"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_EVENTOS (
                $COL_ID TEXT PRIMARY KEY,
                $COL_TITULO TEXT NOT NULL,
                $COL_DESCRIPCION TEXT DEFAULT '',
                $COL_CATEGORIA TEXT DEFAULT '',
                $COL_UBICACION TEXT DEFAULT '',
                $COL_FECHA TEXT DEFAULT '',
                $COL_HORA_INICIO TEXT DEFAULT '',
                $COL_HORA_FIN TEXT DEFAULT '',
                $COL_ORGANIZADOR TEXT DEFAULT '',
                $COL_IMAGEN_URL TEXT DEFAULT '',
                $COL_URL TEXT DEFAULT '',
                $COL_SOURCE TEXT DEFAULT '',
                $COL_CLIPS INTEGER DEFAULT 0,
                $COL_TIEMPO_TEXTO TEXT DEFAULT ''
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EVENTOS")
        onCreate(db)
    }

    fun insertarEvento(evento: Evento): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_ID, evento.id)
            put(COL_TITULO, evento.titulo)
            put(COL_DESCRIPCION, evento.descripcion)
            put(COL_CATEGORIA, evento.categoria)
            put(COL_UBICACION, evento.ubicacion)
            put(COL_FECHA, evento.fecha)
            put(COL_HORA_INICIO, evento.horaInicio)
            put(COL_HORA_FIN, evento.horaFin)
            put(COL_ORGANIZADOR, evento.organizador)
            put(COL_IMAGEN_URL, evento.imagenUrl)
            put(COL_URL, evento.url)
            put(COL_SOURCE, evento.source)
            put(COL_CLIPS, evento.clips)
            put(COL_TIEMPO_TEXTO, evento.tiempoTexto)
        }
        return db.insertWithOnConflict(TABLE_EVENTOS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun insertarEventos(eventos: List<Evento>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            eventos.forEach { insertarEvento(it) }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun eliminarEInsertar(eventos: List<Evento>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_EVENTOS, null, null)
            eventos.forEach { insertarEvento(it) }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun obtenerTodos(): List<Evento> {
        val db = readableDatabase
        val cursor = db.query(TABLE_EVENTOS, null, null, null, null, null, null)
        return cursorToList(cursor)
    }

    fun obtenerPorId(id: String): Evento? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_EVENTOS, null,
            "$COL_ID = ?", arrayOf(id),
            null, null, null
        )
        return cursorToList(cursor).firstOrNull()
    }

    fun buscar(query: String): List<Evento> {
        val db = readableDatabase
        val patron = "%$query%"
        val cursor = db.query(
            TABLE_EVENTOS, null,
            "$COL_TITULO LIKE ? OR $COL_CATEGORIA LIKE ? OR $COL_UBICACION LIKE ?",
            arrayOf(patron, patron, patron),
            null, null, null
        )
        return cursorToList(cursor)
    }

    fun eliminarTodo() {
        val db = writableDatabase
        db.delete(TABLE_EVENTOS, null, null)
    }

    fun contar(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_EVENTOS", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        return count
    }

    private fun cursorToList(cursor: Cursor): List<Evento> {
        val eventos = mutableListOf<Evento>()
        cursor.use { c ->
            while (c.moveToNext()) {
                eventos.add(
                    Evento(
                        id = c.getString(c.getColumnIndexOrThrow(COL_ID)),
                        titulo = c.getString(c.getColumnIndexOrThrow(COL_TITULO)),
                        descripcion = c.getString(c.getColumnIndexOrThrow(COL_DESCRIPCION)),
                        categoria = c.getString(c.getColumnIndexOrThrow(COL_CATEGORIA)),
                        ubicacion = c.getString(c.getColumnIndexOrThrow(COL_UBICACION)),
                        fecha = c.getString(c.getColumnIndexOrThrow(COL_FECHA)),
                        horaInicio = c.getString(c.getColumnIndexOrThrow(COL_HORA_INICIO)),
                        horaFin = c.getString(c.getColumnIndexOrThrow(COL_HORA_FIN)),
                        organizador = c.getString(c.getColumnIndexOrThrow(COL_ORGANIZADOR)),
                        imagenUrl = c.getString(c.getColumnIndexOrThrow(COL_IMAGEN_URL)),
                        url = c.getString(c.getColumnIndexOrThrow(COL_URL)),
                        source = c.getString(c.getColumnIndexOrThrow(COL_SOURCE)),
                        clips = c.getInt(c.getColumnIndexOrThrow(COL_CLIPS)),
                        tiempoTexto = c.getString(c.getColumnIndexOrThrow(COL_TIEMPO_TEXTO))
                    )
                )
            }
        }
        return eventos
    }
}
