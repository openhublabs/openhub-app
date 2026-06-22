package dev.openhub.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import dev.openhub.app.R
import dev.openhub.app.databinding.FragmentDetalleEventoBinding
import dev.openhub.app.util.EventoUtils

class DetalleEventoFragment : Fragment() {

    private var _binding: FragmentDetalleEventoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EventoViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleEventoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbarDetalle.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        binding.toolbarDetalle.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        viewModel.eventoSeleccionado.observe(viewLifecycleOwner) { evento ->
            if (evento != null) {
                if (evento.imagenUrl.isNotEmpty()) {
                    Glide.with(binding.imagenDetalle.context)
                        .load(evento.imagenUrl)
                        .placeholder(R.drawable.evento_placeholder_1)
                        .centerCrop()
                        .into(binding.imagenDetalle)
                } else {
                    binding.imagenDetalle.setImageResource(R.drawable.evento_placeholder_1)
                }

                binding.etiquetaCategoriaDetalle.text = evento.categoria.uppercase()
                binding.etiquetaCategoriaDetalle.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), EventoUtils.colorChipParaCategoria(evento.categoria))

                binding.textoTiempoDetalle.text = "Hace ${evento.tiempoTexto}"
                binding.tituloDetalle.text = EventoUtils.capitalizarPalabras(evento.titulo)
                binding.textoUbicacionDetalle.text = EventoUtils.capitalizarUbicacion(evento.ubicacion)
                binding.textoFechaDetalle.text = EventoUtils.capitalizarPalabras(evento.fecha)
                binding.descripcionDetalle.text = EventoUtils.capitalizarPrimeraLetra(evento.descripcion)
            }
        }

        binding.botonInscribirse.setOnClickListener {
            val url = viewModel.eventoSeleccionado.value?.url.orEmpty()
            if (url.isNotEmpty()) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } else {
                Toast.makeText(requireContext(), "No hay enlace disponible", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
