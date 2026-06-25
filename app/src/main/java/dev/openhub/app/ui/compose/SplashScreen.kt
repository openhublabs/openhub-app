package dev.openhub.app.ui.compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.navigation.NavController
import dev.openhub.app.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(navController: NavController) {
    // variables de estado animables para controlar escala, opacidad y desenfoque inicial
    val scale = remember { Animatable(1.4f) }
    val alpha = remember { Animatable(0f) }
    val blur = remember { Animatable(32f) }

    LaunchedEffect(Unit) {
        // animacion de entrada: la imagen se reduce, aparece y se enfoca simulando un aterrizaje suave
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
            )
        }
        launch {
            blur.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
            )
        }
        // tiempo de espera en pantalla antes de iniciar la animacion de salida
        delay(2200)
        
        // animacion de salida: el logo se acerca hacia la camara y se desvanece
        // esto crea una ilusion de inmersion antes de cargar el feed
        launch {
            scale.animateTo(
                targetValue = 1.3f,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
        }
        
        delay(500)

        // navegacion hacia el feed una vez terminada la animacion
        // popup elimina el splash de la pila de navegacion para que no puedas volver atras con el boton fisico
        navController.navigate(Screen.Feed.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    // contenedor principal que ocupa toda la pantalla con fondo negro absoluto
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // columna central que agrupa el logo y el titulo y aplica los valores de la animacion
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            // imagen del logo con esquinas redondeadas
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "OpenHub Logo",
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(40.dp))
            )
            
            // espacio separador entre logo y texto
            Spacer(modifier = Modifier.height(24.dp))
            
            // titulo de la aplicacion con fuente personalizada y espacio negativo entre letras
            // esto le da el aspecto cinematografico
            Text(
                text = "Open Hub",
                color = Color.White,
                style = androidx.compose.material3.MaterialTheme.typography.displayMedium.copy(
                    letterSpacing = (-1).sp
                )
            )
        }
    }
}
