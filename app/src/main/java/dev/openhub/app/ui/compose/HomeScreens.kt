package dev.openhub.app.ui.compose

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.IconButton
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
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
import dev.openhub.app.util.EventoUtils
import dev.openhub.app.util.EventoUtils.obtenerSaludoDiario
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

    // titulo dinamico basado en la hora local
    val headerTitle = obtenerSaludoDiario()
    
    var hasAnimated by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!hasAnimated) {
            kotlinx.coroutines.delay(1000)
            hasAnimated = true
        }
    }

    var selectedCategory by remember { mutableStateOf("Todas") }
    var selectedCity by remember { mutableStateOf("Todas") }

    val categories = listOf("Todas") + eventos.map { it.categoria }.distinct()
    val cities = listOf("Todas") + eventos.map { it.ubicacion.split(",").first() }.distinct()

    val filteredEventos = eventos.filter {
        (selectedCategory == "Todas" || it.categoria == selectedCategory) &&
        (selectedCity == "Todas" || it.ubicacion.split(",").first() == selectedCity)
    }.sortedBy { it.fecha }

    // lazycolumn es la lista optimizada vertical que solo dibuja lo que ves en pantalla
    // el padding inferior asegura que las tarjetas no se tapen con el menu de navegacion
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding()),
        contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding() + 80.dp) 
    ) {
        item {
            StaggeredItem(index = 0, hasAnimated = hasAnimated) {
                Header(title = headerTitle, showLogo = true, onProfileClick = { navController.navigate(Screen.Perfil.route) })
            }
        }

        item {
            StaggeredItem(index = 1, hasAnimated = hasAnimated) {
                Column {
                    Text(
                        "Filtros",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextLight,
                        modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 8.dp)
                    )
                    LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { cat ->
                            androidx.compose.material3.FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) },
                                colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color.White,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(cities) { city ->
                            androidx.compose.material3.FilterChip(
                                selected = selectedCity == city,
                                onClick = { selectedCity = city },
                                label = { Text(city) },
                                colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color.White,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }
                    
                    Text(
                        "Próximos Eventos",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextTitle,
                        modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 12.dp)
                    )
                }
            }
        }

        // recorremos la lista de eventos de forma optimizada
        itemsIndexed(filteredEventos) { index, evento ->
            // staggereditem aplica el efecto de cascada sumando el indice al tiempo de retardo
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
    // comprobacion critica para evitar recalculos graficos
    // si la app ya mostro las animaciones iniciales renderizamos directo para no saturar el scroll
    if (hasAnimated) {
        Box { content() }
    } else {
        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(100L + (index * 120L))
            visible = true
        }
        
        val alpha by animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = tween(500)
        )
        val translationY by animateFloatAsState(
            targetValue = if (visible) 0f else 50f,
            animationSpec = tween(500, easing = FastOutSlowInEasing)
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
        item { Header(title = "Explorar", subtitle = "Los eventos más populares.") }
        items(eventos.sortedByDescending { it.clips }) { evento -> EventCard(evento, navController, viewModel, sharedTransitionScope, animatedVisibilityScope) }
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
fun Header(title: String, showLogo: Boolean = false, onProfileClick: (() -> Unit)? = null) {
    // cabecera principal con parametro condicional para inyectar el logo nativo
    // alineado a la izquierda con el texto a su derecha
    if (showLogo) {
        Row(
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
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
            if (onProfileClick != null) {
                IconButton(onClick = onProfileClick) {
                    Icon(Icons.Outlined.Person, contentDescription = "Perfil", tint = TextTitle)
                }
            }
        }
    } else {
        Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp)) {
            // titulo principal de la vista
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
    // contenedor apilado verticalmente para cabeceras con subtitulo
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 40.dp)) {
        Text(
            text = title, 
            style = MaterialTheme.typography.displayLarge,
            color = TextTitle
        )
        // solo renderizamos subtitulo si hay contenido
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
    val context = LocalContext.current
    val favoritos by viewModel.favoritos.observeAsState(emptySet())
    val isFavorito = favoritos.contains(evento.id)
    var showLoginPrompt by remember { mutableStateOf(false) }

    if (showLoginPrompt) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showLoginPrompt = false },
            title = { Text("Sesión requerida") },
            text = { Text("Debes iniciar sesión para guardar este evento en tus favoritos.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showLoginPrompt = false
                    navController.navigate(Screen.Perfil.route)
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

    // with inyecta el ambito de transicion compartida para usar sharedbounds
    with(sharedTransitionScope) {
        // contenedor maestro de la tarjeta
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                // click espacial que acciona la transicion fluida a la pantalla de detalles
                .spatialClickable {
                    viewModel.seleccionarEvento(evento)
                    navController.navigate(Screen.Detalle.route)
                }
                .liquidGlass(shape = RoundedCornerShape(32.dp))
        ) {
            // ordenacion vertical de la imagen arriba y la info abajo
            Column {
                Box {
                    // motor coil con efecto de difuminado o crossfade activado a 500ms
                    // esto asegura que cuando se descargue la foto no salte de golpe sino que aparezca elegantemente
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

                    Box(
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.TopEnd)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        IconButton(
                            onClick = { 
                                if (FirebaseAuth.getInstance().currentUser != null) {
                                    viewModel.toggleFavorito(evento.id)
                                } else {
                                    showLoginPrompt = true
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorito) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorito",
                                tint = if (isFavorito) Color.Red else Color.White
                            )
                        }
                    }
                }

                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = EventoUtils.capitalizarPalabras(evento.titulo),
                        color = TextTitle,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.5).sp
                        ),
                        // enlace de fisica compartida, permite que este texto vuele de manera independiente
                        // hacia la siguiente pantalla sin escalar la tarjeta entera
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
                                text = EventoUtils.capitalizarPalabras(evento.ubicacion.split(",").first()), 
                                color = TextLight, 
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.DateRange, contentDescription = null, tint = TextLight, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = EventoUtils.capitalizarPalabras(evento.fecha), 
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
