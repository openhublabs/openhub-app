package dev.openhub.app.ui.compose

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage
import dev.openhub.app.model.Evento
import dev.openhub.app.ui.EventoViewModel
import dev.openhub.app.ui.theme.spatialClickable
import kotlinx.coroutines.launch
import dev.openhub.app.util.WordValidationService

// Simple Wordle logic
enum class LetterState { UNKNOWN, CORRECT, PRESENT, ABSENT }

data class WordleState(
    val targetWord: String,
    val guesses: List<String> = emptyList(),
    val isCompleted: Boolean = false,
    val currentGuess: String = ""
)


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DescifraScreen(
    viewModel: EventoViewModel,
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    innerPadding: PaddingValues
) {
    val eventos: List<Evento> by viewModel.eventos.observeAsState(emptyList())
    val topEvento = eventos.firstOrNull()

    // Dialog state
    var showWordleDialog by remember { mutableStateOf(false) }

    // Words for past days
    val days = listOf("L", "M", "M", "J", "V", "S", "Hoy")
    var selectedDayIndex by remember { mutableIntStateOf(6) } // Default to "Hoy"
    
    // Scroll state
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = innerPadding.calculateTopPadding()),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            var dominantColor by remember { mutableStateOf(Color.DarkGray) }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .clip(RoundedCornerShape(32.dp))
            ) {
                if (topEvento != null) {
                    AsyncImage(
                        model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                            .data(topEvento.imagenUrl)
                            .crossfade(true)
                            .allowHardware(false)
                            .build(),
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
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                // Gradient overlay over the image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    dominantColor.copy(alpha = 0.4f),
                                    dominantColor.copy(alpha = 0.9f)
                                )
                            )
                        )
                )
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text("Hoy", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = topEvento?.titulo ?: "A Tale of Two Launches",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 36.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Editado por: OpenHub", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("Descifra el evento de hoy", color = Color.White, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { showWordleDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("Descifra", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                days.forEachIndexed { index, day ->
                    val isSelected = index == selectedDayIndex
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .clip(if (day == "Hoy") RoundedCornerShape(20.dp) else CircleShape)
                            .background(if (isSelected) Color(0xFF9B6FE6) else Color(0xFFF0F0F0))
                            .clickable { selectedDayIndex = index }
                            .padding(horizontal = if (day == "Hoy") 16.dp else 0.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day == "Hoy") {
                            Text(day, color = if (isSelected) Color.White else Color(0xFFA0A0A0), fontWeight = FontWeight.Bold)
                        } else {
                            Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                                Text(day, color = if (isSelected) Color.White else Color(0xFFA0A0A0), fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
            Text(
                "¡Inicia una racha!",
                color = Color(0xFFA0A0A0),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                textAlign = TextAlign.Center
            )
        }

        // Related articles
        item { Spacer(modifier = Modifier.height(48.dp)) }
        items(eventos.take(3)) { evento ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF2F4F8))
                    .padding(12.dp)
                    .spatialClickable {
                        viewModel.seleccionarEvento(evento)
                        navController.navigate(Screen.Detalle.route)
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dev.openhub.app.util.EventoUtils.capitalizarPalabras(evento.titulo),
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                )
                AsyncImage(
                    model = evento.imagenUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }

    if (showWordleDialog) {
        WordleDialog(eventos = eventos, onDismiss = { showWordleDialog = false })
    }
}

