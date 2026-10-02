package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubjectType
import com.example.ui.theme.*

@Composable
fun SubjectHubScreen(
    onSelectAndAskPrompt: (String, SubjectType) -> Unit,
    modifier: Modifier = Modifier
) {
    val academicSubjects = listOf(
        SubjectType.MATHS,
        SubjectType.CHEMISTRY,
        SubjectType.HISTORY,
        SubjectType.CIVICS,
        SubjectType.BIOLOGY,
        SubjectType.GRAMMAR,
        SubjectType.GEOGRAPHY
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RomaDarkBg)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // --- Header ---
        Column(modifier = Modifier.padding(top = 12.dp, bottom = 16.dp)) {
            Text(
                text = "ROMA Subject Hub",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "7 Specialized Academic Disciplines • Tap any question to ask ROMA",
                fontSize = 13.sp,
                color = TextSecondaryDark
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(academicSubjects, key = { it.name }) { subject ->
                SubjectCard(
                    subject = subject,
                    onPromptClicked = { prompt -> onSelectAndAskPrompt(prompt, subject) }
                )
            }
        }
    }
}

@Composable
private fun SubjectCard(
    subject: SubjectType,
    onPromptClicked: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = RomaDarkCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(
                    subject.color.copy(alpha = 0.5f),
                    Color(0xFF1E293B)
                )
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("subject_card_${subject.name.lowercase()}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Subject Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(subject.color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = subject.iconEmoji, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = subject.title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = subject.tagline,
                            fontSize = 11.sp,
                            color = TextSecondaryDark
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(subject.color.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "VOICE READY",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = subject.color
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFF263352)
            )

            // Sample Questions
            Text(
                text = "HIGH-YIELD QUESTIONS:",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextTertiaryDark,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            subject.samplePrompts.forEach { prompt ->
                Surface(
                    onClick = { onPromptClicked(prompt) },
                    shape = RoundedCornerShape(10.dp),
                    color = RomaDarkSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Ask by voice",
                            tint = subject.color,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = prompt,
                            fontSize = 13.sp,
                            color = TextPrimaryDark,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = null,
                            tint = TextTertiaryDark,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}
