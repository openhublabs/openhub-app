package dev.openhub.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dev.openhub.app.MainActivity
import dev.openhub.app.R
import dev.openhub.app.adapter.EventoAdapter
import dev.openhub.app.databinding.FragmentListadoEventosBinding
import dev.openhub.app.model.Evento

class ListaEventosFragment : Fragment() {

    enum class Modo(@StringRes val tituloRes: Int, @StringRes val subtituloRes: Int) {
        EXPLORAR(R.string.titulo_explorar, R.string.subtitulo_explorar),
        CATEGORIAS(R.string.titulo_categorias, R.string.subtitulo_categorias),
        HISTORIAL(R.string.titulo_historial, R.string.subtitulo_historial)
    }

    companion object {
        private const val ARG_MODO = "modo"

        fun newInstance(modo: Modo): ListaEventosFragment {
            return ListaEventosFragment().apply {
                arguments = bundleOf(ARG_MODO to modo.name)
            }
        }
    }

    private var _binding: FragmentListadoEventosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EventoViewModel by activityViewModels()
    private lateinit var adaptador: EventoAdapter
    private lateinit var modo: Modo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        modo = Modo.valueOf(requireArguments().getString(ARG_MODO)!!)
    }

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

        binding.textoTituloFragmento.text = getString(modo.tituloRes)
        binding.textoSubtituloFragmento.text = getString(modo.subtituloRes)

        adaptador = EventoAdapter { evento ->
            viewModel.seleccionarEvento(evento)
            (requireActivity() as MainActivity).abrirDetalleEvento()
        }
        binding.recyclerListaEventos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerListaEventos.adapter = adaptador

        viewModel.eventos.observe(viewLifecycleOwner) { lista ->
            adaptador.submitList(transformarLista(lista))
        }
    }

    private fun transformarLista(lista: List<Evento>): List<Evento> {
        return when (modo) {
            Modo.EXPLORAR -> lista.reversed()
            Modo.CATEGORIAS -> lista.sortedBy { it.categoria }
            Modo.HISTORIAL -> lista.take(2)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
