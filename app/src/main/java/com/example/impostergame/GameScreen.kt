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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
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
    val creamyWhite = Color(0xFFFDF5E6) // Krem bijela (Old Lace)

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
            shape = RoundedCornerShape(32.dp),
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
                        Text(word, color = textColor, fontSize = 42.sp, fontWeight = FontWeight.ExtraBold)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Visibility, 
                            tint = PurpleGradient,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("DODIRNI ZA OTKRIVANJE", color = textColor.copy(alpha = 0.5f), fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                Text("Chat", fontWeight = FontWeight.Bold, color = BlueGradient, fontSize = 18.sp)
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
                            if (!isMe) {
                                Text(
                                    msg.sender, 
                                    fontSize = 11.sp, 
                                    color = textColor.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                                )
                            }
                            Surface(
                                color = if (isMe) BlueGradient else (if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f)),
                                shape = RoundedCornerShape(
                                    topStart = 16.dp, 
                                    topEnd = 16.dp, 
                                    bottomStart = if (isMe) 16.dp else 4.dp, 
                                    bottomEnd = if (isMe) 4.dp else 16.dp
                                )
                            ) {
                                Text(
                                    msg.message,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    color = if (isMe) Color.White else textColor,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = chatInput,
                        onValueChange = { chatInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Napiši nešto...", fontSize = 14.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = textColor.copy(alpha = 0.05f),
                            unfocusedContainerColor = textColor.copy(alpha = 0.05f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        maxLines = 2,
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            val trimmedMessage = chatInput.trim()
                            if (trimmedMessage.isNotBlank()) {
                                val newMessage = ChatMessage(sender = username, message = trimmedMessage)
                                database.child("chatMessages").push().setValue(newMessage)
                                chatInput = ""
                            }
                        },
                        modifier = Modifier.background(BlueGradient, CircleShape).size(48.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Pošalji", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Kontrole
        Column(modifier = Modifier.fillMaxWidth()) {
            if (isUserAdmin) {
                var holdJob by remember { mutableStateOf<Job?>(null) }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(creamyWhite) // Krem bijela pozadina
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
                                        database.child("chatMessages").removeValue()
                                        holdProgress = 0f
                                    }
                                    try { awaitRelease() } finally { holdJob?.cancel(); holdProgress = 0f }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Progress fill (prikazuje se samo dok se drži)
                    if (holdProgress > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(holdProgress / 2f)
                                .fillMaxHeight()
                                .background(Brush.horizontalGradient(listOf(BlueGradient, PurpleGradient)))
                                .align(Alignment.CenterStart)
                        )
                    }

                    Text(
                        text = if (holdProgress > 0f) String.format(Locale.US, "%.1fs", 2f - holdProgress) else "PONOVI IGRU",
                        color = if (holdProgress > 1f) Color.White else Color(0xFF2D2D2D), // Tamni tekst na krem pozadini, bijeli na progresu
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { FirebaseManager.leaveRoomWithAdminTransfer(roomCode, username, onNewGame) },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = textColor.copy(alpha = 0.1f)),
                contentPadding = PaddingValues()
            ) {
                Text("IZAĐI IZ SOBE", color = textColor, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
        }
    }
}
