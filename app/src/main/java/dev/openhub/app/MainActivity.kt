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
                // Estado para controlar la pantalla actual
                var currentRoute by remember { mutableStateOf("main") }

                when (currentRoute) {
                    "login" -> {
                        androidx.activity.compose.BackHandler { currentRoute = "main" }
                        LoginScreen(
                            onLoginSuccess = { currentRoute = "main" },
                            onNavigateToRegister = { currentRoute = "register" }
                        )
                    }
                    "register" -> {
                        androidx.activity.compose.BackHandler { currentRoute = "login" }
                        dev.openhub.app.ui.compose.RegisterScreen(
                            onRegisterSuccess = { currentRoute = "main" },
                            onNavigateToLogin = { currentRoute = "login" }
                        )
                    }
                    "main" -> {
                        MainScreen(
                            viewModel = viewModel,
                            onNavigateToLogin = { currentRoute = "login" }
                        )
                    }
                }
            }
        }
    }
}