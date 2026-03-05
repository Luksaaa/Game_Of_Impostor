package com.example.impostergame

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.random.Random

@Composable
fun LobbyScreen(roomCode: String, username: String, isAdmin: Boolean, onGameStarted: () -> Unit) {
    val database = Firebase.database.getReference("rooms").child(roomCode)
    var messages by remember { mutableStateOf(listOf<String>()) }
    var playerCount by remember { mutableStateOf(0) }
    var status by remember { mutableStateOf("waiting") }

    LaunchedEffect(roomCode) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return
                
                status = snapshot.child("status").getValue(String::class.java) ?: "waiting"
                if (status == "started") {
                    onGameStarted()
                }

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

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("SOBA: $roomCode", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(if (isAdmin) "Ti si ADMIN" else "Čekanje admina...", 
            color = if (isAdmin) MaterialTheme.colorScheme.primary else Color.Gray)
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Igrači: $playerCount / 8", fontSize = 18.sp)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Obavijesti:", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            items(messages) { msg ->
                Text(msg, modifier = Modifier.padding(vertical = 4.dp))
            }
        }

        if (isAdmin) {
            Button(
                onClick = {
                    val mainWord = croatianWords.random()
                    val imposterWord = croatianWords.filter { it != mainWord }.random()
                    
                    database.get().addOnSuccessListener { snapshot ->
                        val players = snapshot.child("players").children.map { it.key!! }
                        val imposter = players.random()
                        
                        val updates = mapOf(
                            "mainWord" to mainWord,
                            "imposterWord" to imposterWord,
                            "imposterId" to imposter,
                            "status" to "started"
                        )
                        database.updateChildren(updates)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                enabled = playerCount >= 1 // Možeš staviti 3 za pravu igru
            ) {
                Text("KRENI", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp))
            Text("Igra će početi čim admin stisne gumb...")
        }
    }
}
