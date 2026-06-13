package dev.openhub.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dev.openhub.app.R
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dev.openhub.app.adapter.EventoAdapter
import dev.openhub.app.databinding.FragmentBuscarBinding

class BuscarFragment : Fragment() {

    private var _binding: FragmentBuscarBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: EventoViewModel by activityViewModels()
    private lateinit var adaptador: EventoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBuscarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        configurarRecycler()
        observarDatos()
        configurarBuscador()
    }
    
    private fun configurarRecycler() {
        adaptador = EventoAdapter { evento ->
            viewModel.seleccionarEvento(evento)
            (requireActivity() as dev.openhub.app.MainActivity).abrirDetalleEvento()
        }
        binding.recyclerListaEventos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerListaEventos.adapter = adaptador
    }

    private fun observarDatos() {
        viewModel.eventos.observe(viewLifecycleOwner) { lista ->
            filtrarLista(binding.campoBusqueda.text.toString(), lista)
        }
    }
    
    private fun configurarBuscador() {
        binding.campoBusqueda.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                viewModel.eventos.value?.let { lista ->
                    filtrarLista(query, lista)
                }
            }
        })
    }
    
    private fun filtrarLista(query: String, listaCompleta: List<dev.openhub.app.model.Evento>) {
        if (query.isEmpty()) {
            adaptador.submitList(listaCompleta)
            binding.layoutEstadoVacio.visibility = View.GONE
            return
        }
        
        val filtrada = listaCompleta.filter {
            it.titulo.contains(query, ignoreCase = true) ||
            it.categoria.contains(query, ignoreCase = true) ||
            it.ubicacion.contains(query, ignoreCase = true)
        }
        
        adaptador.submitList(filtrada)
        
        if (filtrada.isEmpty()) {
            binding.layoutEstadoVacio.visibility = View.VISIBLE
        } else {
            binding.layoutEstadoVacio.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
