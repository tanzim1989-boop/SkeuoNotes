package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Note
import com.example.ui.NoteViewModel
import com.example.ui.SkeuoColor
import com.example.ui.SkeuomorphicButton
import com.example.ui.SkeuomorphicDeskBackground
import com.example.ui.SkeuomorphicPaper
import com.example.ui.SkeuomorphicPushpin
import com.example.ui.SkeuomorphicLeatherTag
import com.example.ui.SkeuomorphicPaperclip
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotesListScreen(
    viewModel: NoteViewModel,
    onNavigateToEdit: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val notes by viewModel.notes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    SkeuomorphicDeskBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 16.dp, bottom = 16.dp)
        ) {
            // --- MAIN LOGO & HEADER ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                // A leather-bound style plate for the logo
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF3E2723))
                        .border(1.5.dp, Color(0xFFD7CCC8), RoundedCornerShape(12.dp))
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "S K E U O N O T E S",
                            color = Color(0xFFFFF9C4),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Serif,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Your Analog Desk Notepad",
                            color = Color(0xFFD7CCC8),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- SUNKEN LEATHER SEARCH BAR ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF150F0B)) // sunken shadow effect
                    .border(2.dp, Color(0xFF5D4037), RoundedCornerShape(8.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input"),
                    placeholder = {
                        Text(
                            text = "Search notes on your desk...",
                            color = Color(0xFF8D6E63),
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Serif
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search icon",
                            tint = Color(0xFF8D6E63)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = Color(0xFF8D6E63),
                                modifier = Modifier.clickable { viewModel.setSearchQuery("") }
                            )
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color(0xFFEFEBE9),
                        unfocusedTextColor = Color(0xFFEFEBE9)
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- DESK ORGANIZER INDEX TABS (CATEGORY FILTER) ---
            val categories = listOf("All", "Personal", "Work", "Ideas", "Tasks", "Other")
            val selectedCategory by viewModel.selectedCategory.collectAsState()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    val tabColor = when (cat) {
                        "Personal" -> Color(0xFFEF5350)
                        "Work" -> Color(0xFF29B6F6)
                        "Ideas" -> Color(0xFFFFCA28)
                        "Tasks" -> Color(0xFF66BB6A)
                        "Other" -> Color(0xFFAB47BC)
                        else -> Color(0xFF8D6E63) // All
                    }
                    val darkTabColor = when (cat) {
                        "Personal" -> Color(0xFFC62828)
                        "Work" -> Color(0xFF0277BD)
                        "Ideas" -> Color(0xFFF57F17)
                        "Tasks" -> Color(0xFF2E7D32)
                        "Other" -> Color(0xFF6A1B9A)
                        else -> Color(0xFF4E342E) // All
                    }

                    SkeuomorphicLeatherTag(
                        text = cat.uppercase(),
                        isSelected = isSelected,
                        onClick = { viewModel.setSelectedCategory(cat) },
                        activeColor = tabColor,
                        inactiveColor = darkTabColor.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- NOTES DESK LAYOUT ---
            if (notes.isEmpty()) {
                // Empty Desk State: Draw a gorgeous piece of scrap paper centered
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SkeuomorphicPaper(
                        modifier = Modifier
                            .width(280.dp)
                            .wrapContentHeight()
                            .rotate(-2f),
                        paperColor = SkeuoColor.StickyYellow,
                        hasLines = true
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Desk is Empty!",
                            color = Color(0xFF4E342E),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "There are no notes scattered on your desk right now. Press the red push-button below to sketch a new one!",
                            color = Color(0xFF5D4037),
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Serif,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    // A decorative red pushpin pinning the empty state paper
                    SkeuomorphicPushpin(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-14).dp, x = 10.dp)
                    )
                }
            } else {
                // Scattered Grid of Notes
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("notes_grid"),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        // Generate a pseudo-random rotation angle based on ID to scatter the notes organically!
                        val rotationAngle = remember(note.id) {
                            val angles = listOf(-3f, -1.5f, 0f, 1.5f, 3f)
                            angles[note.id % angles.size]
                        }

                        Box(
                            modifier = Modifier
                                .padding(6.dp)
                                .graphicsLayer {
                                    rotationZ = rotationAngle
                                }
                        ) {
                            val itemFont = when (note.fontType) {
                                "Monospace" -> FontFamily.Monospace
                                "Sans-Serif" -> FontFamily.SansSerif
                                "Cursive" -> FontFamily.Cursive
                                else -> FontFamily.Serif
                            }

                            SkeuomorphicPaper(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp),
                                paperColor = Color(note.colorHex),
                                hasLines = true,
                                onClick = { onNavigateToEdit(note.id) }
                            ) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = note.title.ifBlank { "Untitled" },
                                    color = Color(0xFF263238),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = itemFont,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = note.content,
                                    color = Color(0xFF37474F),
                                    fontSize = 12.sp,
                                    fontFamily = itemFont,
                                    maxLines = 4,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(note.timestamp)),
                                    color = Color(0xFF78909C),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.End
                                )
                            }

                            // If pinned, draw a lovely glossy pushpin holding this note!
                            if (note.isPinned) {
                                SkeuomorphicPushpin(
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .offset(y = (-8).dp, x = (-4).dp)
                                )
                            }

                            // Always draw the lovely, color-coded paperclip representing its category!
                            SkeuomorphicPaperclip(
                                category = note.category,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-8).dp, y = (-12).dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- BOTTOM ACTION TRAY ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.Center
            ) {
                // Large physical beveled stamp for "New Note"
                SkeuomorphicButton(
                    onClick = { onNavigateToEdit(0) },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .testTag("create_note_button"),
                    baseColor = Color(0xFFD32F2F), // Bright red mechanical button
                    darkColor = Color(0xFF7B1FA2),
                    lightColor = Color(0xFFFFCDD2),
                    contentColor = Color.White,
                    height = 54.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Note",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NEW NOTE STAMP",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 1.sp,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
