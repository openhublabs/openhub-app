package dev.openhub.app.ui.compose

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import coil.request.ImageRequest
import android.graphics.drawable.BitmapDrawable
import androidx.palette.graphics.Palette
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
    var dominantColor by remember { androidx.compose.runtime.mutableStateOf(androidx.compose.ui.graphics.Color(0xFF4A1515)) }

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
        var selectedTab by remember { mutableIntStateOf(0) }
        
        androidx.compose.runtime.LaunchedEffect(currentEvento.id) {
            viewModel.agregarAHistorial(currentEvento.id)
        }
        
        with(sharedTransitionScope) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(dominantColor)
            ) {
                val scrollState = rememberScrollState()
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    scrollState.scrollTo(0)
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(currentEvento.imagenUrl)
                                .crossfade(500)
                                .allowHardware(false)
                                .build(),
                            onSuccess = { result ->
                                val drawable = result.result.drawable
                                if (drawable is BitmapDrawable) {
                                    Palette.from(drawable.bitmap).generate { palette ->
                                        val swatch = palette?.darkVibrantSwatch ?: palette?.vibrantSwatch ?: palette?.dominantSwatch
                                        swatch?.rgb?.let { color ->
                                            dominantColor = Color(color)
                                        }
                                    }
                                }
                            },
                            contentDescription = currentEvento.titulo,
                            modifier = Modifier
                                .fillMaxSize()
                                .sharedElement(
                                    state = rememberSharedContentState(key = "image-${currentEvento.id}"),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    boundsTransform = { _, _ -> spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy) }
                                ),
                            contentScale = ContentScale.Crop
                        )
                        
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colorStops = arrayOf(
                                            0.0f to Color.Transparent,
                                            0.6f to Color.Transparent,
                                            1.0f to dominantColor
                                        )
                                    )
                                )
                        )
                    } // closes Box(400.dp)

                    // Content over dominant color
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(top = 16.dp)
                    ) {
                        Text(
                            text = currentEvento.categoria.uppercase(),
                            color = Color(0x99FFFFFF),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 12.sp,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentEvento.titulo,
                            color = Color.White,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-1).sp,
                                lineHeight = 38.sp
                            ),
                            modifier = Modifier.sharedBounds(
                                sharedContentState = rememberSharedContentState(key = "title-${currentEvento.id}"),
                                animatedVisibilityScope = animatedVisibilityScope,
                                boundsTransform = { _, _ -> spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy) }
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = currentEvento.descripcion.take(150) + "...",
                            color = Color(0xCCFFFFFF),
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        val tabs = listOf("Resumen", "Lugar", "Fecha")
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            tabs.forEachIndexed { idx, tab ->
                                val isSelected = selectedTab == idx
                                Box(
                                    modifier = Modifier
                                        .spatialClickable { selectedTab = idx }
                                        .padding(bottom = 8.dp)
                                ) {
                                    Text(
                                        text = tab,
                                        color = if (isSelected) Color.White else Color(0x99FFFFFF),
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // White card below tabs
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp)
                            .background(Color.White, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                            .padding(32.dp)
                            .padding(bottom = 80.dp) // extra padding for bottom button
                    ) {
                        when (selectedTab) {
                            0 -> {
                                Column {
                                    val parrafos = currentEvento.descripcion.split(". ").filter { it.isNotBlank() }
                                    parrafos.forEach { parrafo ->
                                        Row(
                                            modifier = Modifier.padding(vertical = 12.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(top = 8.dp)
                                                    .size(8.dp)
                                                    .background(Color(0xFFD4E6B5), CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Text(
                                                text = parrafo.trim() + if (!parrafo.endsWith(".")) "." else "",
                                                color = Color(0xFF2C2C2C),
                                                fontSize = 17.sp,
                                                lineHeight = 26.sp
                                            )
                                        }
                                    }
                                }
                            }
                            1 -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Place, contentDescription = null, tint = Color(0xFF757575))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = currentEvento.ubicacion, color = Color(0xFF757575), fontSize = 16.sp)
                                }
                            }
                            2 -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.DateRange, contentDescription = null, tint = Color(0xFF757575))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = currentEvento.fecha, color = Color(0xFF757575), fontSize = 16.sp)
                                } // closes Row
                            } // closes 2 -> block
                        } // closes when
                    } // closes White Card Box
                } // closes Scrollable Column

                // Ir a evento Button
                if (currentEvento.url.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(24.dp)
                            .fillMaxWidth()
                    ) {
                        androidx.compose.material3.Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(currentEvento.url))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.Black),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
                        ) {
                            Text("Ir a evento", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                // Sticky actions row
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.4f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                .background(Color.Black.copy(alpha = 0.4f))
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
                                .background(Color.Black.copy(alpha = 0.4f))
                        ) {
                            Icon(
                                imageVector = if (isFavorito) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorito",
                                tint = if (isFavorito) Color.Red else Color.White
                            )
                        }
                    }
                }
            } // closes Box(120)
        } // closes with(119)
    } // closes let(111)
} // closes DetailScreen
