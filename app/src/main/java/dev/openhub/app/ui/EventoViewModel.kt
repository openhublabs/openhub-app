package dev.openhub.app.ui

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
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
    private val sharedPrefs: SharedPreferences = application.getSharedPreferences("openhub_prefs", Context.MODE_PRIVATE)

    private val _eventos = MutableLiveData<List<Evento>>()
    val eventos: LiveData<List<Evento>> = _eventos

    private val _eventoSeleccionado = MutableLiveData<Evento?>()
    val eventoSeleccionado: LiveData<Evento?> = _eventoSeleccionado

    private val _cargando = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    private val _favoritos = MutableLiveData<Set<String>>(emptySet())
    val favoritos: LiveData<Set<String>> = _favoritos

    private val _historial = MutableLiveData<List<String>>(emptyList())
    val historial: LiveData<List<String>> = _historial

    init {
        cargarFavoritos()
        cargarHistorial()
        cargarEventos()
    }

    private fun cargarHistorial() {
        val histStr = sharedPrefs.getString("historial_csv", "") ?: ""
        if (histStr.isNotEmpty()) {
            _historial.value = histStr.split(",")
        }
    }

    fun agregarAHistorial(eventoId: String) {
        val currentList = _historial.value?.toMutableList() ?: mutableListOf()
        currentList.remove(eventoId)
        currentList.add(0, eventoId)
        val limited = currentList.take(20)
        _historial.value = limited
        sharedPrefs.edit().putString("historial_csv", limited.joinToString(",")).apply()
    }

    private fun cargarFavoritos() {
        val favs = sharedPrefs.getStringSet("favoritos_set", emptySet()) ?: emptySet()
        _favoritos.value = favs
    }

    fun toggleFavorito(eventoId: String) {
        val currentFavs = _favoritos.value?.toMutableSet() ?: mutableSetOf()
        if (currentFavs.contains(eventoId)) {
            currentFavs.remove(eventoId)
        } else {
            currentFavs.add(eventoId)
        }
        _favoritos.value = currentFavs
        sharedPrefs.edit().putStringSet("favoritos_set", currentFavs).apply()
    }

    fun isFavorito(eventoId: String): Boolean {
        return _favoritos.value?.contains(eventoId) == true
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
        viewModelScope.launch {
            val evento = withContext(Dispatchers.IO) {
                repository.obtenerPorId(id)
            }
            _eventoSeleccionado.value = evento
        }
    }
}
