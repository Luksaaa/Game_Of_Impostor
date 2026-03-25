package com.example.impostergame

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    @Suppress("UNUSED_PARAMETER") isAdmin: Boolean,
    onRepeat: () -> Unit,
    onNewGame: () -> Unit
) {
    if (roomCode.isBlank()) return

    // Blokiramo povratak (gestu ili gumb) kako korisnik ne bi slučajno izašao
    BackHandler(enabled = true) {
        // Prazno - korisnik mora kliknuti na gumb "IZAĐI IZ SOBE"
    }

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
    var chatMessages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var chatInput by remember { mutableStateOf("") }
    
    val isUserAdmin = currentAdmin == username
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

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

                val chatList = mutableListOf<ChatMessage>()
                snapshot.child("chatMessages").children.forEach {
                    it.getValue(ChatMessage::class.java)?.let { msg -> chatList.add(msg) }
                }
                chatMessages = chatList
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        
        database.addValueEventListener(listener)
        
        onDispose {
            database.removeEventListener(listener)
        }
    }

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Kartica s riječi
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor.copy(alpha = 0.9f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().clickable { isRevealed = !isRevealed },
                contentAlignment = Alignment.Center
            ) {
                if (isRevealed) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Tvoja riječ:", color = textColor.copy(alpha = 0.6f), fontSize = 14.sp)
                        Text(word, color = textColor, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Visibility, contentDescription = null, tint = PurpleGradient)
                        Spacer(Modifier.width(8.dp))
                        Text("Dodirni za otkrivanje", color = textColor.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chat sekcija
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Chat", fontWeight = FontWeight.Bold, color = BlueGradient)
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    state = listState,
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(chatMessages) { msg ->
                        val isMe = msg.sender == username
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                        ) {
                            Text(msg.sender, fontSize = 10.sp, color = textColor.copy(alpha = 0.5f))
                            Surface(
                                color = if (isMe) BlueGradient else textColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    msg.message,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = if (isMe) Color.White else textColor,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = chatInput,
                        onValueChange = { chatInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Napiši nešto...", fontSize = 14.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        maxLines = 2
                    )
                    IconButton(
                        onClick = {
                            if (chatInput.isNotBlank()) {
                                val newMessage = ChatMessage(sender = username, message = chatInput)
                                database.child("chatMessages").push().setValue(newMessage)
                                chatInput = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Pošalji", tint = BlueGradient)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Kontrole
        Column(modifier = Modifier.fillMaxWidth()) {
            if (isUserAdmin) {
                var holdJob by remember { mutableStateOf<Job?>(null) }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PurpleGradient)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    showHoldMessage = true
                                    scope.launch { delay(2000); if (holdProgress == 0f) showHoldMessage = false }
                                },
                                onPress = {
                                    showHoldMessage = true
                                    holdJob = scope.launch {
                                        val startTime = System.currentTimeMillis()
                                        while (holdProgress < 2f) {
                                            holdProgress = ((System.currentTimeMillis() - startTime) / 1000f).coerceAtMost(2f)
                                            delay(10)
                                        }
                                        database.child("status").setValue("waiting")
                                        database.child("chatMessages").removeValue() // Očisti chat za novu rundu
                                        holdProgress = 0f
                                    }
                                    try { awaitRelease() } finally { holdJob?.cancel(); holdProgress = 0f }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (holdProgress > 0f) String.format(Locale.US, "%.2fs", holdProgress) else "PONOVI IGRU",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { FirebaseManager.leaveRoomWithAdminTransfer(roomCode, username, onNewGame) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = textColor.copy(alpha = 0.1f))
            ) {
                Text("IZAĐI IZ SOBE", color = textColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}
