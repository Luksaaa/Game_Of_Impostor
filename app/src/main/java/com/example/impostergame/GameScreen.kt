package com.example.impostergame

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun GameScreen(roomCode: String, username: String, onNewGame: () -> Unit) {
    val database = Firebase.database("https://gameofimpostor-default-rtdb.europe-west1.firebasedatabase.app/").getReference("rooms").child(roomCode)
    
    var role by remember { mutableStateOf("") }
    var word by remember { mutableStateOf("") }
    var isRevealed by remember { mutableStateOf(false) }

    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val containerColor = if (isDarkTheme) DarkInputGray else Color.White

    LaunchedEffect(Unit) {
        database.get().addOnSuccessListener { snapshot ->
            val imposterId = snapshot.child("imposterId").getValue(String::class.java)
            if (imposterId == username) {
                role = "IMPOSTOR"
                word = snapshot.child("imposterWord").getValue(String::class.java) ?: ""
            } else {
                role = "CREWMATE"
                word = snapshot.child("mainWord").getValue(String::class.java) ?: ""
            }
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("TVOJA ULOGA", fontSize = 16.sp, color = textColor.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                
                Surface(
                    color = if (role == "IMPOSTOR") ImposterRed.copy(alpha = 0.1f) else CrewmateGreen.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = role,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (role == "IMPOSTOR") ImposterRed else CrewmateGreen
                    )
                }
            }

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
                            Text("Tvoja tajna riječ:", fontSize = 18.sp, color = textColor.copy(alpha = 0.6f))
                            Text(
                                text = word,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BlueGradient,
                                modifier = Modifier.padding(top = 16.dp),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("(Klikni za sakrivanje)", fontSize = 12.sp, color = textColor.copy(alpha = 0.3f))
                        }
                    } else {
                        Button(
                            onClick = { isRevealed = true },
                            modifier = Modifier
                                .width(220.dp)
                                .height(100.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BlueGradient)
                        ) {
                            Text(
                                text = "POGLEDAJ RIJEČ", 
                                fontSize = 18.sp, 
                                fontWeight = FontWeight.Bold, 
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onNewGame,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = textColor.copy(alpha = 0.1f))
            ) {
                Text("NOVA IGRA", color = textColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}
