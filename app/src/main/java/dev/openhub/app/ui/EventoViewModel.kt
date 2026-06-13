package dev.openhub.app.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.openhub.app.data.RepositorioEventos
import dev.openhub.app.model.Evento

class EventoViewModel : ViewModel() {

    private val _eventos = MutableLiveData<List<Evento>>()
    val eventos: LiveData<List<Evento>> = _eventos
    
    private val _eventoSeleccionado = MutableLiveData<Evento?>()
    val eventoSeleccionado: LiveData<Evento?> = _eventoSeleccionado

    init {
        cargarEventos()
    }

    fun cargarEventos() {
        _eventos.value = RepositorioEventos.obtenerTodos()
    }

    fun seleccionarEvento(evento: Evento?) {
        _eventoSeleccionado.value = evento
    }

    fun seleccionarEvento(id: Int) {
        _eventoSeleccionado.value = RepositorioEventos.obtenerPorId(id)
    }

    fun filtrarPorCategoria(categoria: String) {
        _eventos.value = RepositorioEventos.obtenerPorCategoria(categoria)
    }
}
