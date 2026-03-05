package com.example.impostergame

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.impostergame.ui.components.AnimatedBackground
import com.example.impostergame.ui.theme.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GameScreen(
    roomCode: String, 
    username: String, 
    isAdmin: Boolean,
    onRepeat: () -> Unit,
    onNewGame: () -> Unit
) {
    if (roomCode.isBlank()) return

    val database = remember(roomCode) { 
        Firebase.database("https://gameofimpostor-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("rooms").child(roomCode) 
    }
    
    var word by remember { mutableStateOf("") }
    var isRevealed by remember { mutableStateOf(false) }
    var showAdminOnlyMessage by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val containerColor = if (isDarkTheme) DarkInputGray else Color.White

    // Koristimo DisposableEffect kako bi ispravno uklonili slušača kada korisnik izađe
    DisposableEffect(roomCode) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return
                val status = snapshot.child("status").getValue(String::class.java)
                
                // Samo ako je status "waiting", vraćamo se u lobby
                if (status == "waiting") {
                    onRepeat()
                }

                val imposterId = snapshot.child("imposterId").getValue(String::class.java)
                word = if (imposterId == username) {
                    snapshot.child("imposterWord").getValue(String::class.java) ?: ""
                } else {
                    snapshot.child("mainWord").getValue(String::class.java) ?: ""
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        
        database.addValueEventListener(listener)
        
        onDispose {
            database.removeEventListener(listener)
        }
    }

    AnimatedBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = containerColor.copy(alpha = 0.9f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isRevealed) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { isRevealed = false },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Tvoja tajna riječ:",
                                color = textColor.copy(alpha = 0.7f),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = word,
                                color = textColor,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { isRevealed = true },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Vidi",
                                tint = PurpleGradient,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Dodirni za otkrivanje",
                                color = textColor.copy(alpha = 0.5f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showAdminOnlyMessage,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = "Samo admin može ponoviti igru",
                            color = Color.Red,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Button(
                    onClick = {
                        if (isAdmin) {
                            database.child("status").setValue("waiting")
                        } else {
                            if (!showAdminOnlyMessage) {
                                showAdminOnlyMessage = true
                                scope.launch {
                                    delay(3000)
                                    showAdminOnlyMessage = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    enabled = true,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAdmin) PurpleGradient else PurpleGradient.copy(alpha = 0.3f),
                        contentColor = Color.White
                    )
                ) {
                    Text("PONOVI", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        // Čist izlazak: brišemo listener i podatke
                        database.child("players").child(username).removeValue()
                        database.child("messages").push().setValue("$username je izašao")
                        onNewGame()
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = textColor.copy(alpha = 0.1f))
                ) {
                    Text("IZAĐI IZ SOBE", color = textColor, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
