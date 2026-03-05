package com.example.impostergame

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@Composable
fun GameScreen(roomCode: String, username: String, onNewGame: () -> Unit) {
    var userWord by remember { mutableStateOf("Učitavanje...") }
    var isImposter by remember { mutableStateOf(false) }
    val database = Firebase.database.getReference("rooms").child(roomCode)

    LaunchedEffect(roomCode) {
        database.get().addOnSuccessListener { snapshot ->
            val imposterId = snapshot.child("imposterId").getValue(String::class.java)
            val mainWord = snapshot.child("mainWord").getValue(String::class.java) ?: ""
            val imposterWord = snapshot.child("imposterWord").getValue(String::class.java) ?: ""
            
            isImposter = (imposterId == username)
            userWord = if (isImposter) imposterWord else mainWord
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Tvoja uloga i riječ:", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(24.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = if (isImposter) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = userWord,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isImposter) Color(0xFFC62828) else Color(0xFF2E7D32)
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (isImposter) {
                    Text("TI SI IMPOSTER!", color = Color(0xFFC62828), fontWeight = FontWeight.ExtraBold)
                } else {
                    Text("Ti si običan igrač", color = Color(0xFF2E7D32))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        Button(onClick = onNewGame) {
            Text("Nova igra / Izlaz")
        }
    }
}
