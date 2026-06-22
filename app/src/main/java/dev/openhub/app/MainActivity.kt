package dev.openhub.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dev.openhub.app.databinding.ActivityMainBinding
import dev.openhub.app.ui.BuscarFragment
import dev.openhub.app.ui.DetalleEventoFragment
import dev.openhub.app.ui.FeedEventosFragment
import dev.openhub.app.ui.ListaEventosFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val fragmentInicio by lazy { FeedEventosFragment() }
    private val fragmentExplorar by lazy { ListaEventosFragment.newInstance(ListaEventosFragment.Modo.EXPLORAR) }
    private val fragmentCategorias by lazy { ListaEventosFragment.newInstance(ListaEventosFragment.Modo.CATEGORIAS) }
    private val fragmentHistorial by lazy { ListaEventosFragment.newInstance(ListaEventosFragment.Modo.HISTORIAL) }
    private val fragmentBuscar by lazy { BuscarFragment() }

    private var fragmentActivo: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            mostrarFragmento(fragmentInicio)
        }

        binding.navegacionInferior.setOnItemSelectedListener { item ->
            val fragmento = when (item.itemId) {
                R.id.nav_inicio -> fragmentInicio
                R.id.nav_explorar -> fragmentExplorar
                R.id.nav_categorias -> fragmentCategorias
                R.id.nav_historial -> fragmentHistorial
                R.id.nav_buscar -> fragmentBuscar
                else -> null
            }
            if (fragmento != null) {
                mostrarFragmento(fragmento)
                true
            } else {
                false
            }
        }
    }

    private fun mostrarFragmento(fragmento: Fragment) {
        if (fragmento === fragmentActivo) return

        val transaccion = supportFragmentManager.beginTransaction()

        fragmentActivo?.let { transaccion.hide(it) }

        if (fragmento.isAdded) {
            transaccion.show(fragmento)
        } else {
            transaccion.add(R.id.contenedor_fragmento, fragmento)
        }

        transaccion.commit()
        fragmentActivo = fragmento
    }

    fun abrirDetalleEvento() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.contenedor_fragmento, DetalleEventoFragment())
            .addToBackStack(null)
            .commit()
        fragmentActivo = null
    }
}
