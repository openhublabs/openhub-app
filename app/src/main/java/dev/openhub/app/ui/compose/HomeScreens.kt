package dev.openhub.app.ui.compose

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.WorkOutline
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
    val currentTime = System.currentTimeMillis()
    // Restamos 24 horas para que los eventos de "hoy" todavía se muestren durante todo el día
    val startOfDay = currentTime - (24 * 60 * 60 * 1000)

    val filteredEventos = eventos.filter {
        val eventTime = dev.openhub.app.util.EventoUtils.parseDateToLong(it.fecha)
        eventTime == 0L || eventTime >= startOfDay
    }.sortedBy { dev.openhub.app.util.EventoUtils.parseDateToLong(it.fecha).takeIf { t -> t > 0L } ?: Long.MAX_VALUE }

    // Eventos del carrusel horizontal (los primeros 5 son el carrusel principal)
    val carouselEventos = filteredEventos.take(5)
    val listEventos = filteredEventos.drop(5)

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

        // ─── Carrusel horizontal de tarjetas (Particle News: main story carousel) ───
        if (carouselEventos.isNotEmpty()) {
            item {
                StaggeredItem(index = 1, hasAnimated = hasAnimated) {
                    ParticleStoryCarousel(
                        eventos = carouselEventos,
                        navController = navController,
                        viewModel = viewModel,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                }
            }
        }

        // ─── Separador de sección con flecha (Particle: "Following ›") ───
        item {
            StaggeredItem(index = 2, hasAnimated = hasAnimated) {
                ParticleSectionHeader(
                    title = "Próximos Eventos",
                    showChevron = true
                )
            }
        }

        // recorremos la lista de eventos de forma optimizada
        itemsIndexed(listEventos) { index, evento ->
            // staggereditem aplica el efecto de cascada sumando el indice al tiempo de retardo
            StaggeredItem(index = 3 + index, hasAnimated = hasAnimated) {
                EventCard(evento, navController, viewModel, sharedTransitionScope, animatedVisibilityScope)
            }
        }

        // Si la lista está vacía pero hay carrusel, mostramos todos en el carrusel
        if (filteredEventos.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No hay eventos próximos", color = Color(0xFF757575), style = MaterialTheme.typography.bodyLarge)
                }
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
    // Animaciones eliminadas a peticion del usuario para que sea instantaneo y sin tirones
    Box { content() }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ExplorarScreen(viewModel: EventoViewModel, navController: NavController, sharedTransitionScope: SharedTransitionScope, animatedVisibilityScope: AnimatedVisibilityScope, innerPadding: PaddingValues) {
    val eventos: List<Evento> by viewModel.eventos.observeAsState(emptyList())
    var selectedCategory by remember { mutableStateOf("Todos") }
    
    // Dynamic categories from events
    val categories = remember(eventos) {
        listOf("Todos") + eventos.map { 
            it.categoria.replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase(java.util.Locale.getDefault()) else c.toString() } 
        }.distinct().sorted()
    }
    
    val now = java.util.Calendar.getInstance()
    val dateStr = "jun ${now.get(java.util.Calendar.DAY_OF_MONTH)}, ${now.get(java.util.Calendar.HOUR)}:${now.get(java.util.Calendar.MINUTE).toString().padStart(2, '0')} p.m."

    // Filter logic
    val filteredEventos = if (selectedCategory == "Todos") eventos else eventos.filter { 
        it.categoria.equals(selectedCategory, ignoreCase = true) 
    }
    val topSliderEventos = filteredEventos.take(4)
    val localEventos = filteredEventos.drop(4).take(4)

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Soft gradient background for the top half
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF5F8B7B), // Muted green/teal from screenshot
                            Color(0xFF5F8B7B).copy(alpha = 0.5f),
                            Color.White.copy(alpha = 0.0f)
                        )
                    )
                )
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding()),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header: Title and Date
            item {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)) {
                    Text("Explorar", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.ExtraBold)
                    Text(dateStr, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                }
            }

            // Horizontal Categories Tab (Text only, no chip background)
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(categories.size) { idx ->
                        val isSelected = selectedCategory == categories[idx]
                        Text(
                            text = categories[idx],
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                            fontSize = 16.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.spatialClickable { selectedCategory = categories[idx] }
                        )
                    }
                }
            }

            // Top Eventos Slider (4 cards)
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val titles = listOf("Top Eventos", "Tendencias", "Recomendados", "Populares")
                    val baseColors = listOf(
                        Color(0xFF0B2D7F), // Deep Blue
                        Color(0xFF226E22), // Deep Green
                        Color(0xFF8B0000), // Dark Red
                        Color(0xFF4A148C)  // Deep Purple
                    )
                    
                    items(topSliderEventos.size) { index ->
                        val evento = topSliderEventos[index]
                        Box(
                            modifier = Modifier
                                .width(220.dp)
                                .height(280.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .spatialClickable {
                                    viewModel.seleccionarEvento(evento)
                                    navController.navigate(Screen.Detalle.route)
                                }
                        ) {
                            AsyncImage(
                                model = evento.imagenUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Unique overlay gradients for the big cards
                            val overlayColors = listOf(Color.Transparent, baseColors[index % baseColors.size].copy(alpha = 0.9f))
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.verticalGradient(overlayColors))
                            )
                            
                            Text(
                                text = titles[index % titles.size],
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
                            )
                        }
                    }
                }
            }

            // Local Section Title
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    "Local",
                    color = Color.Black,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            // Local Section Horizontal Cards
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(localEventos) { evento ->
                        Box(
                            modifier = Modifier
                                .width(300.dp)
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .spatialClickable {
                                    viewModel.seleccionarEvento(evento)
                                    navController.navigate(Screen.Detalle.route)
                                }
                        ) {
                            AsyncImage(
                                model = evento.imagenUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))))
                            )
                            
                            // "Local" Pill Badge
                            Box(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .background(Color(0xFF3366FF), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                    .align(Alignment.TopStart)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Place, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Local", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Text(
                                text = dev.openhub.app.util.EventoUtils.capitalizarPalabras(evento.titulo),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}



@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HistorialScreen(viewModel: EventoViewModel, navController: NavController, sharedTransitionScope: SharedTransitionScope, animatedVisibilityScope: AnimatedVisibilityScope, innerPadding: PaddingValues) {
    val eventos: List<Evento> by viewModel.eventos.observeAsState(emptyList())
    val historialIds by viewModel.historial.observeAsState(emptyList())
    
    val historialEventos = historialIds.mapNotNull { id -> eventos.find { it.id == id } }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Soft gradient background for the top half (Amber/Orange theme)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFCC7A00), // Amber
                            Color(0xFFCC7A00).copy(alpha = 0.5f),
                            Color.White.copy(alpha = 0.0f)
                        )
                    )
                )
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding()),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp)) {
                    Text("Historial", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Tus viajes anteriores registrados.", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                }
            }
            
            if (historialEventos.isEmpty()) {
                item {
                    Text("Aún no tienes un historial de eventos.", color = Color.Black.copy(alpha = 0.6f), modifier = Modifier.padding(24.dp))
                }
            } else {
                items(historialEventos) { evento -> 
                    EventCard(evento, navController, viewModel, sharedTransitionScope, animatedVisibilityScope) 
                }
            }
        }
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

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Soft gradient background for the top half (Blue/Purple theme)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF4A3B8C), // Deep Purple
                            Color(0xFF4A3B8C).copy(alpha = 0.5f),
                            Color.White.copy(alpha = 0.0f)
                        )
                    )
                )
        )
        
        Column(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding())) {
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp)) {
                Text("Buscar", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.ExtraBold)
                Text("Rastrea cualquier transmisión en la red.", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            }
            
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(percent = 50)),
                placeholder = { Text("Focaliza tu objetivo...", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyLarge) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.7f)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
                items(filtered) { evento -> EventCard(evento, navController, viewModel, sharedTransitionScope, animatedVisibilityScope) }
            }
        }
    }
}

