package com.example.impostergame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.impostergame.ui.theme.ImposterGameTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            ImposterGameTheme {
                ImposterApp()
            }
        }
    }
}

@Composable
fun ImposterApp() {
    var currentScreen by remember { mutableStateOf(Screen.ENTER_NAME) }
    var username by remember { mutableStateOf("") }
    var roomCode by remember { mutableStateOf("") }
    var isAdmin by remember { mutableStateOf(false) }

    when (currentScreen) {
        Screen.ENTER_NAME -> EnterNameScreen { name ->
            username = name
            currentScreen = Screen.HOME
        }
        Screen.HOME -> HomeScreen(
            username = username,
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
            onJoined = { code ->
                roomCode = code
                currentScreen = Screen.LOBBY
            },
            onBack = { currentScreen = Screen.HOME }
        )
        Screen.LOBBY -> LobbyScreen(
            roomCode = roomCode,
            username = username,
            isAdmin = isAdmin,
            onGameStarted = {
                currentScreen = Screen.GAME
            }
        )
        Screen.GAME -> GameScreen(
            roomCode = roomCode,
            username = username,
            onNewGame = {
                currentScreen = Screen.HOME
            }
        )
    }
}
