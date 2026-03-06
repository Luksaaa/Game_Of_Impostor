package com.example.impostergame

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.impostergame.ui.components.AnimatedBackground
import com.example.impostergame.ui.theme.ImposterGameTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        
        val sharedPref = getSharedPreferences("ImposterGamePrefs", Context.MODE_PRIVATE)
        val savedUsername = sharedPref.getString("username", "") ?: ""
        val savedRoomCode = sharedPref.getString("persistentRoomCode", "") ?: ""
        val savedIsAdmin = sharedPref.getBoolean("persistentIsAdmin", false)
        
        // Provjera Deep Linka (QR kod)
        val action: String? = intent?.action
        val data: Uri? = intent?.data
        var qrRoomCode = ""
        
        if (Intent.ACTION_VIEW == action && data != null) {
            qrRoomCode = data.getQueryParameter("code") ?: ""
        }

        // Ako imamo QR kod, on ima prednost nad spremljenom sobom
        val initialRoomCode = if (qrRoomCode.isNotBlank()) qrRoomCode else savedRoomCode

        enableEdgeToEdge()
        setContent {
            ImposterGameTheme {
                val infiniteTransition = rememberInfiniteTransition(label = "global_background")
                
                val xOffset by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 400f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(10000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "x"
                )
                
                val yOffset by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 800f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(15000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "y"
                )

                AnimatedBackground(xOffset = xOffset, yOffset = yOffset) {
                    ImposterApp(savedUsername, initialRoomCode, savedIsAdmin) { newName, rememberMe ->
                        if (rememberMe) {
                            sharedPref.edit().putString("username", newName).apply()
                        } else {
                            sharedPref.edit().remove("username").apply()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ImposterApp(initialUsername: String, initialRoomCode: String, initialIsAdmin: Boolean, onNameSaved: (String, Boolean) -> Unit) {
    var username by remember { mutableStateOf(initialUsername) }
    var roomCode by remember { mutableStateOf(initialRoomCode) }
    var isAdmin by remember { mutableStateOf(initialIsAdmin) }
    
    var currentScreen by remember { 
        mutableStateOf(
            if (username.isBlank()) Screen.ENTER_NAME 
            else if (roomCode.isNotBlank()) Screen.LOBBY 
            else Screen.HOME
        ) 
    }
    
    val context = LocalContext.current
    val database = remember { Firebase.database("https://gameofimpostor-default-rtdb.europe-west1.firebasedatabase.app/").getReference("rooms") }

    // Spremanje sobe za perzistentnost
    LaunchedEffect(roomCode, isAdmin) {
        val sharedPref = context.getSharedPreferences("ImposterGamePrefs", Context.MODE_PRIVATE)
        sharedPref.edit()
            .putString("persistentRoomCode", roomCode)
            .putBoolean("persistentIsAdmin", isAdmin)
            .apply()
    }

    // Automatsko spajanje (za QR kod ili perzistentnu sesiju)
    LaunchedEffect(roomCode) {
        if (roomCode.isNotBlank() && username.isNotBlank()) {
            joinRoomLogic(database, roomCode, username) {
                // Ako smo se uspješno spojili (ili smo već unutra), osiguravamo da smo na LOBBY ekranu
                // (LobbyScreen će nas sam prebaciti u GAME ako je igra u tijeku)
                if (currentScreen != Screen.GAME) {
                    currentScreen = Screen.LOBBY
                }
            }
        }
    }

    // Rukovanje Back gumbom ovisno o ekranu
    when (currentScreen) {
        Screen.JOIN -> {
            BackHandler {
                currentScreen = Screen.HOME
            }
        }
        Screen.LOBBY -> {
            BackHandler {
                FirebaseManager.leaveRoomWithAdminTransfer(roomCode, username) {
                    currentScreen = Screen.HOME
                    roomCode = ""
                    isAdmin = false
                }
            }
        }
        Screen.GAME -> {
            BackHandler {
                FirebaseManager.leaveRoomWithAdminTransfer(roomCode, username) {
                    currentScreen = Screen.HOME
                    roomCode = ""
                    isAdmin = false
                }
            }
        }
        else -> { /* Na HOME i ENTER_NAME dozvoli standardni Back (izlaz iz aplikacije) */ }
    }

    when (currentScreen) {
        Screen.ENTER_NAME -> EnterNameScreen(noBackground = true) { name, rememberMe ->
            username = name
            onNameSaved(name, rememberMe)
            if (roomCode.isNotBlank()) {
                joinRoomLogic(database, roomCode, name) {
                    currentScreen = Screen.LOBBY
                }
            } else {
                currentScreen = Screen.HOME
            }
        }
        Screen.HOME -> HomeScreen(
            username = username,
            noBackground = true,
            onCreateRoom = {
                FirebaseManager.generateRoom(username) { code ->
                    roomCode = code
                    isAdmin = true
                    currentScreen = Screen.LOBBY
                }
            },
            onJoinRoom = {
                isAdmin = false
                currentScreen = Screen.JOIN
            }
        )
        Screen.JOIN -> JoinRoomScreen(
            username = username,
            noBackground = true,
            onJoined = { code ->
                roomCode = code
                isAdmin = false
                currentScreen = Screen.LOBBY
            },
            onBack = { currentScreen = Screen.HOME }
        )
        Screen.LOBBY -> LobbyScreen(
            roomCode = roomCode,
            username = username,
            isAdmin = isAdmin,
            noBackground = true,
            onLeaveRoom = {
                currentScreen = Screen.HOME
                roomCode = ""
                isAdmin = false
            },
            onGameStarted = {
                currentScreen = Screen.GAME
            }
        )
        Screen.GAME -> GameScreen(
            roomCode = roomCode,
            username = username,
            isAdmin = isAdmin,
            noBackground = true,
            onRepeat = {
                currentScreen = Screen.LOBBY
            },
            onNewGame = {
                currentScreen = Screen.HOME
                roomCode = ""
                isAdmin = false
            }
        )
    }
}

private fun joinRoomLogic(
    database: com.google.firebase.database.DatabaseReference,
    code: String,
    username: String,
    onSuccess: () -> Unit
) {
    database.child(code).get().addOnSuccessListener { snapshot ->
        if (snapshot.exists()) {
            val playersSnapshot = snapshot.child("players")
            // Ako je igrač već unutra (npr. rekonekcija), samo nastavi
            if (playersSnapshot.hasChild(username)) {
                onSuccess()
            } else if (playersSnapshot.childrenCount < 8) {
                database.child(code).child("players").child(username).setValue(mapOf("name" to username, "isReady" to false)).addOnSuccessListener {
                    database.child(code).child("messages").push().setValue("$username je ušao")
                    onSuccess()
                }
            }
        }
    }
}
