package dev.openhub.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dev.openhub.app.R
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dev.openhub.app.adapter.EventoAdapter
import dev.openhub.app.databinding.FragmentListadoEventosBinding

class HistorialFragment : Fragment() {

    private var _binding: FragmentListadoEventosBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: EventoViewModel by activityViewModels()
    private lateinit var adaptador: EventoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListadoEventosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.textoTituloFragmento.text = getString(R.string.titulo_historial)
        binding.textoSubtituloFragmento.text = getString(R.string.subtitulo_historial)
        
        configurarRecycler()
        observarDatos()
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
            // Simulating history by taking just the first 2 events
            adaptador.submitList(lista.take(2))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
