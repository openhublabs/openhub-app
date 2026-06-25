package dev.openhub.app.ui.compose

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import android.content.Intent
import android.widget.Toast
import coil.compose.AsyncImage
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.openhub.app.ui.EventoViewModel
import dev.openhub.app.ui.theme.TextLight
import dev.openhub.app.ui.theme.TextSubtitle
import dev.openhub.app.ui.theme.TextTitle
import dev.openhub.app.ui.theme.liquidGlassStrong
import dev.openhub.app.ui.theme.spatialClickable

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DetailScreen(
    viewModel: EventoViewModel,
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val evento by viewModel.eventoSeleccionado.observeAsState()
    val hazeState = remember { HazeState() }
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val favoritos by viewModel.favoritos.observeAsState(emptySet())
    var showLoginPrompt by remember { androidx.compose.runtime.mutableStateOf(false) }

    if (showLoginPrompt) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showLoginPrompt = false },
            title = { Text("Sesión requerida") },
            text = { Text("Debes iniciar sesión para guardar este evento en tus favoritos.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showLoginPrompt = false
                    navController.navigate(dev.openhub.app.ui.compose.Screen.Perfil.route)
                }) {
                    Text("Iniciar Sesión")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showLoginPrompt = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    evento?.let { currentEvento ->
        val isFavorito = favoritos.contains(currentEvento.id)
        androidx.compose.runtime.LaunchedEffect(currentEvento.id) {
            viewModel.agregarAHistorial(currentEvento.id)
        }
        
        // scope especial que permite vincular visualmente esta pantalla con la pantalla anterior
        with(sharedTransitionScope) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // columnda que permite scroll vertical para ver todos los detalles del evento
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // caja que contiene la imagen de cabecera a 350dp de altura
                    Box(modifier = Modifier.fillMaxWidth().height(350.dp)) {
                        AsyncImage(
                            model = currentEvento.imagenUrl,
                            contentDescription = currentEvento.titulo,
                            modifier = Modifier
                                .fillMaxSize()
                                // motor de cristal liquido que difumina el contenido que pasa por detras
                                .haze(hazeState)
                                // enlace de elemento compartido para recibir la foto expandida desde la tarjeta
                                .sharedElement(
                                    state = rememberSharedContentState(key = "image-${currentEvento.id}"),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    boundsTransform = { _, _ -> tween(durationMillis = 400) }
                                ),
                            contentScale = ContentScale.Crop
                        )
                        
                        // fila superpuesta en la parte superior para el boton de volver atras y compartir
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // boton con efecto liquid glass para retroceder
                            IconButton(
                                onClick = { navController.navigateUp() },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .hazeChild(state = hazeState, shape = CircleShape, blurRadius = 64.dp, tint = Color.Black.copy(alpha = 0.4f))
                                    .border(
                                        width = 1.dp,
                                        color = Color.White.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    )
                                    .background(Color.Black.copy(alpha = 0.3f))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = Color.White
                                )
                            }
                            
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                IconButton(
                                    onClick = {
                                        val sendIntent: Intent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, "¡Mira este evento!: ${currentEvento.titulo} en ${currentEvento.url}")
                                            type = "text/plain"
                                        }
                                        val shareIntent = Intent.createChooser(sendIntent, null)
                                        context.startActivity(shareIntent)
                                    },
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .hazeChild(state = hazeState, shape = CircleShape, blurRadius = 64.dp, tint = Color.Black.copy(alpha = 0.4f))
                                        .border(
                                            width = 1.dp,
                                            color = Color.White.copy(alpha = 0.3f),
                                            shape = CircleShape
                                        )
                                        .background(Color.Black.copy(alpha = 0.3f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Share,
                                        contentDescription = "Compartir",
                                        tint = Color.White
                                    )
                                }
                                
                                IconButton(
                                    onClick = {
                                        if (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null) {
                                            viewModel.toggleFavorito(currentEvento.id)
                                        } else {
                                            showLoginPrompt = true
                                        }
                                    },
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .hazeChild(state = hazeState, shape = CircleShape, blurRadius = 64.dp, tint = Color.Black.copy(alpha = 0.4f))
                                        .border(
                                            width = 1.dp,
                                            color = Color.White.copy(alpha = 0.3f),
                                            shape = CircleShape
                                        )
                                        .background(Color.Black.copy(alpha = 0.3f))
                                ) {
                                    Icon(
                                        imageVector = if (isFavorito) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = "Favorito",
                                        tint = if (isFavorito) Color.Red else Color.White
                                    )
                                }
                            }
                        }
                    }

                    // contenedor inferior tipo tarjeta superpuesta que contiene la informacion
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            // aplicamos cristal liquido fuerte para que el fondo de la foto se difumine si bajas mucho
                            .liquidGlassStrong(hazeState = hazeState, shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp, bottomStart = 0.dp, bottomEnd = 0.dp))
                            .padding(32.dp)
                            .navigationBarsPadding()
                    ) {
                        Text(
                            text = currentEvento.categoria.uppercase(),
                            color = TextLight,
                            style = MaterialTheme.typography.labelLarge,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = currentEvento.titulo,
                            color = TextTitle,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-1).sp
                            ),
                            // recibe el texto en vuelo desde el feed garantizando la transicion ios26 pura
                            modifier = Modifier.sharedBounds(
                                sharedContentState = rememberSharedContentState(key = "title-${currentEvento.id}"),
                                animatedVisibilityScope = animatedVisibilityScope,
                                boundsTransform = { _, _ -> tween(durationMillis = 400) }
                            )
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.DateRange, contentDescription = null, tint = TextLight, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = currentEvento.fecha, color = TextLight, style = MaterialTheme.typography.bodyLarge)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Place, contentDescription = null, tint = TextLight, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = currentEvento.ubicacion, color = TextLight, style = MaterialTheme.typography.bodyLarge)
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = currentEvento.descripcion,
                            color = TextSubtitle,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        // espaciado antes del boton de accion principal
                        Spacer(modifier = Modifier.height(48.dp))
                        
                        // boton de llamada a la accion gigante en la parte inferior
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .spatialClickable { 
                                    if (currentEvento.url.isNotEmpty()) {
                                        uriHandler.openUri(currentEvento.url)
                                    } else {
                                        Toast.makeText(context, "Enlace no disponible", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .background(Color.White.copy(alpha = 0.95f), CircleShape)
                        ) {
                            // centrado horizontal y vertical del texto y el icono
                            Row(
                                modifier = Modifier.align(Alignment.Center),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Comenzar Viaje",
                                    color = Color.Black,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Outlined.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}