@Composable
fun WordleDialog(eventos: List<Evento>, onDismiss: () -> Unit) {
    val thematicWords = remember(eventos) { WordValidationService.extractThematicWords(eventos) }
    var currentState by remember { mutableStateOf(WordleState(thematicWords.randomOrNull() ?: "EVENT")) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE5E5EA), RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Descifra el Evento", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    IconButton(onClick = onDismiss) {
                        Text("X", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                }
                
                Text("Pista: Relacionado a los eventos de la comunidad", fontSize = 12.sp, color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                
                var errorMessage by remember { mutableStateOf("") }
                
                LaunchedEffect(errorMessage) {
                    if (errorMessage.isNotEmpty()) {
                        kotlinx.coroutines.delay(2000)
                        errorMessage = ""
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                if (errorMessage.isNotEmpty()) {
                    Text(errorMessage, color = Color.Red, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                } else {
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Wordle Board
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    for (row in 0 until 6) {
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (col in 0 until 5) {
                                val letter = when {
                                    row < currentState.guesses.size -> currentState.guesses[row][col].toString()
                                    row == currentState.guesses.size && col < currentState.currentGuess.length -> currentState.currentGuess[col].toString()
                                    else -> ""
                                }
                                
                                val state = if (row < currentState.guesses.size) {
                                    val guess = currentState.guesses[row]
                                    val target = currentState.targetWord
                                    if (guess[col] == target[col]) LetterState.CORRECT
                                    else if (target.contains(guess[col])) LetterState.PRESENT
                                    else LetterState.ABSENT
                                } else LetterState.UNKNOWN

                                val bgColor = when (state) {
                                    LetterState.CORRECT -> Color(0xFF538D4E)
                                    LetterState.PRESENT -> Color(0xFFB59F3B)
                                    LetterState.ABSENT -> Color(0xFF757575)
                                    LetterState.UNKNOWN -> Color.Transparent
                                }
                                val textColor = if (state == LetterState.UNKNOWN) Color.Black else Color.White
                                val borderColor = if (state == LetterState.UNKNOWN) Color.LightGray else Color.Transparent

                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .border(2.dp, borderColor, RoundedCornerShape(8.dp))
                                        .background(bgColor, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = letter,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (currentState.isCompleted) {
                    val isWin = currentState.guesses.lastOrNull() == currentState.targetWord
                    Text(
                        text = if (isWin) "¡Ganaste!" else "La palabra era ${currentState.targetWord}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isWin) Color(0xFF538D4E) else Color.Red
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            currentState = WordleState(thematicWords.randomOrNull() ?: "EVENT")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B6FE6))
                    ) {
                        Text("Siguiente Palabra", color = Color.White)
                    }
                } else {
                    WordleKeyboard(
                        state = currentState,
                        onKeyPress = { key ->
                            if (currentState.isCompleted) return@WordleKeyboard
                            
                            if (key == "ENTER") {
                                if (currentState.currentGuess.length == 5) {
                                    if (WordValidationService.isValidWord(currentState.currentGuess, thematicWords)) {
                                        val newGuesses = currentState.guesses + currentState.currentGuess
                                        val isWin = currentState.currentGuess == currentState.targetWord
                                        val isLoss = newGuesses.size >= 6 && !isWin
                                        currentState = currentState.copy(
                                            guesses = newGuesses,
                                            currentGuess = "",
                                            isCompleted = isWin || isLoss
                                        )
                                    } else {
                                        errorMessage = "Palabra no válida"
                                    }
                                } else {
                                    errorMessage = "Faltan letras"
                                }
                            } else if (key == "DEL") {
                                if (currentState.currentGuess.isNotEmpty()) {
                                    currentState = currentState.copy(
                                        currentGuess = currentState.currentGuess.dropLast(1)
                                    )
                                }
                            } else {
                                if (currentState.currentGuess.length < 5) {
                                    currentState = currentState.copy(
                                        currentGuess = currentState.currentGuess + key
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WordleKeyboard(state: WordleState, onKeyPress: (String) -> Unit) {
    val rows = listOf(
        listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
        listOf("A", "S", "D", "F", "G", "H", "J", "K", "L", "Ñ"),
        listOf("ENTER", "Z", "X", "C", "V", "B", "N", "M", "DEL")
    )
    
    val keyStates = mutableMapOf<String, LetterState>()
    state.guesses.forEach { guess ->
        guess.forEachIndexed { col, char ->
            val charStr = char.toString()
            val targetChar = state.targetWord[col]
            if (char == targetChar) {
                keyStates[charStr] = LetterState.CORRECT
            } else if (state.targetWord.contains(char)) {
                if (keyStates[charStr] != LetterState.CORRECT) {
                    keyStates[charStr] = LetterState.PRESENT
                }
            } else {
                if (keyStates[charStr] != LetterState.CORRECT && keyStates[charStr] != LetterState.PRESENT) {
                    keyStates[charStr] = LetterState.ABSENT
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                row.forEach { key ->
                    val isAction = key == "ENTER" || key == "DEL"
                    val keyState = keyStates[key] ?: LetterState.UNKNOWN
                    
                    val bgColor = when (keyState) {
                        LetterState.CORRECT -> Color(0xFF538D4E)
                        LetterState.PRESENT -> Color(0xFFB59F3B)
                        LetterState.ABSENT -> Color(0xFF3A3A3C)
                        LetterState.UNKNOWN -> Color(0xFFD3D6DA)
                    }
                    val textColor = if (keyState == LetterState.UNKNOWN) Color.Black else Color.White

                    Box(
                        modifier = Modifier
                            .weight(if (isAction) 1.5f else 1f)
                            .padding(horizontal = 2.dp)
                            .height(48.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(bgColor)
                            .clickable { onKeyPress(key) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = key,
                            fontSize = if (isAction) 10.sp else 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}
