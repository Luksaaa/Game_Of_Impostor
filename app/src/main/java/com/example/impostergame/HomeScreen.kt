package com.example.impostergame

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.impostergame.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(username: String, onCreateRoom: () -> Unit, onJoinRoom: () -> Unit) {
    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) OffWhite else DeepCharcoal
    
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    var showRules by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Info Gumb u gornjem desnom kutu
        IconButton(
            onClick = { showRules = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Pravila igre",
                tint = textColor.copy(alpha = 0.5f),
                modifier = Modifier.size(28.dp)
            )
        }

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
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                letterSpacing = (-0.5).sp
            )
            
            Text(
                text = "Spreman za novu rundu?",
                fontSize = 16.sp,
                color = textColor.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(screenHeight * 0.08f))

            AestheticButton(
                text = "Napravi sobu",
                subText = "Postani admin i započni igru",
                icon = Icons.Default.Add,
                onClick = onCreateRoom,
                color = SageGreen
            )

            Spacer(modifier = Modifier.height(20.dp))

            AestheticButton(
                text = "Pridruži se",
                subText = "Uđi u postojeću sobu",
                icon = Icons.Default.Group,
                onClick = onJoinRoom,
                color = MutedRose
            )
            
            Spacer(modifier = Modifier.height(screenHeight * 0.05f))
        }
    }

    if (showRules) {
        ModalBottomSheet(
            onDismissRequest = { showRules = false },
            containerColor = if (isDarkTheme) DarkInputGray else OffWhite,
            dragHandle = { BottomSheetDefaults.DragHandle(color = textColor.copy(alpha = 0.2f)) }
        ) {
            RulesContent(textColor)
        }
    }
}

@Composable
fun RulesContent(textColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 48.dp)
    ) {
        Text(
            text = "Kako igrati?",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        RuleSection(
            title = "1. Dodjela uloga",
            description = "Svaki igrač dobiva tajnu riječ. Većina dobiva istu riječ, dok Imposter dobiva sličnu, ali različitu riječ.",
            textColor = textColor
        )

        RuleSection(
            title = "2. Opisivanje",
            description = "Igrači redom govore samo JEDNU riječ (asocijaciju). Npr. ako je riječ 'Brod', možete reći 'Voda' ili 'Kapetan'.",
            textColor = textColor
        )

        RuleSection(
            title = "3. Zabrana",
            description = "Zabranjeno je izgovoriti samu tajnu riječ ili njezine korijene.",
            textColor = textColor,
            isWarning = true
        )

        RuleSection(
            title = "4. Glasanje",
            description = "Nakon rasprave, svi istovremeno upiru prstom u sumnjivca. Osoba s najviše glasova ispada.",
            textColor = textColor
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = textColor.copy(alpha = 0.1f))

        Text(
            text = "🏆 Cilj igre",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            text = "Većina: Pronaći Impostera.\nImposter: Preživjeti do kraja blefiranjem.",
            fontSize = 15.sp,
            color = textColor.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = SageGreen.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("💡 Savjeti", fontWeight = FontWeight.Bold, color = SageGreen)
                Text(
                    "• Budi tajnovit: Ne reci preočitu riječ.\n• Pažljivo slušaj: Imposter mora kopirati druge.\n• Blefiraj: Čak i ako te otkriju, optuži drugoga!",
                    fontSize = 14.sp,
                    color = textColor.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun RuleSection(title: String, description: String, textColor: Color, isWarning: Boolean = false) {
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = if (isWarning) MutedRose else textColor
        )
        Text(
            text = description,
            fontSize = 15.sp,
            color = textColor.copy(alpha = 0.7f),
            lineHeight = 20.sp
        )
    }
}

@Composable
fun AestheticButton(
    text: String,
    subText: String,
    icon: ImageVector,
    onClick: () -> Unit,
    color: Color
) {
    val isDarkTheme = isSystemInDarkTheme()
    
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = if (isDarkTheme) 0.15f else 0.9f),
            contentColor = if (isDarkTheme) color else Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isDarkTheme) 0.dp else 4.dp),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (isDarkTheme) color.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(28.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column {
                Text(
                    text = text.uppercase(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = subText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = (if (isDarkTheme) color else Color.White).copy(alpha = 0.7f)
                )
            }
        }
    }
}