@Composable
fun Header(title: String, showLogo: Boolean = false, onProfileClick: (() -> Unit)? = null) {
    // Cabecera estilo widget_list_layout.xml de Particle News:
    // [logo 20dp] [titulo 14sp bold weight=1] [boton perfil 32dp]
    if (showLogo) {
        Row(
            modifier = Modifier
                .padding(start = 12.dp, end = 12.dp, top = 16.dp, bottom = 8.dp) // paddingStart/End="12dp" del XML
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // widget_logo: layout_width="20dp", layout_height="20dp" (Particle News widget_list_layout.xml)
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(36.dp).clip(CircleShape).scale(1.2f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp
                    ),
                    color = Color(0xFF000000)
                )
            }
            // widget_refresh_button equivalente: boton de perfil 32dp (widget_refresh_button: 32dp)
            if (onProfileClick != null) {
                Box(
                    modifier = Modifier
                        .size(32.dp) // layout_width="32dp" del XML
                        .spatialClickable { onProfileClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = "Perfil",
                        tint = Color(0xFF000000), // tint=widget_text_headline del XML
                        modifier = Modifier.size(20.dp)
                    )
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
    animatedVisibilityScope: AnimatedVisibilityScope,
    rank: Int? = null
) {
    val context = LocalContext.current
    val favoritos by viewModel.favoritos.observeAsState(emptySet())
    val isFavorito = favoritos.contains(evento.id)
    var showLoginPrompt by remember { mutableStateOf(false) }
    var dominantColor by remember { mutableStateOf(Color(0xFF4A1515)) }

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
            // Elementos en capas para la tarjeta estilo Particle News
            Box {
                // motor coil con efecto de difuminado o crossfade activado a 500ms
                // esto asegura que cuando se descargue la foto no salte de golpe sino que aparezca elegantemente
                AsyncImage(
                    model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                        .data(evento.imagenUrl)
                        .crossfade(500)
                        .allowHardware(false)
                        .build(),
                    contentDescription = evento.titulo,
                    onSuccess = { result ->
                        val drawable = result.result.drawable
                        if (drawable is android.graphics.drawable.BitmapDrawable) {
                            androidx.palette.graphics.Palette.from(drawable.bitmap).generate { palette ->
                                val swatch = palette?.darkVibrantSwatch ?: palette?.vibrantSwatch ?: palette?.dominantSwatch
                                swatch?.rgb?.let { color ->
                                    dominantColor = Color(color)
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .sharedElement(
                            state = rememberSharedContentState(key = "image-${evento.id}"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = { _, _ -> spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy) }
                        ),
                    contentScale = ContentScale.Crop
                )
                
                // Degradado: transparent -> transparent -> negro profundo (estilo Particle News)
                // El XML usa layout_gravity="bottom" para anclar el texto, este gradiente lo hace legible
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color.Transparent,
                                    0.4f to Color.Transparent,
                                    1.0f to dominantColor
                                )
                            )
                        )
                )
                
                // Logo/Favorito en esquina superior derecha, estilo Particle News (ic_particle_logo top|end)

                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopEnd)
                        .background(dev.openhub.app.ui.theme.LiquidGlassBackground, CircleShape)
                ) {
                    IconButton(
                        onClick = { 
                            if (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null) {
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
                            tint = if (isFavorito) Color.Red else Color.Black
                        )
                    }
                }

                // --- Texto inferior, layout_gravity="bottom" (Particle News: widget_story_card_layout.xml) ---
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp) // padding="16dp" del XML original
                ) {
                    // metadata: textSize="10sp", textColor="#99ffffff", letterSpacing="0.15" (Particle News XML)
                    Text(
                        text = if (rank != null) "🔥 #$rank  •  ${evento.categoria.uppercase()}" else evento.categoria.uppercase(),
                        color = Color(0x99FFFFFF), // #99ffffff del XML de Particle News
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            letterSpacing = 0.15.sp, // letterSpacing="0.15" del XML
                            fontWeight = if (rank != null) FontWeight.Bold else FontWeight.Normal
                        ),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp)) // layout_marginTop="4dp" del XML
                    
                    // headline: textSize="18sp", textStyle="bold", fontWeight="800", shadowColor="#80000000" (Particle News XML)
                    Text(
                        text = EventoUtils.capitalizarPalabras(evento.titulo),
                        color = Color.White, // textColor="#ffffff" del XML
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold, // fontWeight="800" del XML
                            fontSize = 18.sp,   // textSize="18sp" del XML
                            letterSpacing = 0.sp
                        ),
                        maxLines = 3, // maxLines="3" del XML
                        overflow = TextOverflow.Ellipsis,
                        // sombra de texto: shadowColor="#80000000", shadowDx=0, shadowDy=1, shadowRadius=4 (Particle News XML)
                        modifier = Modifier
                            .sharedBounds(
                                sharedContentState = rememberSharedContentState(key = "title-${evento.id}"),
                                animatedVisibilityScope = animatedVisibilityScope,
                                boundsTransform = { _, _ -> spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy) }
                            )
                            .graphicsLayer {
                                // shadowDy=1, shadowRadius=4 — sombra sutil para legibilidad
                                shadowElevation = 4f
                            }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Fila de metadata inferior: ubicacion y fecha
                    // textColor="#b3ffffff" del widget_list_item.xml de Particle News
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                Icons.Outlined.Place,
                                contentDescription = null,
                                tint = Color(0xB3FFFFFF), // #b3ffffff — widget_story_card_subheadline textColor
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = EventoUtils.capitalizarPalabras(evento.ubicacion.split(",").first()),
                                color = Color(0xB3FFFFFF), // #b3ffffff del XML
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        // separador visual • (igual que widget_list_item.xml de Particle News)
                        Text(
                            text = "•",
                            color = Color(0x66FFFFFF),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.DateRange,
                                contentDescription = null,
                                tint = Color(0xB3FFFFFF),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = EventoUtils.capitalizarPalabras(evento.fecha),
                                color = Color(0xB3FFFFFF),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ParticleStoryCarousel — Carrusel horizontal de tarjetas estilo Particle News
// Imagen 3: main story card con paginación (dots), full-width, 280dp alto
// ⚡ N HR AGO • M ARTÍCULOS — texto metadata sobre imagen + gradiente
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalSharedTransitionApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ParticleStoryCarousel(
    eventos: List<Evento>,
    navController: NavController,
    viewModel: EventoViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    if (eventos.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { eventos.size })

    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp
        ) { page ->
            val evento = eventos[page]
            var dominantColor by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(androidx.compose.ui.graphics.Color(0xFF4A1515)) }
            // Tarjeta principal del carrusel — full-bleed, 280dp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .spatialClickable {
                        viewModel.seleccionarEvento(evento)
                        navController.navigate(Screen.Detalle.route)
                    }
            ) {
                // Imagen full-bleed
                AsyncImage(
                    model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                        .data(evento.imagenUrl)
                        .crossfade(500)
                        .allowHardware(false)
                        .build(),
                    onSuccess = { result ->
                        val drawable = result.result.drawable
                        if (drawable is android.graphics.drawable.BitmapDrawable) {
                            androidx.palette.graphics.Palette.from(drawable.bitmap).generate { palette ->
                                val swatch = palette?.darkVibrantSwatch ?: palette?.vibrantSwatch ?: palette?.dominantSwatch
                                swatch?.rgb?.let { color ->
                                    dominantColor = androidx.compose.ui.graphics.Color(color)
                                }
                            }
                        }
                    },
                    contentDescription = evento.titulo,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Gradiente fuerte para texto — igual al scrim de Particle News
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color.Transparent,
                                    0.35f to Color.Transparent,
                                    1.0f to dominantColor
                                )
                            )
                        )
                )
                // Contenido textual — bottom, padding=16dp (widget_story_card_layout.xml)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    // "⚡ N HR AGO • M ARTÍCULOS" — exacto de Particle News (imagen 3 y 5)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⚡",
                            fontSize = 10.sp,
                            color = Color(0xFF99FFFFFF) // #99ffffff del XML
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = EventoUtils.capitalizarPalabras(evento.fecha).uppercase(),
                            color = Color(0x99FFFFFF), // #99ffffff del XML
                            fontSize = 10.sp,
                            letterSpacing = 0.15.sp,   // letterSpacing="0.15" del XML
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp)) // layout_marginTop="4dp" del XML
                    // Headline 18sp ExtraBold (fontWeight="800" del XML)
                    Text(
                        text = EventoUtils.capitalizarPalabras(evento.titulo),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold, // fontWeight="800"
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    // Subheadline: 14sp #b3ffffff (widget_story_card_subheadline del XML)
                    Text(
                        text = "${evento.ubicacion.split(",").first()} • ${evento.categoria}",
                        color = Color(0xB3FFFFFF), // #b3ffffff del XML
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // "🔥 Popular" badge chip top-left (imagen 4 y 5 de Particle News)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .background(Color(0xCC000000), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "🔥 Destacado",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Indicadores de página — dots (Particle News: pager dots debajo del carrusel)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(eventos.size) { idx ->
                val isSelected = pagerState.currentPage == idx
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (isSelected) 8.dp else 6.dp)
                        .background(
                            if (isSelected) Color(0xFF000000) else Color(0xFFCCCCCC),
                            CircleShape
                        )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ParticleSectionHeader — Separador de sección estilo Particle News
// Imagen 3 y 4: "Following ›", "Popular ›", "Próximos Eventos ›"
// widget_title: textSize="14sp" bold, textColor=widget_text_headline="#000000"
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ParticleSectionHeader(
    title: String,
    showChevron: Boolean = true,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // título bold 18sp, widget_text_headline=#000000
            Text(
                text = title,
                color = Color(0xFF000000),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
            if (showChevron) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF000000),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        trailingContent?.invoke()
    }
}

