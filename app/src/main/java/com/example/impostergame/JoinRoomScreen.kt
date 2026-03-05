package com.example.impostergame

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@Composable
fun JoinRoomScreen(username: String, onJoined: (String) -> Unit, onBack: () -> Unit) {
    var inputCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val database = Firebase.database.getReference("rooms")

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Pridruži se sobi", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = inputCode,
            onValueChange = { 
                if (it.length <= 6) inputCode = it.uppercase() 
                errorMessage = ""
            },
            label = { Text("Kod (3 slova + 3 broja)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            isError = errorMessage.isNotEmpty()
        )
        
        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                if (inputCode.length == 6) {
                    database.child(inputCode).get().addOnSuccessListener { snapshot ->
                        if (snapshot.exists()) {
                            val players = snapshot.child("players")
                            if (players.childrenCount < 8) {
                                // Add player to room
                                val playerRef = database.child(inputCode).child("players").child(username)
                                playerRef.setValue(true).addOnSuccessListener {
                                    // Add "X je ušao" message
                                    val msgRef = database.child(inputCode).child("messages").push()
                                    msgRef.setValue("$username je ušao")
                                    onJoined(inputCode)
                                }
                            } else {
                                errorMessage = "Soba je puna"
                            }
                        } else {
                            errorMessage = "Soba ne postoji"
                        }
                    }.addOnFailureListener {
                        errorMessage = "Pogreška pri spajanju"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Pridruži se")
        }
        
        TextButton(onClick = onBack) {
            Text("Povratak")
        }
    }
}
