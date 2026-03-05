package com.example.impostergame

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
fun LobbyScreen(roomCode: String, username: String, isAdmin: Boolean, onGameStarted: () -> Unit) {
    val database = Firebase.database("https://gameofimpostor-default-rtdb.europe-west1.firebasedatabase.app/").getReference("rooms").child(roomCode)
    
    var messages by remember { mutableStateOf(listOf<String>()) }
    var playerCount by remember { mutableStateOf(0) }
    var status by remember { mutableStateOf("waiting") }
    
    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val containerColor = if (isDarkTheme) DarkInputGray else Color.White

    LaunchedEffect(roomCode) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return
                status = snapshot.child("status").getValue(String::class.java) ?: "waiting"
                if (status == "started") onGameStarted()

                val msgList = mutableListOf<String>()
                snapshot.child("messages").children.forEach {
                    it.getValue(String::class.java)?.let { msg -> msgList.add(msg) }
                }
                messages = msgList.reversed()
                playerCount = snapshot.child("players").childrenCount.toInt()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    AnimatedBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SOBA: $roomCode", 
                fontSize = 32.sp, 
                fontWeight = FontWeight.ExtraBold,
                color = BlueGradient
            )
            
            Text(
                text = if (isAdmin) "Ti si ADMIN" else "Čekanje admina...", 
                color = if (isAdmin) Gold else textColor.copy(alpha = 0.5f),
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = containerColor.copy(alpha = 0.9f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Igrači", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor)
                        Text("$playerCount / 8", color = BlueGradient, fontWeight = FontWeight.Bold)
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = textColor.copy(alpha = 0.1f))
                    
                    Text("Događaji:", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = textColor.copy(alpha = 0.5f))
                    
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(messages) { msg ->
                            Surface(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = textColor.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = msg,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    fontSize = 14.sp,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isAdmin) {
                Button(
                    onClick = {
                        val mainWord = croatianWords.random()
                        val imposterWord = croatianWords.filter { it != mainWord }.random()
                        database.get().addOnSuccessListener { snapshot ->
                            val players = snapshot.child("players").children.map { it.key!! }
                            if (players.isNotEmpty()) {
                                val imposter = players.random()
                                val updates = mapOf(
                                    "mainWord" to mainWord,
                                    "imposterWord" to imposterWord,
                                    "imposterId" to imposter,
                                    "status" to "started"
                                )
                                database.updateChildren(updates)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    enabled = playerCount >= 1
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.horizontalGradient(listOf(BlueGradient, PurpleGradient)), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("POKRENI IGRU", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Gold.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp, color = Gold)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Čekamo admina...", color = textColor)
                    }
                }
            }
        }
    }
}
