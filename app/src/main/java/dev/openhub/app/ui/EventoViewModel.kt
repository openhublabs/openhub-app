package dev.openhub.app.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dev.openhub.app.OpenHubApp
import dev.openhub.app.model.Evento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EventoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as OpenHubApp).eventRepository

    private val _eventos = MutableLiveData<List<Evento>>()
    val eventos: LiveData<List<Evento>> = _eventos

    private val _eventoSeleccionado = MutableLiveData<Evento?>()
    val eventoSeleccionado: LiveData<Evento?> = _eventoSeleccionado

    private val _cargando = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    init {
        cargarEventos()
    }

    fun cargarEventos() {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val eventos = withContext(Dispatchers.IO) {
                    repository.obtenerEventos()
                }
                _eventos.value = eventos
            } catch (e: Exception) {
                Log.e("EventoViewModel", "Error loading events: ${e.message}", e)
                val locales = withContext(Dispatchers.IO) {
                    repository.obtenerDeSQLite()
                }
                _eventos.value = locales
            } finally {
                _cargando.value = false
            }
        }
    }

    fun seleccionarEvento(evento: Evento?) {
        _eventoSeleccionado.value = evento
    }

    fun seleccionarEvento(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val evento = repository.obtenerPorId(id)
            withContext(Dispatchers.Main) {
                _eventoSeleccionado.value = evento
            }
        }
    }
}
