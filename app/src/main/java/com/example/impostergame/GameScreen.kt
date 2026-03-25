package com.example.impostergame

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.ui.window.Dialog
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

    BackHandler(enabled = true) { }

    val database = remember(roomCode) { 
        Firebase.database("https://gameofimpostor-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("rooms").child(roomCode) 
    }
    
    var word by remember { mutableStateOf("") }
    var isRevealed by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableFloatStateOf(0f) }
    var currentAdmin by remember { mutableStateOf("") }
    var chatMessages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var chatInput by remember { mutableStateOf("") }
    var players by remember { mutableStateOf<Map<String, PlayerInfo>>(emptyMap()) }
    
    var gameStatus by remember { mutableStateOf("started") }
    var resultMessage by remember { mutableStateOf("") }
    var imposterId by remember { mutableStateOf("") }
    var showVoteDialog by remember { mutableStateOf(false) }
    
    var isDiscussionActive by remember { mutableStateOf(false) }
    var discussionEndTime by remember { mutableLongStateOf(0L) }
    var timeLeft by remember { mutableIntStateOf(0) }
    
    val isUserAdmin = remember(currentAdmin, username) { currentAdmin == username }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var holdJob by remember { mutableStateOf<Job?>(null) }

    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) OffWhite else DeepCharcoal
    val containerColor = if (isDarkTheme) DarkInputGray else Color.White
    val accentColor = SageGreen
    
    val repeatButtonBg = if (isDarkTheme) Color(0xFF3E3A33) else Color(0xFFFDF5E6)
    val repeatButtonProgress = if (isDarkTheme) SageGreen.copy(alpha = 0.3f) else SageGreen.copy(alpha = 0.2f)

    DisposableEffect(roomCode) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return
                snapshot.child("admin").getValue(String::class.java)?.let { currentAdmin = it }
                gameStatus = snapshot.child("status").getValue(String::class.java) ?: "started"
                resultMessage = snapshot.child("resultMessage").getValue(String::class.java) ?: ""
                imposterId = snapshot.child("imposterId").getValue(String::class.java) ?: ""
                if (gameStatus == "waiting") onRepeat()
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
                val playersMap = mutableMapOf<String, PlayerInfo>()
                snapshot.child("players").children.forEach {
                    val pInfo = it.getValue(PlayerInfo::class.java)
                    if (pInfo != null) playersMap[it.key!!] = pInfo
                }
                players = playersMap
                isDiscussionActive = snapshot.child("isDiscussionActive").getValue(Boolean::class.java) ?: false
                discussionEndTime = snapshot.child("discussionEndTime").getValue(Long::class.java) ?: 0L
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        database.addValueEventListener(listener)
        onDispose { database.removeEventListener(listener) }
    }

    LaunchedEffect(isDiscussionActive, discussionEndTime) {
        if (isDiscussionActive && discussionEndTime > 0L) {
            while (true) {
                val now = System.currentTimeMillis()
                val diff = ((discussionEndTime - now) / 1000).toInt()
                if (diff <= 0) {
                    timeLeft = 0
                    if (isUserAdmin) database.child("isDiscussionActive").setValue(false)
                    break
                }
                timeLeft = diff
                delay(1000)
            }
        } else { timeLeft = 0 }
    }

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) listState.animateScrollToItem(chatMessages.size - 1)
    }

    // DIJALOG ZA REZULTATE
    if (gameStatus == "finished") {
        Dialog(onDismissRequest = {}) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = containerColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("KRAJ RUNDE", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = if (resultMessage.contains("🏆")) accentColor else MutedRose)
                    Spacer(Modifier.height(16.dp))
                    Text(resultMessage, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = textColor, lineHeight = 28.sp)
                    Spacer(Modifier.height(24.dp))
                    if (isUserAdmin) {
                        Button(
                            onClick = { 
                                database.updateChildren(mapOf("status" to "waiting", "chatMessages" to null, "isDiscussionActive" to false, "discussionEndTime" to 0L, "resultMessage" to ""))
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) { Text("POVRATAK U SOBU", fontWeight = FontWeight.ExtraBold) }
                    } else {
                        Text("Čekamo admina...", fontSize = 13.sp, color = textColor.copy(alpha = 0.5f), fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }

    // DIJALOG ZA GLASANJE (Samo Admin)
    if (showVoteDialog) {
        Dialog(onDismissRequest = { showVoteDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) Color(0xFF2D2D2D) else Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("TKO JE IMPOSTER?", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MutedRose)
                    Spacer(Modifier.height(16.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(players.keys.toList()) { pId ->
                            val playerName = players[pId]?.name ?: pId
                            Surface(
                                onClick = {
                                    val isCorrect = pId == imposterId
                                    val imposterName = players[imposterId]?.name ?: "Imposter"
                                    val msg = if (isCorrect) "Pronašli ste Impostera! $playerName je bio on. Većina pobjeđuje! 🏆"
                                    else "Izbacili ste nevinu osobu! Imposter $imposterName pobjeđuje! 🎭"
                                    database.updateChildren(mapOf("status" to "finished", "resultMessage" to msg, "isDiscussionActive" to false))
                                    showVoteDialog = false
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.04f),
                                border = if (isDarkTheme) null else BorderStroke(1.dp, Color.Black.copy(0.05f))
                            ) {
                                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(32.dp).background(accentColor.copy(0.2f), CircleShape), contentAlignment = Alignment.Center) {
                                        Text(playerName.take(1).uppercase(), fontWeight = FontWeight.Bold, color = accentColor, fontSize = 14.sp)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Text(playerName, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = textColor)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = { showVoteDialog = false }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("ODUSTANI", color = textColor.copy(0.4f), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).statusBarsPadding().navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Kartica s riječi
        Card(
            modifier = Modifier.fillMaxWidth().height(160.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor.copy(alpha = 0.9f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (isDiscussionActive) {
                    Row(
                        modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).background(MutedRose.copy(alpha = 0.15f), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Timer, null, tint = MutedRose, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(String.format(Locale.US, "%02d:%02d", timeLeft / 60, timeLeft % 60), color = MutedRose, fontWeight = FontWeight.Bold)
                    }
                }
                Box(modifier = Modifier.fillMaxSize().clickable { isRevealed = !isRevealed }, contentAlignment = Alignment.Center) {
                    if (isRevealed) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Tvoja riječ:", color = textColor.copy(alpha = 0.6f), fontSize = 14.sp)
                            Text(word, color = textColor, fontSize = 42.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Visibility, null, tint = accentColor, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("DODIRNI ZA OTKRIVANJE", color = textColor.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chat sekcija
        Card(modifier = Modifier.fillMaxWidth().weight(1f), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = containerColor.copy(alpha = 0.5f))) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(if (isDiscussionActive) "RASPRAVA" else "CHAT", fontWeight = FontWeight.Bold, color = if (isDiscussionActive) MutedRose else accentColor)
                    if (isUserAdmin && !isDiscussionActive) {
                        var showTimerMenu by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { showTimerMenu = true }) { Icon(Icons.Default.Timer, null, tint = accentColor) }
                            DropdownMenu(expanded = showTimerMenu, onDismissRequest = { showTimerMenu = false }) {
                                listOf(30, 45, 60).forEach { sec ->
                                    DropdownMenuItem(text = { Text("$sec sekundi") }, onClick = {
                                        database.updateChildren(mapOf("isDiscussionActive" to true, "discussionEndTime" to System.currentTimeMillis() + (sec * 1000)))
                                        showTimerMenu = false
                                    })
                                }
                            }
                        }
                    }
                }
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), state = listState, contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(chatMessages) { msg ->
                        val isMe = msg.sender == username
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
                            if (!isMe) Text(msg.sender, fontSize = 11.sp, color = textColor.copy(alpha = 0.5f))
                            Surface(color = if (isMe) accentColor.copy(alpha = if(isDarkTheme) 0.2f else 0.8f) else (if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f)), shape = RoundedCornerShape(16.dp), contentColor = if (isMe && !isDarkTheme) Color.White else textColor) {
                                Text(msg.message, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), fontSize = 15.sp)
                            }
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    TextField(value = chatInput, onValueChange = { chatInput = it }, modifier = Modifier.weight(1f), placeholder = { Text("Napiši nešto...") }, colors = TextFieldDefaults.colors(focusedContainerColor = textColor.copy(alpha = 0.05f), unfocusedContainerColor = textColor.copy(alpha = 0.05f), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent), shape = RoundedCornerShape(24.dp))
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { if (chatInput.trim().isNotBlank()) { database.child("chatMessages").push().setValue(ChatMessage(username, chatInput.trim())); chatInput = "" } }, modifier = Modifier.background(accentColor, CircleShape).size(48.dp)) { Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ADMIN CONTROL PANEL
        if (isUserAdmin && !isDiscussionActive) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    modifier = Modifier.weight(1f).height(50.dp).clip(RoundedCornerShape(16.dp))
                        .pointerInput(Unit) {
                            detectTapGestures(onPress = {
                                holdJob = scope.launch {
                                    val start = System.currentTimeMillis()
                                    while (holdProgress < 2f) { holdProgress = ((System.currentTimeMillis() - start) / 1000f).coerceAtMost(2f); delay(10) }
                                    database.updateChildren(mapOf("status" to "waiting", "chatMessages" to null, "isDiscussionActive" to false, "discussionEndTime" to 0L, "resultMessage" to ""))
                                    holdProgress = 0f
                                }
                                try { awaitRelease() } finally { holdJob?.cancel(); holdProgress = 0f }
                            })
                        },
                    color = repeatButtonBg
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (holdProgress > 0f) Box(modifier = Modifier.fillMaxWidth(holdProgress / 2f).fillMaxHeight().background(accentColor.copy(0.2f)).align(Alignment.CenterStart))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Refresh, null, tint = textColor.copy(0.6f), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (holdProgress > 0f) String.format(Locale.US, "%.1fs", 2f - holdProgress) else "PONOVI", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = textColor)
                        }
                    }
                }

                Button(
                    onClick = { showVoteDialog = true },
                    modifier = Modifier.weight(1.2f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MutedRose.copy(alpha = 0.9f))
                ) {
                    Icon(Icons.Default.Gavel, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("IZBACI IMPOSTERA", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // IZAĐI GUMB
        Button(
            onClick = { FirebaseManager.leaveRoomWithAdminTransfer(roomCode, username, onNewGame) }, 
            modifier = Modifier.fillMaxWidth().height(50.dp), 
            shape = RoundedCornerShape(16.dp), 
            colors = ButtonDefaults.buttonColors(containerColor = textColor.copy(alpha = 0.05f))
        ) { Text("IZAĐI IZ SOBE", color = textColor.copy(alpha = 0.5f), fontWeight = FontWeight.Bold, fontSize = 14.sp) }
    }
}
