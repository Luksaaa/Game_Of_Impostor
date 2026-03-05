package com.example.impostergame

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.impostergame.ui.components.AnimatedBackground
import com.example.impostergame.ui.theme.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@Composable
fun JoinRoomScreen(username: String, onJoined: (String) -> Unit, onBack: () -> Unit) {
    var inputCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val inputContainerColor = if (isDarkTheme) DarkInputGray else Color.White
    
    val context = LocalContext.current
    val database = Firebase.database("https://gameofimpostor-default-rtdb.europe-west1.firebasedatabase.app/").getReference("rooms")

    // Camera launcher to actually open the camera
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            Toast.makeText(context, "Kamera je bila otvorena!", Toast.LENGTH_SHORT).show()
        }
    }

    // Permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch()
        } else {
            Toast.makeText(context, "Dopuštenje za kameru je odbijeno", Toast.LENGTH_SHORT).show()
        }
    }

    AnimatedBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .statusBarsPadding()
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Pridruži se",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor
                )
                
                Spacer(modifier = Modifier.height(48.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = inputContainerColor.copy(alpha = 0.9f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Unesi kod sobe",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = inputCode,
                            onValueChange = { 
                                if (it.length <= 6) inputCode = it.uppercase() 
                                errorMessage = ""
                            },
                            placeholder = { Text("ABC 123", color = textColor.copy(alpha = 0.4f)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            isError = errorMessage.isNotEmpty(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PurpleGradient,
                                unfocusedBorderColor = textColor.copy(alpha = 0.2f),
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            )
                        )
                        
                        if (errorMessage.isNotEmpty()) {
                            Text(
                                text = errorMessage, 
                                color = MaterialTheme.colorScheme.error, 
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = {
                                if (inputCode.length == 6) {
                                    database.child(inputCode).get().addOnSuccessListener { snapshot ->
                                        if (snapshot.exists()) {
                                            val players = snapshot.child("players")
                                            if (players.children.count() < 8) {
                                                val playerRef = database.child(inputCode).child("players").child(username)
                                                playerRef.setValue(mapOf("name" to username, "isReady" to false)).addOnSuccessListener {
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
                                        errorMessage = "Pogreška: ${it.message}"
                                    }
                                } else {
                                    errorMessage = "Kod mora imati 6 znakova"
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(PurpleGradient, BlueGradient)),
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("PRIDRUŽI SE", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Gumb s ikonom kamere - sada zapravo otvara kameru
                        IconButton(
                            onClick = { 
                                when (PackageManager.PERMISSION_GRANTED) {
                                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                                        cameraLauncher.launch()
                                    }
                                    else -> {
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(64.dp)
                                .background(textColor.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Camera",
                                tint = BlueGradient,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
