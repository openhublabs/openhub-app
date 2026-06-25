package dev.openhub.app.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import dev.openhub.app.ui.EventoViewModel
import dev.openhub.app.ui.theme.TextSubtitle
import dev.openhub.app.ui.theme.TextTitle

import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PerfilScreen(
    viewModel: EventoViewModel, 
    navController: NavController, 
    onNavigateToLogin: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val auth = remember { FirebaseAuth.getInstance() }
    var currentUser by remember { mutableStateOf(auth.currentUser) }
    
    val eventos by viewModel.eventos.observeAsState(emptyList())
    val favoritosIds by viewModel.favoritos.observeAsState(emptySet())
    val eventosFavoritos = eventos.filter { favoritosIds.contains(it.id) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.navigateUp() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Perfil", color = TextTitle, style = MaterialTheme.typography.titleLarge)
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (currentUser == null) {
            Text("No has iniciado sesión", color = TextSubtitle, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onNavigateToLogin,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth(0.8f).height(54.dp)
            ) {
                Text("Iniciar Sesión o Registrarse", color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        } else {
            Text("Sesión iniciada como:", color = TextSubtitle, style = MaterialTheme.typography.bodyLarge)
            Text(currentUser!!.email ?: "Usuario", color = TextTitle, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    auth.signOut()
                    currentUser = null
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                modifier = Modifier.fillMaxWidth(0.8f).height(48.dp)
            ) {
                Text("Cerrar Sesión", color = Color.White)
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("Tus Favoritos", color = TextTitle, style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.Start).padding(start = 24.dp))
            Spacer(modifier = Modifier.height(16.dp))
            
            if (eventosFavoritos.isEmpty()) {
                Text("Aún no tienes eventos favoritos.", color = TextSubtitle, modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(eventosFavoritos) { evento ->
                        EventCard(
                            evento = evento,
                            navController = navController,
                            viewModel = viewModel,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    }
                }
            }
        }
    }
}
