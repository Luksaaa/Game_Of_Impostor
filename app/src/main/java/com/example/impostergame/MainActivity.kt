package com.example.impostergame

import android.content.Context
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
        
        val sharedPref = getSharedPreferences("ImposterGamePrefs", Context.MODE_PRIVATE)
        val savedUsername = sharedPref.getString("username", "") ?: ""
        
        enableEdgeToEdge()
        setContent {
            ImposterGameTheme {
                ImposterApp(savedUsername) { newName, rememberMe ->
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

@Composable
fun ImposterApp(initialUsername: String, onNameSaved: (String, Boolean) -> Unit) {
    var username by remember { mutableStateOf(initialUsername) }
    var currentScreen by remember { 
        mutableStateOf(if (username.isBlank()) Screen.ENTER_NAME else Screen.HOME) 
    }
    var roomCode by remember { mutableStateOf("") }
    var isAdmin by remember { mutableStateOf(false) }

    when (currentScreen) {
        Screen.ENTER_NAME -> EnterNameScreen { name, rememberMe ->
            username = name
            onNameSaved(name, rememberMe)
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
            isAdmin = isAdmin,
            onRepeat = {
                currentScreen = Screen.LOBBY
            },
            onNewGame = {
                currentScreen = Screen.HOME
            }
        )
    }
}
