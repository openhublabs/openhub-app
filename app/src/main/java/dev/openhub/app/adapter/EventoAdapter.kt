package dev.openhub.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.openhub.app.R
import dev.openhub.app.databinding.ItemEventoBinding
import dev.openhub.app.model.Evento

class EventoAdapter(
    private val alHacerClic: (Evento) -> Unit
) : ListAdapter<Evento, EventoAdapter.EventoViewHolder>(EventoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val binding = ItemEventoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        holder.vincular(getItem(position))
    }

    inner class EventoViewHolder(
        private val binding: ItemEventoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun vincular(evento: Evento) {
            binding.imagenEvento.setImageResource(evento.imagenRes)

            binding.etiquetaCategoria.text = evento.categoria.uppercase()

            val colorChip = when (evento.categoria.lowercase()) {
                "hackathon" -> R.color.color_chip_hackathon
                "conferencia" -> R.color.color_chip_conferencia
                "taller" -> R.color.color_chip_taller
                else -> R.color.color_etiqueta_fondo
            }
            binding.etiquetaCategoria.setBackgroundResource(R.drawable.fondo_etiqueta)
            binding.etiquetaCategoria.backgroundTintList = 
                androidx.core.content.ContextCompat.getColorStateList(binding.root.context, colorChip)

            val tiempoStr = "${evento.tiempoTexto} ago \u00B7 ${evento.clips} artículos".uppercase()
            binding.etiquetaTiempo.text = "\u26A1 $tiempoStr"

            binding.tituloEvento.text = capitalizarPalabras(evento.titulo)
            binding.descripcionEvento.text = capitalizarPrimeraLetra(evento.descripcion)
            
            val ubicacionCap = evento.ubicacion.split(",").joinToString(", ") { capitalizarPalabras(it.trim()) }
            binding.textoUbicacion.text = "\uD83D\uDCCD $ubicacionCap"
            
            val fechaCap = capitalizarPalabras(evento.fecha)
            binding.textoFecha.text = "\uD83D\uDCC5 $fechaCap"
            
            binding.textoClips.text = "\uD83C\uDF99 ${evento.clips} Clips"

            binding.tarjetaEvento.setOnClickListener {
                alHacerClic(evento)
            }
        }
    }

    class EventoDiffCallback : DiffUtil.ItemCallback<Evento>() {
        override fun areItemsTheSame(oldItem: Evento, newItem: Evento): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Evento, newItem: Evento): Boolean =
            oldItem == newItem
    }

    private fun capitalizarPalabras(texto: String): String {
        return texto.split(" ").joinToString(" ") { palabra ->
            palabra.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }

    private fun capitalizarPrimeraLetra(texto: String): String {
        return texto.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}