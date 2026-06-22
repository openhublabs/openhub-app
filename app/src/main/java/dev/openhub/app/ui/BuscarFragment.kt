package dev.openhub.app.ui

import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dev.openhub.app.MainActivity
import dev.openhub.app.adapter.EventoAdapter
import dev.openhub.app.databinding.FragmentBuscarBinding
import dev.openhub.app.model.Evento

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

        adaptador = EventoAdapter { evento ->
            viewModel.seleccionarEvento(evento)
            (requireActivity() as MainActivity).abrirDetalleEvento()
        }
        binding.recyclerListaEventos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerListaEventos.adapter = adaptador

        viewModel.eventos.observe(viewLifecycleOwner) { lista ->
            filtrarLista(binding.campoBusqueda.text.toString(), lista)
        }

        binding.campoBusqueda.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                viewModel.eventos.value?.let { filtrarLista(s.toString(), it) }
            }
        })
    }

    private fun filtrarLista(query: String, listaCompleta: List<Evento>) {
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
        binding.layoutEstadoVacio.visibility = if (filtrada.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
