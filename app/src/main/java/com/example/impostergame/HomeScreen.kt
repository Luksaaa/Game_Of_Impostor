package com.example.impostergame

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.impostergame.ui.theme.*

@Composable
fun HomeScreen(username: String, onCreateRoom: () -> Unit, onJoinRoom: () -> Unit) {
    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) Color.White else Color.Black
    
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(screenHeight * 0.05f))

        Text(
            text = "Bok, $username!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        
        Text(
            text = "Spreman za igru?",
            fontSize = 16.sp,
            color = textColor.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(screenHeight * 0.08f))

        MenuButton(
            text = "Napravi sobu",
            icon = Icons.Default.Add,
            onClick = onCreateRoom,
            gradient = listOf(BlueGradient, BlueGradient.copy(alpha = 0.7f))
        )

        Spacer(modifier = Modifier.height(20.dp))

        MenuButton(
            text = "Pridruži se",
            icon = Icons.Default.Person,
            onClick = onJoinRoom,
            gradient = listOf(PurpleGradient, PurpleGradient.copy(alpha = 0.7f))
        )
        
        Spacer(modifier = Modifier.height(screenHeight * 0.05f))
    }
}

@Composable
fun MenuButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    gradient: List<Color>
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(gradient), RoundedCornerShape(20.dp))
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
