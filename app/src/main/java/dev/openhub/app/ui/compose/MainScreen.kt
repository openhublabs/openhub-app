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
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
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
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
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

sealed class Screen(val route: String, val title: String, val icon: ImageVector?) {
    object Feed : Screen("feed", "Inicio", Icons.Outlined.Home)
    object Explorar : Screen("explorar", "Explorar", Icons.Outlined.Explore)
    object Categorias : Screen("categorias", "Categorías", Icons.Outlined.Category)
    object Historial : Screen("historial", "Historial", Icons.Outlined.DateRange)
    object Buscar : Screen("buscar", "Buscar", Icons.Outlined.Search)
    object Detalle : Screen("detalle", "Detalle", null)
    object Splash : Screen("splash", "Splash", null)
    object Perfil : Screen("perfil", "Perfil", null)
}

val bottomNavItems = listOf(
    Screen.Feed,
    Screen.Explorar,
    Screen.Categorias,
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

    val context = LocalContext.current
    
    val videoUrls = listOf(
        "android.resource://${context.packageName}/${R.raw.video_night}",
        "android.resource://${context.packageName}/${R.raw.video_explore}",
        "android.resource://${context.packageName}/${R.raw.video_day}"
    )

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
            volume = 0f
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    var activeVideoUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentRoute) {
        val targetVideoUrl = when (currentRoute) {
            Screen.Feed.route -> videoUrls[0]
            Screen.Explorar.route, Screen.Detalle.route -> videoUrls[1]
            else -> videoUrls[2]
        }
        
        if (activeVideoUrl != targetVideoUrl) {
            activeVideoUrl = targetVideoUrl
            exoPlayer.setMediaItem(MediaItem.fromUri(targetVideoUrl))
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    SharedTransitionLayout {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            bottomBar = {
                AnimatedVisibility(
                    visible = currentRoute != Screen.Detalle.route && currentRoute != Screen.Splash.route,
                    enter = fadeIn(animationSpec = tween(400, delayMillis = 400)) +
                            slideInVertically(animationSpec = tween(400, delayMillis = 400), initialOffsetY = { it }),
                    exit = fadeOut(animationSpec = tween(400)) +
                           slideOutVertically(animationSpec = tween(400), targetOffsetY = { it })
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
                    .background(Color.Black)
            ) {
                if (currentRoute != Screen.Splash.route) {
                    Box(modifier = Modifier.matchParentSize().haze(hazeState)) {
                        AndroidView(
                            factory = { ctx ->
                                PlayerView(ctx).apply {
                                    player = exoPlayer
                                    useController = false
                                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                    layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f))
                        )
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = Screen.Splash.route,
                    enterTransition = {
                        val duration = if (initialState.destination.route == Screen.Splash.route) 400 else 150
                        fadeIn(animationSpec = tween(duration))
                    },
                    exitTransition = {
                        val duration = if (targetState.destination.route == Screen.Splash.route) 400 else 150
                        fadeOut(animationSpec = tween(duration))
                    },
                    popEnterTransition = { fadeIn(animationSpec = tween(150)) },
                    popExitTransition = { fadeOut(animationSpec = tween(150)) },
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
                    composable(Screen.Categorias.route) {
                        CategoriasScreen(viewModel, navController, this@SharedTransitionLayout, this, innerPadding)
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
                        PerfilScreen(viewModel, navController, onNavigateToLogin)
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
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .fillMaxWidth()
            .height(72.dp)
            .shadow(32.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.6f))
            .clip(CircleShape)
            .hazeChild(state = hazeState, shape = CircleShape, blurRadius = 64.dp, tint = Color.White.copy(alpha = 0.15f))
            .border(
                width = 1.5.dp, 
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.6f),
                        Color.White.copy(alpha = 0.1f),
                        Color.Transparent,
                        Color.White.copy(alpha = 0.1f),
                        Color.White.copy(alpha = 0.4f)
                    )
                ), 
                shape = CircleShape
            )
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
                        tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(if (isSelected) 28.dp else 24.dp)
                    )
                }
            }
        }
    }
}
