package com.example.impostergame

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.impostergame.ui.components.QRCodeImage
import com.example.impostergame.ui.theme.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@Composable
fun LobbyScreen(
    roomCode: String, 
    username: String, 
    isAdmin: Boolean,
    onLeaveRoom: () -> Unit,
    onGameStarted: () -> Unit
) {
    if (roomCode.isBlank()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BlueGradient)
        }
        return
    }

    val database = remember(roomCode) { 
        Firebase.database("https://gameofimpostor-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("rooms").child(roomCode) 
    }
    
    var messages by remember { mutableStateOf(listOf<String>()) }
    var playerCount by remember { mutableStateOf(0) }
    var status by remember { mutableStateOf("waiting") }
    var currentAdmin by remember { mutableStateOf("") }
    
    val isUserAdmin = currentAdmin == username
    
    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val containerColor = if (isDarkTheme) DarkInputGray else Color.White

    val clipboardManager = LocalClipboardManager.current

    val deepLinkUrl = "impostergame://join?code=$roomCode"

    DisposableEffect(roomCode) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return
                status = snapshot.child("status").getValue(String::class.java) ?: "waiting"
                currentAdmin = snapshot.child("admin").getValue(String::class.java) ?: ""
                
                if (status == "started") onGameStarted()

                val msgList = mutableListOf<String>()
                snapshot.child("messages").children.forEach {
                    it.getValue(String::class.java)?.let { msg -> msgList.add(msg) }
                }
                messages = msgList.reversed()
                playerCount = snapshot.child("players").childrenCount.toInt()
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        
        database.addValueEventListener(listener)
        
        onDispose {
            database.removeEventListener(listener)
        }
    }

    Scaffold(
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            clipboardManager.setText(AnnotatedString(roomCode))
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SOBA: $roomCode", 
                        fontSize = 32.sp, 
                        fontWeight = FontWeight.ExtraBold,
                        color = BlueGradient
                    )
                    Text(
                        text = "(Klikni za kopiranje)",
                        fontSize = 12.sp,
                        color = textColor.copy(alpha = 0.3f)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Surface(
                    modifier = Modifier
                        .size(80.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            clipboardManager.setText(AnnotatedString(roomCode))
                        },
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp),
                    tonalElevation = 4.dp
                ) {
                    Box(modifier = Modifier.padding(4.dp)) {
                        QRCodeImage(content = deepLinkUrl, modifier = Modifier.fillMaxSize())
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (isUserAdmin) "Ti si ADMIN" else "Admin je: $currentAdmin", 
                color = if (isUserAdmin) Gold else textColor.copy(alpha = 0.5f),
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
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = textColor.copy(alpha = 0.1f))
                    
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

            OutlinedButton(
                onClick = {
                    FirebaseManager.leaveRoomWithAdminTransfer(roomCode, username, onLeaveRoom)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
            ) {
                Text("IZAĐI IZ SOBE", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isUserAdmin) {
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
                    enabled = playerCount >= 2
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = if (playerCount >= 2) Brush.horizontalGradient(listOf(BlueGradient, PurpleGradient))
                                        else Brush.horizontalGradient(listOf(Color.Gray, Color.Gray)),
                                shape = RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (playerCount < 2) "MIN 2 IGRAČA" else "POKRENI IGRU",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
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

fun leaveRoomWithAdminTransfer(roomRef: DatabaseReference, username: String, onComplete: () -> Unit) {
    roomRef.get().addOnSuccessListener { snapshot ->
        if (!snapshot.exists()) {
            onComplete()
            return@addOnSuccessListener
        }
        
        val currentAdmin = snapshot.child("admin").getValue(String::class.java)
        val players = snapshot.child("players").children.toList()
        
        if (currentAdmin == username) {
            val nextAdmin = players.firstOrNull { it.key != username }?.key
            
            if (nextAdmin != null) {
                val updates = mutableMapOf<String, Any?>()
                updates["admin"] = nextAdmin
                updates["players/$username"] = null
                updates["messages/${roomRef.push().key}"] = "$username je izašao, novi admin je $nextAdmin"
                
                roomRef.updateChildren(updates).addOnCompleteListener { onComplete() }
            } else {
                roomRef.removeValue().addOnCompleteListener { onComplete() }
            }
        } else {
            roomRef.child("players").child(username).removeValue()
            roomRef.child("messages").push().setValue("$username je izašao")
            onComplete()
        }
    }.addOnFailureListener {
        onComplete()
    }
}
