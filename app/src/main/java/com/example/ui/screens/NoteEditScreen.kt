package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Note
import com.example.ui.NoteViewModel
import com.example.ui.SkeuoColor
import com.example.ui.SkeuomorphicButton
import com.example.ui.SkeuomorphicDeskBackground
import com.example.ui.SkeuomorphicPaper
import com.example.ui.SkeuomorphicPushpin
import com.example.ui.SkeuomorphicSpiralBinding
import com.example.ui.SkeuomorphicPaperclip

@Composable
fun NoteEditScreen(
    viewModel: NoteViewModel,
    noteId: Int,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val notes by viewModel.notes.collectAsState()
    
    // Find note if editing
    val existingNote = remember(noteId, notes) {
        notes.find { it.id == noteId }
    }

    // Editable states
    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var content by remember { mutableStateOf(existingNote?.content ?: "") }
    var colorHex by remember { mutableStateOf(existingNote?.colorHex ?: 0xFFFFF9C4) }
    var isPinned by remember { mutableStateOf(existingNote?.isPinned ?: false) }
    var fontType by remember { mutableStateOf(existingNote?.fontType ?: "Serif") }
    var category by remember { mutableStateOf(existingNote?.category ?: "Personal") }

    // If existingNote loads late/asynchronously, update local values
    LaunchedEffect(existingNote) {
        existingNote?.let {
            title = it.title
            content = it.content
            colorHex = it.colorHex
            isPinned = it.isPinned
            fontType = it.fontType
            category = it.category
        }
    }

    // Color options: Yellow, Blue, Green, Pink, White
    val colorOptions = listOf(
        0xFFFFF9C4 to "Yellow",
        0xFFE3F2FD to "Blue",
        0xFFE8F5E9 to "Green",
        0xFFFCE4EC to "Pink",
        0xFFFFFDF9 to "White"
    )

    SkeuomorphicDeskBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(vertical = 12.dp)
        ) {
            // --- TOP NAVIGATION ROW (Back and Pin) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button (Wooden button style)
                SkeuomorphicButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("back_button"),
                    height = 42.dp,
                    baseColor = Color(0xFF8D6E63),
                    darkColor = Color(0xFF4E342E),
                    lightColor = Color(0xFFBCAAA4),
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to desk",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DESK",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                }

                // Pushpin Pin Toggle (Click to pin note physically)
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isPinned) Color(0xFFD32F2F).copy(alpha = 0.2f) else Color.Transparent)
                        .clickable { isPinned = !isPinned }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        SkeuomorphicPushpin(
                            modifier = Modifier.size(28.dp),
                            color = if (isPinned) SkeuoColor.PinRed else SkeuoColor.PinBlue,
                            colorDark = if (isPinned) SkeuoColor.PinRedDark else SkeuoColor.PinBlueDark
                        )
                        Text(
                            text = if (isPinned) "PINNED" else "TAP TO PIN",
                            color = if (isPinned) Color(0xFFFF8A80) else Color(0xFFB0BEC5),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- WATERCOLOR WELL COLOR PICKER ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1E1712)) // Sunken wooden holder tray
                    .border(2.dp, Color(0xFF4E342E), RoundedCornerShape(10.dp))
                    .padding(vertical = 8.dp, horizontal = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PAPER:",
                        color = Color(0xFFD7CCC8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    colorOptions.forEach { (colorVal, _) ->
                        val isSelected = colorHex == colorVal
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(colorVal))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color(0xFFFFB300) else Color(0xFF5D4037),
                                    shape = CircleShape
                                )
                                .clickable { colorHex = colorVal }
                                .drawBehind {
                                    // Make water color wells look curved inside
                                    val s = this.size
                                    drawCircle(
                                        color = Color.Black.copy(alpha = 0.15f),
                                        radius = s.width / 2.5f,
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- BEVELED WOODEN CATEGORY PICKER TRAY ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1E1712)) // Sunken wooden holder tray
                    .border(2.dp, Color(0xFF4E342E), RoundedCornerShape(10.dp))
                    .padding(vertical = 8.dp, horizontal = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TAG:",
                        color = Color(0xFFD7CCC8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(end = 4.dp)
                    )

                    val categoriesList = listOf("Personal", "Work", "Ideas", "Tasks", "Other")

                    categoriesList.forEach { catVal ->
                        val isSelected = category == catVal
                        val clipColor = when (catVal) {
                            "Personal" -> Color(0xFFEF5350)
                            "Work" -> Color(0xFF29B6F6)
                            "Ideas" -> Color(0xFFFFCA28)
                            "Tasks" -> Color(0xFF66BB6A)
                            else -> Color(0xFFAB47BC)
                        }
                        val darkColor = when (catVal) {
                            "Personal" -> Color(0xFFC62828)
                            "Work" -> Color(0xFF0277BD)
                            "Ideas" -> Color(0xFFF57F17)
                            "Tasks" -> Color(0xFF2E7D32)
                            else -> Color(0xFF6A1B9A)
                        }

                        SkeuomorphicButton(
                            onClick = { category = catVal },
                            height = 36.dp,
                            baseColor = if (isSelected) clipColor else Color(0xFF8D6E63),
                            darkColor = if (isSelected) darkColor else Color(0xFF4E342E),
                            lightColor = if (isSelected) clipColor.copy(alpha = 0.5f) else Color(0xFFBCAAA4),
                            contentColor = if (isSelected) Color.White else Color(0xFFD7CCC8),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = catVal.uppercase(),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- BEVELED WOODEN FONT PICKER TRAY ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1E1712)) // Sunken wooden holder tray
                    .border(2.dp, Color(0xFF4E342E), RoundedCornerShape(10.dp))
                    .padding(vertical = 8.dp, horizontal = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FONT:",
                        color = Color(0xFFD7CCC8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(end = 4.dp)
                    )

                    val fontsList = listOf(
                        "Serif" to "SERIF",
                        "Monospace" to "TYPE",
                        "Sans-Serif" to "CLEAN",
                        "Cursive" to "HAND"
                    )

                    fontsList.forEach { (typeVal, label) ->
                        val isSelected = fontType == typeVal
                        SkeuomorphicButton(
                            onClick = { fontType = typeVal },
                            height = 36.dp,
                            baseColor = if (isSelected) Color(0xFFFFB300) else Color(0xFF8D6E63),
                            darkColor = if (isSelected) Color(0xFFFF8F00) else Color(0xFF4E342E),
                            lightColor = if (isSelected) Color(0xFFFFE082) else Color(0xFFBCAAA4),
                            contentColor = if (isSelected) Color(0xFF3E2723) else Color.White,
                            modifier = Modifier.weight(1f)
                        ) {
                            val btnFont = when (typeVal) {
                                "Monospace" -> FontFamily.Monospace
                                "Sans-Serif" -> FontFamily.SansSerif
                                "Cursive" -> FontFamily.Cursive
                                else -> FontFamily.Serif
                            }
                            Text(
                                text = label,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = btnFont
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- SPIRAL NOTEBOOK CONTAINER ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // The paper notepad itself
                SkeuomorphicPaper(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("editable_paper"),
                    paperColor = Color(colorHex),
                    hasLines = true,
                    hasSpiral = true
                ) {
                    val currentFont = when (fontType) {
                        "Monospace" -> FontFamily.Monospace
                        "Sans-Serif" -> FontFamily.SansSerif
                        "Cursive" -> FontFamily.Cursive
                        else -> FontFamily.Serif
                    }

                    // Editable Title
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("note_title_input"),
                        placeholder = {
                            Text(
                                "Subject / Title...",
                                color = Color(0x8037474F),
                                fontFamily = currentFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        },
                        textStyle = TextStyle(
                            fontFamily = currentFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF212121)
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color(0xFF212121),
                            unfocusedTextColor = Color(0xFF212121)
                        ),
                        singleLine = true
                    )

                    Divider(
                        color = SkeuoColor.PaperLineBlue,
                        modifier = Modifier.padding(vertical = 4.dp),
                        thickness = 1.5.dp
                    )

                    // Editable Content
                    TextField(
                        value = content,
                        onValueChange = { content = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("note_content_input"),
                        placeholder = {
                            Text(
                                "Write notes here as if using a real pen...",
                                color = Color(0x6037474F),
                                fontFamily = currentFont,
                                fontSize = 14.sp
                            )
                        },
                        textStyle = TextStyle(
                            fontFamily = currentFont,
                            fontSize = 14.sp,
                            lineHeight = 28.sp, // Try to loosely align with horizontal line intervals!
                            color = Color(0xFF37474F)
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color(0xFF37474F),
                            unfocusedTextColor = Color(0xFF37474F)
                        )
                    )
                }

                // Metallic Coils overlapping holes at top
                SkeuomorphicSpiralBinding(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = 8.dp)
                )

                // Pushpin animation showing pin state physically pinned down
                androidx.compose.animation.AnimatedVisibility(
                    visible = isPinned,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300)),
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    SkeuomorphicPushpin(
                        modifier = Modifier
                            .offset(y = (-6).dp, x = 20.dp)
                            .size(36.dp)
                    )
                }

                // Always draw the lovely, color-coded paperclip representing its current category!
                SkeuomorphicPaperclip(
                    category = category,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = 20.dp) // Sit over the paper margin edge beautifully!
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- FOOTER BUTTONS ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delete Note Button (If editing existing note)
                if (existingNote != null) {
                    SkeuomorphicButton(
                        onClick = {
                            viewModel.deleteNote(existingNote)
                            onNavigateBack()
                        },
                        modifier = Modifier.testTag("delete_button"),
                        baseColor = Color(0xFFE53935), // Red
                        darkColor = Color(0xFF990000),
                        lightColor = Color(0xFFFF8A80),
                        contentColor = Color.White
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Note",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TRASH",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                    }
                } else {
                    // Empty placeholder button for nice balance symmetry on new note
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Save Note Button
                SkeuomorphicButton(
                    onClick = {
                        viewModel.saveNote(
                            id = noteId,
                            title = title,
                            content = content,
                            colorHex = colorHex,
                            isPinned = isPinned,
                            fontType = fontType,
                            category = category,
                            onComplete = onNavigateBack
                        )
                    },
                    modifier = Modifier.testTag("save_button"),
                    baseColor = Color(0xFF43A047), // Solid stamp green
                    darkColor = Color(0xFF1B5E20),
                    lightColor = Color(0xFFA5D6A7),
                    contentColor = Color.White,
                    height = 48.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = "Save Note",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "STAMP SAVE",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 1.sp,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
