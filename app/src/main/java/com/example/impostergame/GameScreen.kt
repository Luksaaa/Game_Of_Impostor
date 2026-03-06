package com.example.impostergame

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.impostergame.ui.theme.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

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
    var showHoldMessage by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableStateOf(0f) }
    var currentAdmin by remember { mutableStateOf("") }
    
    val isUserAdmin = currentAdmin == username
    val scope = rememberCoroutineScope()

    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val containerColor = if (isDarkTheme) DarkInputGray else Color.White

    DisposableEffect(roomCode) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return
                val status = snapshot.child("status").getValue(String::class.java)
                currentAdmin = snapshot.child("admin").getValue(String::class.java) ?: ""
                
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Box(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center
        ) {
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
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showAdminOnlyMessage || showHoldMessage,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = if (showAdminOnlyMessage) "Samo admin može ponoviti igru" else "Zadrži 3 sekunde za ponavljanje",
                        color = if (showAdminOnlyMessage) Color.Red else BlueGradient,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            var holdJob by remember { mutableStateOf<Job?>(null) }

            // Zamjena Buttona s Box-om za bolju kontrolu gesti
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isUserAdmin) PurpleGradient else PurpleGradient.copy(alpha = 0.3f)
                    )
                    .pointerInput(isUserAdmin) {
                        if (isUserAdmin) {
                            detectTapGestures(
                                onTap = {
                                    if (!showHoldMessage) {
                                        showHoldMessage = true
                                        scope.launch {
                                            delay(3000)
                                            if (holdProgress == 0f) showHoldMessage = false
                                        }
                                    }
                                },
                                onPress = {
                                    showHoldMessage = true
                                    holdJob = scope.launch {
                                        val startTime = System.currentTimeMillis()
                                        while (holdProgress < 3f) {
                                            val elapsed = System.currentTimeMillis() - startTime
                                            holdProgress = (elapsed / 1000f).coerceAtMost(3f)
                                            delay(10)
                                        }
                                        database.child("status").setValue("waiting")
                                        holdProgress = 0f
                                        showHoldMessage = false
                                    }
                                    try {
                                        awaitRelease()
                                    } finally {
                                        holdJob?.cancel()
                                        holdProgress = 0f
                                    }
                                }
                            )
                        } else {
                            detectTapGestures(
                                onTap = {
                                    if (!showAdminOnlyMessage) {
                                        showAdminOnlyMessage = true
                                        scope.launch {
                                            delay(3000)
                                            showAdminOnlyMessage = false
                                        }
                                    }
                                }
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isUserAdmin && holdProgress > 0f) String.format(Locale.US, "%.2fs", holdProgress) else "PONOVI",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    FirebaseManager.leaveRoomWithAdminTransfer(roomCode, username, onNewGame)
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
