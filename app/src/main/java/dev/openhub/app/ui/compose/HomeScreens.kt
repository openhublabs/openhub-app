package dev.openhub.app.ui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import dev.openhub.app.R
import dev.openhub.app.model.Evento
import dev.openhub.app.ui.EventoViewModel
import dev.openhub.app.ui.theme.TextLight
import dev.openhub.app.ui.theme.TextSubtitle
import dev.openhub.app.ui.theme.TextTitle
import dev.openhub.app.ui.theme.liquidGlass
import dev.openhub.app.ui.theme.spatialClickable
import java.util.Calendar

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FeedScreen(
    viewModel: EventoViewModel,
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    innerPadding: PaddingValues
) {
    val eventos: List<Evento> by viewModel.eventos.observeAsState(emptyList())

    val userName: String? = null

    val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val saludo = when {
        hora < 12 -> "Buenos días"
        hora < 18 -> "Buenas tardes"
        else -> "Buenas noches"
    }

    val headerTitle = if (userName != null) "$saludo,\n$userName" else saludo
    
    var hasAnimated by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!hasAnimated) {
            kotlinx.coroutines.delay(1000)
            hasAnimated = true
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding()),
        contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding() + 80.dp) 
    ) {
        item {
            StaggeredItem(index = 0, hasAnimated = hasAnimated) {
                Header(title = headerTitle, showLogo = true)
            }
        }

        item {
            StaggeredItem(index = 1, hasAnimated = hasAnimated) {
                Text(
                    "Próximos Eventos",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextTitle,
                    modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 12.dp)
                )
            }
        }

        itemsIndexed(eventos) { index, evento ->
            StaggeredItem(index = 2 + index, hasAnimated = hasAnimated) {
                EventCard(evento, navController, viewModel, sharedTransitionScope, animatedVisibilityScope)
            }
        }
    }
}

@Composable
fun StaggeredItem(
    index: Int,
    hasAnimated: Boolean,
    content: @Composable () -> Unit
) {
    if (hasAnimated) {
        Box { content() }
    } else {
        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(100L + (index * 120L))
            visible = true
        }
        
        val alpha by androidx.compose.animation.core.animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = tween(500)
        )
        val translationY by androidx.compose.animation.core.animateFloatAsState(
            targetValue = if (visible) 0f else 50f,
            animationSpec = tween(500, easing = androidx.compose.animation.core.FastOutSlowInEasing)
        )
        
        Box(modifier = Modifier.graphicsLayer {
            this.alpha = alpha
            this.translationY = translationY
        }) {
            content()
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ExplorarScreen(viewModel: EventoViewModel, navController: NavController, sharedTransitionScope: SharedTransitionScope, animatedVisibilityScope: AnimatedVisibilityScope, innerPadding: PaddingValues) {
    val eventos: List<Evento> by viewModel.eventos.observeAsState(emptyList())
    LazyColumn(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding()), contentPadding = PaddingValues(bottom = 100.dp)) {
        item { Header(title = "Explorar", subtitle = "Nuevos mundos y experiencias.") }
        items(eventos.reversed()) { evento -> EventCard(evento, navController, viewModel, sharedTransitionScope, animatedVisibilityScope) }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CategoriasScreen(viewModel: EventoViewModel, navController: NavController, sharedTransitionScope: SharedTransitionScope, animatedVisibilityScope: AnimatedVisibilityScope, innerPadding: PaddingValues) {
    val eventos: List<Evento> by viewModel.eventos.observeAsState(emptyList())
    LazyColumn(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding()), contentPadding = PaddingValues(bottom = 100.dp)) {
        item { Header(title = "Categorías", subtitle = "Navega por ecosistemas visuales.") }
        items(eventos.sortedBy { it.categoria }) { evento -> EventCard(evento, navController, viewModel, sharedTransitionScope, animatedVisibilityScope) }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HistorialScreen(viewModel: EventoViewModel, navController: NavController, sharedTransitionScope: SharedTransitionScope, animatedVisibilityScope: AnimatedVisibilityScope, innerPadding: PaddingValues) {
    val eventos: List<Evento> by viewModel.eventos.observeAsState(emptyList())
    LazyColumn(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding()), contentPadding = PaddingValues(bottom = 100.dp)) {
        item { Header(title = "Historial", subtitle = "Tus viajes anteriores registrados.") }
        items(eventos.take(2)) { evento -> EventCard(evento, navController, viewModel, sharedTransitionScope, animatedVisibilityScope) }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun BuscarScreen(viewModel: EventoViewModel, navController: NavController, sharedTransitionScope: SharedTransitionScope, animatedVisibilityScope: AnimatedVisibilityScope, innerPadding: PaddingValues) {
    val eventos: List<Evento> by viewModel.eventos.observeAsState(emptyList())
    var query by remember { mutableStateOf("") }
    
    val filtered = if (query.isEmpty()) eventos else eventos.filter {
        it.titulo.contains(query, ignoreCase = true) || it.ubicacion.contains(query, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding())) {
        Header(title = "Buscar", subtitle = "Rastrea cualquier transmisión en la red.")
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .liquidGlass(shape = RoundedCornerShape(percent = 50)),
            placeholder = { Text("Focaliza tu objetivo...", color = TextLight, style = MaterialTheme.typography.bodyLarge) },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = TextLight) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = TextTitle,
                unfocusedTextColor = TextTitle
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
            items(filtered) { evento -> EventCard(evento, navController, viewModel, sharedTransitionScope, animatedVisibilityScope) }
        }
    }
}

@Composable
fun Header(title: String, showLogo: Boolean = false) {
    if (showLogo) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(56.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title, 
                style = MaterialTheme.typography.displayLarge,
                color = TextTitle
            )
        }
    } else {
        Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp)) {
            Text(
                text = title, 
                style = MaterialTheme.typography.displayLarge,
                color = TextTitle
            )
        }
    }
}

