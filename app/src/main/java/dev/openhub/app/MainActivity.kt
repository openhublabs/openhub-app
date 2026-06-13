package dev.openhub.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dev.openhub.app.databinding.ActivityMainBinding
import dev.openhub.app.ui.BuscarFragment
import dev.openhub.app.ui.CategoriasFragment
import dev.openhub.app.ui.ExplorarFragment
import dev.openhub.app.ui.FeedEventosFragment
import dev.openhub.app.ui.HistorialFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            cargarFragmento(FeedEventosFragment())
        }

        binding.navegacionInferior.setOnItemSelectedListener { item ->
            val fragmento = when (item.itemId) {
                R.id.nav_inicio -> FeedEventosFragment()
                R.id.nav_explorar -> ExplorarFragment()
                R.id.nav_categorias -> CategoriasFragment()
                R.id.nav_historial -> HistorialFragment()
                R.id.nav_buscar -> BuscarFragment()
                else -> null
            }
            
            if (fragmento != null) {
                cargarFragmento(fragmento)
                true
            } else {
                false
            }
        }
    }

    fun cargarFragmento(fragmento: Fragment, agregarAlBackStack: Boolean = false) {
        val transaccion = supportFragmentManager.beginTransaction()
            .replace(R.id.contenedor_fragmento, fragmento)
            
        if (agregarAlBackStack) {
            transaccion.addToBackStack(null)
        }
        transaccion.commit()
    }
    
    fun abrirDetalleEvento() {
        cargarFragmento(dev.openhub.app.ui.DetalleEventoFragment(), true)
    }
}