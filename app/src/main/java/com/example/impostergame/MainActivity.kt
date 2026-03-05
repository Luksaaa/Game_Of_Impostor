package com.example.impostergame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.impostergame.ui.theme.ImposterGameTheme
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ImposterGameTheme {
                ImposterApp()
            }
        }
    }
}

enum class Screen {
    ENTER_NAME, HOME, LOBBY, JOIN, GAME
}

@Composable
fun ImposterApp() {
    var currentScreen by remember { mutableStateOf(Screen.ENTER_NAME) }
    var username by remember { mutableStateOf("") }
    var roomCode by remember { mutableStateOf("") }
    var isAdmin by remember { mutableStateOf(false) }
    var userWord by remember { mutableStateOf("") }
    var isImposter by remember { mutableStateOf(false) }
    
    // Lista hrvatskih riječi
    val croatianWords = listOf(
        "Kruh", "Voda", "Sunce", "Mjesec", "Šuma", "More", "Rijeka", "Planina",
        "Knjiga", "Auto", "Avion", "Škola", "Torba", "Lopta", "Cvijet", "Drvo",
        "Jabuka", "Kruška", "Sladoled", "Kava", "Čaj", "Ručak", "Večera", "Pas",
        "Mačka", "Ptica", "Riba", "Zmija", "Lav", "Slon", "Konj", "Krava"
    )

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (currentScreen) {
                Screen.ENTER_NAME -> {
                    Text("Unesi svoje ime", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { if (username.isNotBlank()) currentScreen = Screen.HOME },
                        modifier = Modifier.fillMaxWidth(0.5f)
                    ) {
                        Text("GO")
                    }
                }

                Screen.HOME -> {
                    Text("Pozdrav, $username!", fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(40.dp))
                    Button(
                        onClick = {
                            roomCode = generateRandomCode()
                            isAdmin = true
                            currentScreen = Screen.LOBBY
                        },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("1. Napravi sobu")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            isAdmin = false
                            currentScreen = Screen.JOIN
                        },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("2. Pridruži se sobi")
                    }
                }

                Screen.JOIN -> {
                    var inputCode by remember { mutableStateOf("") }
                    Text("Unesi kod sobe", fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = inputCode,
                        onValueChange = { if (it.length <= 6) inputCode = it.uppercase() },
                        label = { Text("Kod (npr. ABC123)") },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (inputCode.length == 6) {
                                roomCode = inputCode
                                currentScreen = Screen.LOBBY
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.5f)
                    ) {
                        Text("Pridruži se")
                    }
                    TextButton(onClick = { currentScreen = Screen.HOME }) {
                        Text("Povratak")
                    }
                }

                Screen.LOBBY -> {
                    Text("Soba: $roomCode", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(if (isAdmin) "Ti si ADMIN" else "Čeka se admin...", 
                        fontSize = 18.sp, 
                        color = if (isAdmin) Color(0xFF4CAF50) else Color.Gray)
                    
                    Spacer(modifier = Modifier.height(40.dp))
                    
                    // Simulacija broja igrača (max 8)
                    Text("Igrači: 1 / 8", fontSize = 16.sp)
                    
                    Spacer(modifier = Modifier.height(40.dp))
                    
                    if (isAdmin) {
                        Button(
                            onClick = {
                                // Odabir riječi
                                val mainWord = croatianWords.random()
                                val possibleImposterWords = croatianWords.filter { it != mainWord }
                                val imposterWord = possibleImposterWords.random()
                                
                                // U pravoj multiplayer aplikaciji, ovdje bi server dodijelio uloge.
                                // Za demo, admin simulira ulogu (npr. admin je 50% šanse imposter)
                                isImposter = Random.nextBoolean()
                                userWord = if (isImposter) imposterWord else mainWord
                                
                                currentScreen = Screen.GAME
                            },
                            modifier = Modifier.fillMaxWidth(0.6f)
                        ) {
                            Text("KRENI")
                        }
                    } else {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(0.6f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Igra će uskoro početi...")
                        
                        // Demo gumb za pridružene igrače da vide ekran igre
                        Button(onClick = {
                             userWord = croatianWords.random()
                             isImposter = false
                             currentScreen = Screen.GAME
                        }, modifier = Modifier.padding(top = 20.dp)) {
                            Text("Simuliraj start (Demo)")
                        }
                    }
                }

                Screen.GAME -> {
                    Text("Tvoja uloga i riječ:", fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isImposter) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = userWord,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isImposter) Color.Red else Color(0xFF2E7D32)
                            )
                            if (isImposter) {
                                Text("TI SI IMPOSTER!", fontWeight = FontWeight.ExtraBold, color = Color.Red)
                            } else {
                                Text("Ti si običan igrač", color = Color(0xFF2E7D32))
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    Button(onClick = { currentScreen = Screen.HOME }) {
                        Text("Nova igra")
                    }
                }
            }
        }
    }
}

fun generateRandomCode(): String {
    val letters = ('A'..'Z').toList()
    val numbers = ('0'..'9').toList()
    val randomLetters = (1..3).map { letters.random() }.joinToString("")
    val randomNumbers = (1..3).map { numbers.random() }.joinToString("")
    return randomLetters + randomNumbers
}
