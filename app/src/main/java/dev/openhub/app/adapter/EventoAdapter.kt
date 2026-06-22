package dev.openhub.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dev.openhub.app.R
import dev.openhub.app.databinding.ItemEventoBinding
import dev.openhub.app.model.Evento
import dev.openhub.app.util.EventoUtils

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
            if (evento.imagenUrl.isNotEmpty()) {
                Glide.with(binding.imagenEvento.context)
                    .load(evento.imagenUrl)
                    .placeholder(R.drawable.evento_placeholder_1)
                    .centerCrop()
                    .into(binding.imagenEvento)
            } else {
                binding.imagenEvento.setImageResource(R.drawable.evento_placeholder_1)
            }

            binding.etiquetaCategoria.text = evento.categoria.uppercase()
            binding.etiquetaCategoria.setBackgroundResource(R.drawable.fondo_etiqueta)
            binding.etiquetaCategoria.backgroundTintList =
                androidx.core.content.ContextCompat.getColorStateList(
                    binding.root.context,
                    EventoUtils.colorChipParaCategoria(evento.categoria)
                )

            val tiempoStr = "${evento.tiempoTexto} ago \u00B7 ${evento.clips} artículos".uppercase()
            binding.etiquetaTiempo.text = "\u26A1 $tiempoStr"

            binding.tituloEvento.text = EventoUtils.capitalizarPalabras(evento.titulo)
            binding.descripcionEvento.text = EventoUtils.capitalizarPrimeraLetra(evento.descripcion)

            binding.textoUbicacion.text = "\uD83D\uDCCD ${EventoUtils.capitalizarUbicacion(evento.ubicacion)}"
            binding.textoFecha.text = "\uD83D\uDCC5 ${EventoUtils.capitalizarPalabras(evento.fecha)}"
            binding.textoClips.text = "\uD83C\uDF99 ${evento.clips} Clips"

            binding.tarjetaEvento.setOnClickListener { alHacerClic(evento) }
        }
    }

    class EventoDiffCallback : DiffUtil.ItemCallback<Evento>() {
        override fun areItemsTheSame(oldItem: Evento, newItem: Evento): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Evento, newItem: Evento): Boolean =
            oldItem == newItem
    }
}
