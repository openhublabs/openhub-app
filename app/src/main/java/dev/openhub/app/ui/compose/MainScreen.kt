package dev.openhub.app.ui.compose

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
// imports media3 removed
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.openhub.app.R
import dev.openhub.app.ui.EventoViewModel
import dev.openhub.app.ui.theme.WhiteBackground
import dev.openhub.app.ui.theme.LiquidGlassStrongShadow
import dev.openhub.app.ui.theme.liquidGlass

sealed class Screen(val route: String, val title: String, val icon: ImageVector?) {
    object Feed : Screen("feed", "Inicio", Icons.Outlined.Home)
    object Explorar : Screen("explorar", "Explorar", Icons.Outlined.Explore)
    object Descifra : Screen("descifra", "Descifra", Icons.Outlined.Apps)
    object Historial : Screen("historial", "Historial", Icons.Outlined.DateRange)
    object Buscar : Screen("buscar", "Buscar", Icons.Outlined.Search)
    object Detalle : Screen("detalle", "Detalle", null)
    object Splash : Screen("splash", "Splash", null)
    object Perfil : Screen("perfil", "Perfil", null)
}

val bottomNavItems = listOf(
    Screen.Feed,
    Screen.Explorar,
    Screen.Descifra,
    Screen.Historial,
    Screen.Buscar
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MainScreen(viewModel: EventoViewModel, onNavigateToLogin: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val hazeState = remember { HazeState() }

    // Se han eliminado los videos de fondo para el tema claro minimalista

    SharedTransitionLayout {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            bottomBar = {
                AnimatedVisibility(
                    visible = currentRoute != Screen.Detalle.route && currentRoute != Screen.Splash.route && currentRoute != Screen.Perfil.route,
                    enter = fadeIn(animationSpec = tween(250)) +
                            slideInVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow), initialOffsetY = { it }),
                    exit = fadeOut(animationSpec = tween(250)) +
                           slideOutVertically(animationSpec = tween(250), targetOffsetY = { it })
                ) {
                    Box(modifier = Modifier.navigationBarsPadding()) {
                        GlassBottomNavigation(navController = navController, currentRoute = currentRoute, hazeState = hazeState)
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(WhiteBackground)
            ) {
                // Subtle top gradient like Particle News
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFEBF2F8), Color.Transparent)
                        )
                    )
                )
                
                Box(modifier = Modifier.fillMaxSize()) {
                    NavHost(
                    navController = navController,
                    startDestination = Screen.Splash.route,
                    enterTransition = {
                        fadeIn(animationSpec = tween(300))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(300))
                    },
                    popEnterTransition = {
                        fadeIn(animationSpec = tween(300))
                    },
                    popExitTransition = {
                        fadeOut(animationSpec = tween(300))
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(Screen.Splash.route) {
                        SplashScreen(navController)
                    }
                    composable(Screen.Feed.route) {
                        FeedScreen(viewModel, navController, this@SharedTransitionLayout, this, innerPadding)
                    }
                    composable(Screen.Explorar.route) {
                        ExplorarScreen(viewModel, navController, this@SharedTransitionLayout, this, innerPadding)
                    }
                    composable(Screen.Descifra.route) {
                        DescifraScreen(
                            viewModel = viewModel,
                            navController = navController,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this,
                            innerPadding = innerPadding
                        )
                    }
                    composable(Screen.Historial.route) {
                        HistorialScreen(viewModel, navController, this@SharedTransitionLayout, this, innerPadding)
                    }
                    composable(Screen.Buscar.route) {
                        BuscarScreen(viewModel, navController, this@SharedTransitionLayout, this, innerPadding)
                    }
                    composable(Screen.Detalle.route) {
                        DetailScreen(viewModel, navController, this@SharedTransitionLayout, this)
                    }
                    composable(Screen.Perfil.route) {
                        PerfilScreen(viewModel, navController, onNavigateToLogin, this@SharedTransitionLayout, this)
                    }
                }
            }
        }
        }
    }
}

@Composable
fun GlassBottomNavigation(navController: NavController, currentRoute: String?, hazeState: HazeState) {
    Row(
        modifier = Modifier
            .padding(horizontal = 32.dp, vertical = 24.dp)
            .fillMaxWidth()
            .height(64.dp)
            .shadow(24.dp, CircleShape, spotColor = LiquidGlassStrongShadow)
            .background(Color(0xE6FFFFFF), CircleShape) // Translucent white instead of haze
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.route
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .clickable {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (item.icon != null) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = if (isSelected) Color.Black else Color.Black.copy(alpha = 0.4f),
                        modifier = Modifier.size(if (isSelected) 28.dp else 24.dp)
                    )
                }
            }
        }
    }
}
