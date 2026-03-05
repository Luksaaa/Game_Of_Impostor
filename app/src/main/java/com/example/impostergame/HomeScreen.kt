package com.example.impostergame

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(username: String, onCreateRoom: () -> Unit, onJoinRoom: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Bok, $username!", fontSize = 24.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onCreateRoom,
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) {
            Text("1. Napravi sobu", fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onJoinRoom,
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) {
            Text("2. Pridruži se sobi", fontSize = 18.sp)
        }
    }
}
