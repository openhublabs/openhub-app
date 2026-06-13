package dev.openhub.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dev.openhub.app.R
import dev.openhub.app.databinding.FragmentDetalleEventoBinding

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
                binding.imagenDetalle.setImageResource(evento.imagenRes)
                
                binding.etiquetaCategoriaDetalle.text = evento.categoria.uppercase()
                val colorChip = when (evento.categoria.lowercase()) {
                    "hackathon" -> R.color.color_chip_hackathon
                    "conferencia" -> R.color.color_chip_conferencia
                    "taller" -> R.color.color_chip_taller
                    else -> R.color.color_etiqueta_fondo
                }
                binding.etiquetaCategoriaDetalle.backgroundTintList = 
                    androidx.core.content.ContextCompat.getColorStateList(requireContext(), colorChip)

                binding.textoTiempoDetalle.text = "Hace ${evento.tiempoTexto}"
                
                val tituloCap = evento.titulo.split(" ").joinToString(" ") { it.replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase() else c.toString() } }
                binding.tituloDetalle.text = tituloCap
                
                val ubicacionCap = evento.ubicacion.split(",").joinToString(", ") { it.trim().replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase() else c.toString() } }
                binding.textoUbicacionDetalle.text = ubicacionCap
                
                val fechaCap = evento.fecha.split(" ").joinToString(" ") { it.replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase() else c.toString() } }
                binding.textoFechaDetalle.text = fechaCap
                
                val descCap = evento.descripcion.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                binding.descripcionDetalle.text = descCap
            }
        }
        
        binding.botonInscribirse.setOnClickListener {
            Toast.makeText(requireContext(), "Redirigiendo a la inscripcion...", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
