package dev.openhub.app.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import dev.openhub.app.ui.EventoViewModel
import dev.openhub.app.ui.theme.TextSubtitle
import dev.openhub.app.ui.theme.TextTitle
import dev.openhub.app.ui.theme.spatialClickable

import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically

// ─────────────────────────────────────────────────────
// Colores exactos de Particle News Account screen
// widget_text_headline="#000000", widget_text_secondary="#757575"
// Accent azul: #2563EB (Color.Blue en Particle), Morado CTA: #7C3AED
// ─────────────────────────────────────────────────────
private val ParticleBlue  = Color(0xFF2563EB)
private val ParticlePurple = Color(0xFF7C3AED)
private val ParticleGray  = Color(0xFFF5F5F5)  // fondo de cards
private val ParticleText  = Color(0xFF212121)   // widget_text_primary
private val ParticleSecondary = Color(0xFF757575) // widget_text_secondary

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
    var showInfoCard by remember { mutableStateOf(true) }

    val eventos by viewModel.eventos.observeAsState(emptyList())
    val favoritosIds by viewModel.favoritos.observeAsState(emptySet())
    val eventosFavoritos = eventos.filter { favoritosIds.contains(it.id) }

    // fondo blanco/gris claro exacto de Particle News Account screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7)) // iOS system gray 6 — igual al fondo de Account en Particle
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // ─── Botón X (cerrar) top right ───────────────────────
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(end = 20.dp, top = 16.dp)) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(32.dp)
                            .background(Color(0xFFE5E5EA), CircleShape)
                            .spatialClickable { navController.navigateUp() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = Color(0xFF3C3C43).copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                    }
                }
            }

            // ─── Header: Avatar + Username + Título (Particle: "cristxpher / Account") ───
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF0E5D8))
                    ) {
                        // Círculo rojo (arriba izquierda)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .offset(x = 10.dp, y = 6.dp)
                                .background(Color(0xFFE83A14), CircleShape)
                        )
                        // Círculo amarillo/naranja (centro)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .offset(x = 14.dp, y = 18.dp)
                                .background(Color(0xFFF6B022).copy(alpha = 0.9f), CircleShape)
                        )
                        // Círculo oscuro/teal (abajo)
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .offset(x = 12.dp, y = 34.dp)
                                .background(Color(0xFF1E3A42), CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        // username pequeño (textColor=widget_text_secondary #757575)
                        val username = currentUser?.email?.substringBefore("@") ?: "Usuario"
                        Text(
                            text = username,
                            color = ParticleSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        )
                        // "Account" — título grande bold, widget_text_headline=#000000
                        Text(
                            text = "Cuenta",
                            color = Color(0xFF000000),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 32.sp
                        )
                    }
                }
            }

            // ─── Info Card dismissable (Particle: "Control Your Preferences") ───
            item {
                AnimatedVisibility(
                    visible = showInfoCard,
                    exit = fadeOut() + shrinkVertically()
                ) {
                    // card blanca redondeada con borde sutil — igual que Particle
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0xFFE5E5EA), RoundedCornerShape(14.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Icono azul flecha abajo (Particle usa flecha download azul)
                        Icon(
                            Icons.Outlined.KeyboardArrowDown,
                            contentDescription = null,
                            tint = ParticleBlue,
                            modifier = Modifier.size(20.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Gestiona tus Preferencias", color = ParticleText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Puedes administrar todos tus eventos guardados y tus preferencias desde aquí.",
                                color = ParticleSecondary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                        // X para descartar
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .spatialClickable { showInfoCard = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Descartar", tint = Color(0xFFAEAEB2), modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            // ─── Estado: no autenticado ────────────────────────────
            if (currentUser == null) {
                item { Spacer(modifier = Modifier.height(16.dp)) }
                item {
                    // Menu card blanca redondeada con opciones (estilo Particle menu items)
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0xFFE5E5EA), RoundedCornerShape(14.dp))
                    ) {
                        ParticleMenuItem(
                            icon = Icons.Outlined.Login,
                            iconTint = ParticleBlue,
                            label = "Iniciar Sesión",
                            onClick = onNavigateToLogin
                        )
                    }
                }
            } else {
                // ─── Menu Items card (Particle: "Content Preferences" + "Edit Profile") ─
                item { Spacer(modifier = Modifier.height(16.dp)) }
                item {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0xFFE5E5EA), RoundedCornerShape(14.dp))
                    ) {
                        ParticleMenuItem(
                            icon = Icons.Outlined.Tune,
                            iconTint = ParticleBlue,
                            label = "Preferencias de contenido",
                            showChevron = true,
                            onClick = {}
                        )
                        // Divider fino entre items (Particle usa línea gris muy sutil)
                        Box(
                            modifier = Modifier
                                .padding(start = 52.dp)
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(Color(0xFFE5E5EA))
                        )
                        ParticleMenuItem(
                            icon = Icons.Outlined.Person,
                            iconTint = ParticleBlue,
                            label = "Editar Perfil",
                            showChevron = false,
                            onClick = {}
                        )
                        Box(
                            modifier = Modifier
                                .padding(start = 52.dp)
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(Color(0xFFE5E5EA))
                        )
                        // Cerrar sesión — rojo
                        ParticleMenuItem(
                            icon = Icons.Outlined.Logout,
                            iconTint = Color(0xFFDC2626),
                            label = "Cerrar Sesión",
                            labelColor = Color(0xFFDC2626),
                            showChevron = false,
                            onClick = {
                                auth.signOut()
                                currentUser = null
                            }
                        )
                    }
                }

                // ─── Favoritos ─────────────────────────────────────────
                item { Spacer(modifier = Modifier.height(28.dp)) }
                item {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tus Favoritos", color = Color(0xFF000000), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        Text("${eventosFavoritos.size}", color = ParticleSecondary, fontSize = 14.sp)
                    }
                }
                item { Spacer(modifier = Modifier.height(12.dp)) }

                if (eventosFavoritos.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(14.dp))
                                .border(1.dp, Color(0xFFE5E5EA), RoundedCornerShape(14.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Outlined.FavoriteBorder, contentDescription = null, tint = Color(0xFFAEAEB2), modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Aún no tienes eventos favoritos.", color = ParticleSecondary, fontSize = 14.sp)
                            }
                        }
                    }
                } else {
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

        // ─── Large CTA pill morado (Particle: "Learn More") ──────────────────
        // Solo se muestra si NO está autenticado
        if (currentUser == null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(ParticlePurple, RoundedCornerShape(32.dp))
                    .spatialClickable { onNavigateToLogin() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Learn More",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.2.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────
// Componente reutilizable: MenuItem estilo Particle News
// icon 20dp azul | label peso 1 | chevron opcional
// ─────────────────────────────────────────────────────
@Composable
private fun ParticleMenuItem(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    labelColor: Color = Color(0xFF000000),
    showChevron: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .spatialClickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // icono de 20dp igual al widget_logo de Particle News
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        // label con weight=1 igual que widget_title en widget_list_layout.xml
        Text(label, color = labelColor, fontSize = 16.sp, fontWeight = FontWeight.Normal, modifier = Modifier.weight(1f))
        if (showChevron) {
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Color(0xFFAEAEB2), modifier = Modifier.size(20.dp))
        }
    }
}
