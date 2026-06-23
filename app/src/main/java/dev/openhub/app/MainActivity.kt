package dev.openhub.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import dev.openhub.app.ui.EventoViewModel
import dev.openhub.app.ui.compose.LoginScreen
import dev.openhub.app.ui.compose.MainScreen
import dev.openhub.app.ui.theme.OpenHubTheme

class MainActivity : ComponentActivity() {

    private val viewModel: EventoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OpenHubTheme {
                // Estado para controlar si el usuario ya inició sesión
                var isLoggedIn by remember { mutableStateOf(false) }

                if (!isLoggedIn) {
                    LoginScreen(onLoginSuccess = {
                        isLoggedIn = true // Cambia el estado al completar Firebase con éxito
                    })
                } else {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}