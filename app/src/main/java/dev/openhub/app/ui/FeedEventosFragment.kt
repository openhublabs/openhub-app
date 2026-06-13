package dev.openhub.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dev.openhub.app.R
import dev.openhub.app.adapter.EventoAdapter
import dev.openhub.app.databinding.FragmentFeedEventosBinding
import java.util.Calendar

class FeedEventosFragment : Fragment() {

    private var _binding: FragmentFeedEventosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EventoViewModel by activityViewModels()
    private lateinit var adaptador: EventoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedEventosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarSaludo()
        configurarRecycler()
        observarDatos()
    }

    private fun configurarSaludo() {
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val saludo = when {
            hora < 12 -> getString(R.string.saludo_buenos_dias)
            hora < 18 -> getString(R.string.saludo_buenas_tardes)
            else -> getString(R.string.saludo_buenas_noches)
        }
        val appName = getString(R.string.app_name)
        binding.textoSaludo.text = "$saludo \n$appName"
    }

    private fun configurarRecycler() {
        adaptador = EventoAdapter { evento ->
            viewModel.seleccionarEvento(evento)
            (requireActivity() as dev.openhub.app.MainActivity).abrirDetalleEvento()
        }
        binding.recyclerEventos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerEventos.adapter = adaptador
    }

    private fun observarDatos() {
        viewModel.eventos.observe(viewLifecycleOwner) { lista ->
            adaptador.submitList(lista)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
