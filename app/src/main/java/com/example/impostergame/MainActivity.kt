package com.example.impostergame

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.impostergame.ui.theme.ImposterGameTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        
        val sharedPref = getSharedPreferences("ImposterGamePrefs", Context.MODE_PRIVATE)
        val savedUsername = sharedPref.getString("username", "") ?: ""
        
        // Provjera Deep Linka (QR kod)
        val action: String? = intent?.action
        val data: Uri? = intent?.data
        var initialRoomCode = ""
        
        if (Intent.ACTION_VIEW == action && data != null) {
            initialRoomCode = data.getQueryParameter("code") ?: ""
        }

        enableEdgeToEdge()
        setContent {
            ImposterGameTheme {
                ImposterApp(savedUsername, initialRoomCode) { newName, rememberMe ->
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
fun ImposterApp(initialUsername: String, qrRoomCode: String, onNameSaved: (String, Boolean) -> Unit) {
    var username by remember { mutableStateOf(initialUsername) }
    var roomCode by remember { mutableStateOf(qrRoomCode) }
    
    var currentScreen by remember { 
        mutableStateOf(if (username.isBlank()) Screen.ENTER_NAME else if (qrRoomCode.isNotBlank()) Screen.LOBBY else Screen.HOME) 
    }
    var isAdmin by remember { mutableStateOf(false) }
    
    val database = remember { Firebase.database("https://gameofimpostor-default-rtdb.europe-west1.firebasedatabase.app/").getReference("rooms") }

    // Automatsko spajanje ako postoji QR kod i username
    LaunchedEffect(qrRoomCode) {
        if (qrRoomCode.isNotBlank() && username.isNotBlank()) {
            joinRoomLogic(database, qrRoomCode, username) {
                currentScreen = Screen.LOBBY
            }
        }
    }

    when (currentScreen) {
        Screen.ENTER_NAME -> EnterNameScreen { name, rememberMe ->
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
            onLeaveRoom = {
                currentScreen = Screen.HOME
                roomCode = ""
            },
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
                roomCode = ""
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
            if (playersSnapshot.childrenCount < 8) {
                database.child(code).child("players").child(username).setValue(mapOf("name" to username, "isReady" to false)).addOnSuccessListener {
                    database.child(code).child("messages").push().setValue("$username je ušao")
                    onSuccess()
                }
            }
        }
    }
}