@Composable
fun Header(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 40.dp)) {
        Text(
            text = title, 
            style = MaterialTheme.typography.displayLarge,
            color = TextTitle
        )
        if (subtitle.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = subtitle, 
                style = MaterialTheme.typography.bodyLarge,
                color = TextSubtitle
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun EventCard(
    evento: Evento,
    navController: NavController,
    viewModel: EventoViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    with(sharedTransitionScope) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .spatialClickable {
                    viewModel.seleccionarEvento(evento)
                    navController.navigate(Screen.Detalle.route)
                }
                .liquidGlass(shape = RoundedCornerShape(32.dp))
        ) {
            Column {
                Box {
                    AsyncImage(
                        model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                            .data(evento.imagenUrl)
                            .crossfade(500)
                            .build(),
                        contentDescription = evento.titulo,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                            .sharedElement(
                                state = rememberSharedContentState(key = "image-${evento.id}"),
                                animatedVisibilityScope = animatedVisibilityScope,
                                boundsTransform = { _, _ -> tween(durationMillis = 400) }
                            ),
                        contentScale = ContentScale.Crop
                    )
                    
                    Box(
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.TopStart)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = evento.categoria, 
                            color = Color.White, 
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = capitalizarPalabras(evento.titulo),
                        color = TextTitle,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.5).sp
                        ),
                        modifier = Modifier.sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "title-${evento.id}"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = { _, _ -> tween(durationMillis = 400) }
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Outlined.Place, contentDescription = null, tint = TextLight, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = capitalizarPalabras(evento.ubicacion.split(",").first()), 
                                color = TextLight, 
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.DateRange, contentDescription = null, tint = TextLight, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = capitalizarPalabras(evento.fecha), 
                                color = TextLight, 
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun capitalizarPalabras(texto: String): String {
    return texto.split(" ").joinToString(" ") { palabra ->
        palabra.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
