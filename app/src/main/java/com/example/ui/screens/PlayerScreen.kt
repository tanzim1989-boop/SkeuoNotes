package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.PlayerTheme
import com.example.ui.LocalPlayerTheme
import com.example.data.Song
import com.example.ui.AeroColor
import com.example.ui.AeroButton
import com.example.ui.AeroGlassWindow
import com.example.ui.AeroOrbButton
import com.example.ui.AeroSlider
import com.example.ui.AeroVisualizer
import com.example.ui.PlayerViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // States from VM
    val songs by viewModel.allSongsState.collectAsState()
    val favorites by viewModel.favoriteSongsState.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val shuffleEnabled by viewModel.shuffleEnabled.collectAsState()
    val repeatEnabled by viewModel.repeatEnabled.collectAsState()

    // Theme & Visualizer Settings States
    val currentTheme by viewModel.currentTheme.collectAsState()
    val visualizerStyle by viewModel.visualizerStyle.collectAsState()
    val visualizerBands by viewModel.visualizerBands.collectAsState()
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Screen Tabs for mobile
    var selectedTab by remember { mutableStateOf(0) } // 0: Player, 1: Equalizer, 2: Library
    var searchQuery by remember { mutableStateOf("") }

    // Permission launcher for media scan
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.scanDeviceMusic(context)
            Toast.makeText(context, "Scanning local songs...", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Storage permission denied. Showing preloaded Aero songs.", Toast.LENGTH_LONG).show()
        }
    }

    // Auto-scan or request permission on app entry (startup)
    LaunchedEffect(Unit) {
        viewModel.checkAndPreloadDemoSongs()
        
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            viewModel.scanDeviceMusic(context)
        } else {
            permissionLauncher.launch(permission)
        }
    }

    if (showSettingsDialog) {
        AeroSettingsDialog(
            viewModel = viewModel,
            onDismiss = { showSettingsDialog = false }
        )
    }

    CompositionLocalProvider(LocalPlayerTheme provides currentTheme) {
        BoxWithConstraints(modifier = modifier.fillMaxSize()) {
            val isExpanded = maxWidth > 650.dp

            AeroGlassWindow(
                title = "Urban Player - Aero Media Player",
                modifier = Modifier.fillMaxSize()
            ) {
            if (isExpanded) {
                // Canonical Wide Layout: Now Playing (Left) & Library/Equalizer (Right)
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Panel (Player & EQ)
                    Column(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Glassy Album Display (2010s premium Aero glass platter)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .drawBehind {
                                    // Soft glassy background
                                    drawRoundRect(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(Color(0x1F000000), Color(0x0A000000))
                                        ),
                                        cornerRadius = CornerRadius(8.dp.toPx())
                                    )
                                    // High contrast glassy reflection sweep (2010s signature glass highlight)
                                    val sheenPath = Path().apply {
                                        moveTo(0f, 0f)
                                        lineTo(size.width * 0.45f, 0f)
                                        lineTo(0f, size.height * 0.55f)
                                        close()
                                    }
                                    drawPath(
                                        path = sheenPath,
                                        brush = Brush.linearGradient(
                                            colors = listOf(Color(0x12FFFFFF), Color.Transparent)
                                        )
                                    )
                                }
                                .border(1.dp, Color(0x18FFFFFF), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            GlassyVinylRecord(
                                isPlaying = isPlaying,
                                title = currentSong?.title ?: "No Song Selected"
                            )
                        }

                        // Song Info Title
                        SongInfoSection(currentSong)

                        // Seek bar
                        val progress = if (duration > 0) currentPosition.toFloat() / duration else 0f
                        AeroSlider(
                            value = progress,
                            onValueChange = { viewModel.seekTo(it) },
                            elapsedText = formatMillis(currentPosition),
                            remainingText = formatMillis(duration - currentPosition)
                        )

                        // Visualizer
                        AeroVisualizer(
                            isPlaying = isPlaying,
                            style = visualizerStyle,
                            bandsCount = visualizerBands
                        )

                        // Action Controllers
                        PlaybackControlSection(
                            isPlaying = isPlaying,
                            shuffleEnabled = shuffleEnabled,
                            repeatEnabled = repeatEnabled,
                            isFavorite = currentSong?.isFavorite ?: false,
                            onTogglePlay = { viewModel.togglePlayPause() },
                            onPrev = { viewModel.playPreviousSong() },
                            onNext = { viewModel.playNextSong() },
                            onToggleShuffle = { viewModel.toggleShuffle() },
                            onToggleRepeat = { viewModel.toggleRepeat() },
                            onFavorite = { currentSong?.let { viewModel.toggleFavorite(it) } },
                            onForward10 = { viewModel.forward10Seconds() },
                            onBackward10 = { viewModel.backward10Seconds() }
                        )
                    }

                    // Right Panel (Equalizer + Songs Library List)
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Mini Tab headers for right side
                        var rightTab by remember { mutableStateOf(0) } // 0: Library, 1: Equalizer, 2: Lyrics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AeroButton(
                                onClick = { rightTab = 0 },
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(Icons.Filled.LibraryMusic, null, tint = if (rightTab == 0) currentTheme.primaryGlow else Color.White, modifier = Modifier.size(14.dp))
                                    Text("Library", color = if (rightTab == 0) currentTheme.primaryGlow else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            AeroButton(
                                onClick = { rightTab = 1 },
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(Icons.Filled.GraphicEq, null, tint = if (rightTab == 1) currentTheme.primaryGlow else Color.White, modifier = Modifier.size(14.dp))
                                    Text("EQ", color = if (rightTab == 1) currentTheme.primaryGlow else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            AeroButton(
                                onClick = { rightTab = 2 },
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(Icons.Filled.Description, null, tint = if (rightTab == 2) currentTheme.primaryGlow else Color.White, modifier = Modifier.size(14.dp))
                                    Text("Lyrics", color = if (rightTab == 2) currentTheme.primaryGlow else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // WMP-style Settings Button
                            AeroButton(
                                onClick = { showSettingsDialog = true },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Settings,
                                    contentDescription = "Settings",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        when (rightTab) {
                            0 -> {
                                // Library List View
                                LibrarySection(
                                    songs = songs,
                                    currentSong = currentSong,
                                    searchQuery = searchQuery,
                                    onSearchChange = { searchQuery = it },
                                    onSongClick = { viewModel.selectAndPlaySong(it) },
                                    onScanClick = {
                                        triggerMusicScan(context, permissionLauncher, viewModel)
                                    }
                                )
                            }
                            1 -> {
                                // Equalizer faders
                                EqualizerSection(viewModel)
                            }
                            2 -> {
                                // Song Lyrics section
                                LyricsSection(viewModel)
                            }
                        }
                    }
                }
            } else {
                // Mobile Portrait view: 3 beautiful glassy tabs
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Mobile tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            Triple("Player", Icons.Filled.MusicNote, 0),
                            Triple("Library", Icons.Filled.LibraryMusic, 1),
                            Triple("Equalizer", Icons.Filled.GraphicEq, 2),
                            Triple("Lyrics", Icons.Filled.Description, 3)
                        ).forEach { (name, icon, index) ->
                            AeroButton(
                                onClick = { selectedTab = index },
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(icon, null, tint = if (selectedTab == index) currentTheme.primaryGlow else Color.White, modifier = Modifier.size(13.dp))
                                    Text(name, color = if (selectedTab == index) currentTheme.primaryGlow else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // WMP-style Settings Button (glassy chrome square button)
                        AeroButton(
                            onClick = { showSettingsDialog = true },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Settings",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        when (selectedTab) {
                            0 -> {
                                // Now Playing View
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .drawBehind {
                                                // Soft glassy background
                                                drawRoundRect(
                                                    brush = Brush.verticalGradient(
                                                        colors = listOf(Color(0x1F000000), Color(0x0A000000))
                                                    ),
                                                    cornerRadius = CornerRadius(8.dp.toPx())
                                                )
                                                // High contrast glassy reflection sweep (2010s signature glass highlight)
                                                val sheenPath = Path().apply {
                                                    moveTo(0f, 0f)
                                                    lineTo(size.width * 0.45f, 0f)
                                                    lineTo(0f, size.height * 0.55f)
                                                    close()
                                                }
                                                drawPath(
                                                    path = sheenPath,
                                                    brush = Brush.linearGradient(
                                                        colors = listOf(Color(0x12FFFFFF), Color.Transparent)
                                                    )
                                                )
                                            }
                                            .border(1.dp, Color(0x15FFFFFF), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        GlassyVinylRecord(
                                            isPlaying = isPlaying,
                                            title = currentSong?.title ?: "No Song Selected"
                                        )
                                    }

                                    SongInfoSection(currentSong)

                                    val progress = if (duration > 0) currentPosition.toFloat() / duration else 0f
                                    AeroSlider(
                                        value = progress,
                                        onValueChange = { viewModel.seekTo(it) },
                                        elapsedText = formatMillis(currentPosition),
                                        remainingText = formatMillis(duration - currentPosition)
                                    )

                                    AeroVisualizer(
                                        isPlaying = isPlaying,
                                        style = visualizerStyle,
                                        bandsCount = visualizerBands
                                    )

                                    PlaybackControlSection(
                                        isPlaying = isPlaying,
                                        shuffleEnabled = shuffleEnabled,
                                        repeatEnabled = repeatEnabled,
                                        isFavorite = currentSong?.isFavorite ?: false,
                                        onTogglePlay = { viewModel.togglePlayPause() },
                                        onPrev = { viewModel.playPreviousSong() },
                                        onNext = { viewModel.playNextSong() },
                                        onToggleShuffle = { viewModel.toggleShuffle() },
                                        onToggleRepeat = { viewModel.toggleRepeat() },
                                        onFavorite = { currentSong?.let { viewModel.toggleFavorite(it) } },
                                        onForward10 = { viewModel.forward10Seconds() },
                                        onBackward10 = { viewModel.backward10Seconds() }
                                    )
                                }
                            }
                            1 -> {
                                LibrarySection(
                                    songs = songs,
                                    currentSong = currentSong,
                                    searchQuery = searchQuery,
                                    onSearchChange = { searchQuery = it },
                                    onSongClick = { viewModel.selectAndPlaySong(it) },
                                    onScanClick = {
                                        triggerMusicScan(context, permissionLauncher, viewModel)
                                    }
                                )
                            }
                            2 -> {
                                EqualizerSection(viewModel)
                            }
                            3 -> {
                                LyricsSection(viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
}

/**
 * 1. Procedural Spinning Glassy Vinyl Record
 * Spun smoothly on Canvas with inner tracks, high-contrast rainbow metallic hues, and glassy glares.
 */
@Composable
fun GlassyVinylRecord(
    isPlaying: Boolean,
    title: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "VinylRotate")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "VinylAngle"
    )

    val currentAngle = if (isPlaying) rotationAngle else 0f

    Canvas(
        modifier = modifier
            .size(180.dp)
            .rotate(currentAngle)
    ) {
        val r = size.width / 2

        // 1. Shadow background
        drawCircle(
            color = Color.Black.copy(alpha = 0.4f),
            radius = r - 2.dp.toPx(),
            center = Offset(r + 4.dp.toPx(), r + 6.dp.toPx())
        )

        // 2. Vinyl disc body (pure black/charcoal grooves)
        drawCircle(
            color = Color(0xFF111111),
            radius = r
        )

        // 3. Concentric Groove tracks
        val grooves = listOf(0.95f, 0.9f, 0.85f, 0.8f, 0.75f, 0.7f, 0.65f, 0.6f)
        grooves.forEach { factor ->
            drawCircle(
                color = Color(0x18FFFFFF),
                radius = r * factor,
                style = Stroke(width = 0.5f.dp.toPx())
            )
        }

        // 4. Glossy iridescent reflections (metallic flare lines)
        val metallicPath = Path().apply {
            moveTo(r, r)
            lineTo(r - r * 0.9f, r - r * 0.9f)
            lineTo(r - r * 0.4f, r - r * 0.95f)
            close()
        }
        drawPath(
            path = metallicPath,
            brush = Brush.radialGradient(
                colors = listOf(Color(0x2AFFFFFF), Color.Transparent),
                center = Offset(r, r),
                radius = r
            )
        )

        val metallicPath2 = Path().apply {
            moveTo(r, r)
            lineTo(r + r * 0.9f, r + r * 0.9f)
            lineTo(r + r * 0.4f, r + r * 0.95f)
            close()
        }
        drawPath(
            path = metallicPath2,
            brush = Brush.radialGradient(
                colors = listOf(Color(0x2AFFFFFF), Color.Transparent),
                center = Offset(r, r),
                radius = r
            )
        )

        // 5. Center Vinyl Record Label (Glossy Aero turquoise gradient)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(AeroColor.VistaCyan, AeroColor.VistaBlue, Color(0xFF0F2027)),
                center = Offset(r, r),
                radius = r * 0.35f
            ),
            radius = r * 0.35f
        )

        // Inner white label ring
        drawCircle(
            color = Color.White.copy(alpha = 0.3f),
            radius = r * 0.35f,
            style = Stroke(width = 1.dp.toPx())
        )

        // Center spindle hole
        drawCircle(
            color = Color(0xFF0A0F14),
            radius = r * 0.08f
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.8f),
            radius = r * 0.08f,
            style = Stroke(width = 1.dp.toPx())
        )

        // Glassy overlay glare sweep (static glint across the glass disk overlay)
        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(Color(0x15FFFFFF), Color.Transparent),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height)
            ),
            radius = r
        )
    }
}

/**
 * 2. Active Song Information Banner
 */
@Composable
fun SongInfoSection(song: Song?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = song?.title ?: "No Track Playing",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            fontFamily = FontFamily.SansSerif,
            style = androidx.compose.ui.text.TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = AeroColor.VistaCyan.copy(alpha = 0.8f),
                    offset = Offset(0f, 0f),
                    blurRadius = 16f
                )
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "${song?.artist ?: "Aero Streamer"}  •  ${song?.album ?: "Aero Album"}",
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 12.sp,
            fontFamily = FontFamily.SansSerif,
            style = androidx.compose.ui.text.TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = AeroColor.VistaBlue.copy(alpha = 0.6f),
                    offset = Offset(0f, 0f),
                    blurRadius = 8f
                )
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 3. Media Controls Bar
 */
@Composable
fun PlaybackControlSection(
    isPlaying: Boolean,
    shuffleEnabled: Boolean,
    repeatEnabled: Boolean,
    isFavorite: Boolean,
    onTogglePlay: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onFavorite: () -> Unit,
    onForward10: () -> Unit,
    onBackward10: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Shuffle Mode with active indicator neon dot
        IconButton(onClick = onToggleShuffle) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (shuffleEnabled) AeroColor.VistaCyan else Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
                if (shuffleEnabled) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(4.dp)
                            .background(AeroColor.VistaCyan, CircleShape)
                    )
                }
            }
        }

        // Skip Previous
        IconButton(onClick = onPrev) {
            Icon(
                imageVector = Icons.Filled.SkipPrevious,
                contentDescription = "Previous",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }

        // 10 Seconds Backward Seek
        IconButton(onClick = onBackward10) {
            Icon(
                imageVector = Icons.Filled.Replay10,
                contentDescription = "Rewind 10 Seconds",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }

        // Glowing Blue Play/Pause Orb
        AeroOrbButton(
            onClick = onTogglePlay,
            isPlaying = isPlaying,
            size = 62.dp
        )

        // 10 Seconds Forward Seek
        IconButton(onClick = onForward10) {
            Icon(
                imageVector = Icons.Filled.Forward10,
                contentDescription = "Forward 10 Seconds",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }

        // Skip Next
        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.Filled.SkipNext,
                contentDescription = "Next",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }

        // Repeat mode
        IconButton(onClick = onToggleRepeat) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.Repeat,
                    contentDescription = "Repeat",
                    tint = if (repeatEnabled) AeroColor.VistaCyan else Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
                if (repeatEnabled) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(4.dp)
                            .background(AeroColor.VistaCyan, CircleShape)
                    )
                }
            }
        }

        // Favorite Toggle
        IconButton(onClick = onFavorite) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) Color(0xFFFF1744) else Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * 4. Library List Pane with Custom Search and File Scanning
 */
@Composable
fun LibrarySection(
    songs: List<Song>,
    currentSong: Song?,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSongClick: (Song) -> Unit,
    onScanClick: () -> Unit
) {
    val filteredSongs = songs.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
        it.artist.contains(searchQuery, ignoreCase = true) ||
        it.album.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Scan Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Glassy Search Box
            TextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search songs...", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp) },
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp)) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .border(1.dp, Color(0x20FFFFFF), RoundedCornerShape(8.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0x1F000000),
                    unfocusedContainerColor = Color(0x0A000000),
                    disabledContainerColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp)
            )

            // Scan Button
            AeroButton(
                onClick = onScanClick,
                modifier = Modifier.wrapContentWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Filled.Refresh, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Text("Scan Music", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Library Song List with glass row entries
        if (filteredSongs.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No songs found in your library.\nUse preloaded streams or scan local files.",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredSongs) { song ->
                    val isSelected = song.id == currentSong?.id
                    
                    // Glassy Song Entry Card (Aero transparent panel)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .drawBehind {
                                val r = 8.dp.toPx()
                                // Base Row glass background (highly transparent)
                                drawRoundRect(
                                    brush = if (isSelected) {
                                        Brush.verticalGradient(
                                            colors = listOf(Color(0x5000E5FF), Color(0x250091EA))
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            colors = listOf(Color(0x15FFFFFF), Color(0x06FFFFFF))
                                        )
                                    },
                                    cornerRadius = CornerRadius(r)
                                )
                                // Inner highlight stroke
                                drawRoundRect(
                                    color = if (isSelected) Color(0x55FFFFFF) else Color(0x10FFFFFF),
                                    style = Stroke(width = 1.dp.toPx()),
                                    cornerRadius = CornerRadius(r)
                                )
                                // Subtle glossy diagonal sweep across selected item
                                if (isSelected) {
                                    val sheenPath = Path().apply {
                                        moveTo(0f, 0f)
                                        lineTo(size.width * 0.25f, 0f)
                                        lineTo(0f, size.height * 0.7f)
                                        close()
                                    }
                                    drawPath(
                                        path = sheenPath,
                                        brush = Brush.linearGradient(
                                            colors = listOf(Color(0x20FFFFFF), Color.Transparent)
                                        )
                                    )
                                }
                            }
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSongClick(song) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Sound waves icon for playing row, or format icon badge
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) AeroColor.VistaCyan.copy(alpha = 0.2f) else Color(0x1A000000)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Filled.VolumeUp, null, tint = AeroColor.VistaCyan, modifier = Modifier.size(18.dp))
                            } else {
                                // Glassy format pill
                                Text(
                                    text = song.format,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Title / Artist column
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                color = if (isSelected) AeroColor.VistaCyan else Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = song.artist,
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Play chevron or favorite icon
                        if (song.isFavorite) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = "Liked",
                                tint = Color(0xFFFF1744),
                                modifier = Modifier
                                    .size(14.dp)
                                    .padding(end = 4.dp)
                            )
                        }

                        Text(
                            text = formatMillis(song.duration),
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

/**
 * 4.5. Glassy Lyrics Viewer & AI Generator
 */
@Composable
fun LyricsSection(viewModel: PlayerViewModel) {
    val theme = LocalPlayerTheme.current
    val currentSong by viewModel.currentSong.collectAsState()
    val lyricsLoading by viewModel.lyricsLoadingState.collectAsState()
    val lyricsError by viewModel.lyricsErrorState.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var editedLyrics by remember { mutableStateOf("") }

    // Synchronize local edit state when current song changes
    LaunchedEffect(currentSong) {
        editedLyrics = currentSong?.lyrics ?: ""
        isEditing = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .drawBehind {
                // Frosted glass background
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0x15FFFFFF), Color(0x06FFFFFF))
                    ),
                    cornerRadius = CornerRadius(8.dp.toPx())
                )
                // Gloss sheen sweep
                val sheenPath = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width * 0.4f, 0f)
                    lineTo(0f, size.height * 0.4f)
                    close()
                }
                drawPath(
                    path = sheenPath,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0x12FFFFFF), Color.Transparent)
                    )
                )
            }
            .border(1.dp, Color(0x12FFFFFF), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SONG LYRICS",
                    color = theme.primaryGlow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
                currentSong?.let { song ->
                    Text(
                        text = "${song.title} - ${song.artist}",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Edit / View Toggle
            currentSong?.let { song ->
                AeroButton(
                    onClick = {
                        if (isEditing) {
                            viewModel.updateLyrics(song, editedLyrics)
                        } else {
                            editedLyrics = song.lyrics ?: ""
                        }
                        isEditing = !isEditing
                    },
                    modifier = Modifier.height(32.dp).widthIn(min = 80.dp)
                ) {
                    Text(
                        text = if (isEditing) "SAVE" else "EDIT",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0x18FFFFFF))
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (currentSong == null) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Select a song from the library\nto view lyrics.",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            val song = currentSong!!
            val hasLyrics = !song.lyrics.isNullOrBlank()

            if (isEditing) {
                // Editing Area
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = editedLyrics,
                        onValueChange = { editedLyrics = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        placeholder = {
                            Text(
                                "Type or paste lyrics here...",
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 13.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = theme.primaryGlow.copy(alpha = 0.5f),
                            unfocusedBorderColor = Color(0x20FFFFFF),
                            focusedContainerColor = Color(0x1A000000),
                            unfocusedContainerColor = Color(0x0A000000)
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 18.sp
                        )
                    )
                }
            } else if (lyricsLoading) {
                // AI Generation Loader with beautiful pulsing color
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = theme.primaryGlow,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Synthesizing AI Lyrics via Gemini...",
                            color = theme.primaryGlow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else if (!hasLyrics) {
                // Option to Generate via AI or add manually
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "AI",
                            tint = theme.primaryGlow,
                            modifier = Modifier.size(42.dp)
                        )

                        Text(
                            text = "No lyrics found for this song",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "Generate premium synchronized lyrics automatically using Gemini API or write them yourself.",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AeroButton(
                                onClick = { viewModel.generateAILyricsForCurrentSong() },
                                modifier = Modifier.height(38.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.AutoAwesome,
                                        null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        "AI GENERATE",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            AeroButton(
                                onClick = {
                                    editedLyrics = ""
                                    isEditing = true
                                },
                                modifier = Modifier.height(38.dp)
                            ) {
                                Text(
                                    "WRITE LYRICS",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (lyricsError != null) {
                            Text(
                                text = lyricsError!!,
                                color = Color(0xFFFF5252),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            } else {
                // Display scrollable lyrics
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = song.lyrics!!,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 22.sp,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Easy access AI Regenerate / manual update button at bottom
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        AeroButton(
                            onClick = { viewModel.generateAILyricsForCurrentSong() },
                            modifier = Modifier.height(32.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Filled.AutoAwesome,
                                    null,
                                    tint = theme.primaryGlow,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    "Regenerate with Gemini",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 5. Glassy Equalizer Control Panel
 */
@Composable
fun EqualizerSection(viewModel: PlayerViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .drawBehind {
                // Frosted glass background
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0x15FFFFFF), Color(0x06FFFFFF))
                    ),
                    cornerRadius = CornerRadius(8.dp.toPx())
                )
                // Gloss sheen sweep
                val sheenPath = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width * 0.4f, 0f)
                    lineTo(0f, size.height * 0.4f)
                    close()
                }
                drawPath(
                    path = sheenPath,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0x10FFFFFF), Color.Transparent)
                    )
                )
            }
            .border(1.dp, Color(0x15FFFFFF), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Studio Sound Equalizer",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            
            // Bass Boost Option
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Glow LED Indicator for Bass Boost state
                val isBassBoosted = viewModel.equalizerBands[0] == 1.0f && viewModel.equalizerBands[1] == 0.8f
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isBassBoosted) AeroColor.VistaGreen else Color.DarkGray)
                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                )
                Text(
                    text = "Bass Boost",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    modifier = Modifier.clickable {
                        if (isBassBoosted) {
                            viewModel.setEqualizerBand(0, 0.5f)
                            viewModel.setEqualizerBand(1, 0.5f)
                        } else {
                            viewModel.setEqualizerBand(0, 1.0f) // Max Bass
                            viewModel.setEqualizerBand(1, 0.8f) // High Mid-Bass
                        }
                    }
                )
            }
        }

        // 5 Faders side by side
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (i in 0 until 5) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    // Vertical slider track drawn on canvas
                    val level = viewModel.equalizerBands[i]
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .width(36.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {} // Click handler
                            )
                            .drawBehind {
                                val trackW = 4.dp.toPx()
                                val x = size.width / 2 - trackW / 2
                                val r = 2.dp.toPx()

                                // Track background
                                drawRoundRect(
                                    color = Color(0x66000000),
                                    topLeft = Offset(x, 0f),
                                    size = Size(trackW, size.height),
                                    cornerRadius = CornerRadius(r)
                                )

                                // Active progress (filled from bottom to slider thumb level)
                                val fillH = size.height * level
                                val fillY = size.height - fillH
                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(AeroColor.VistaCyan, AeroColor.VistaBlue)
                                    ),
                                    topLeft = Offset(x, fillY),
                                    size = Size(trackW, fillH),
                                    cornerRadius = CornerRadius(r)
                                )

                                // Shiny slider thumb knob (horizontal glassy rectangle)
                                val thumbH = 10.dp.toPx()
                                val thumbW = 20.dp.toPx()
                                val thumbX = size.width / 2 - thumbW / 2
                                val thumbY = fillY - thumbH / 2

                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color.White, Color(0xFFCFD8DC), Color(0xFF78909C))
                                    ),
                                    topLeft = Offset(thumbX, thumbY),
                                    size = Size(thumbW, thumbH),
                                    cornerRadius = CornerRadius(2.dp.toPx())
                                )
                                // Highlight glint on knob
                                drawLine(
                                    color = Color.White.copy(alpha = 0.8f),
                                    start = Offset(thumbX + 2.dp.toPx(), thumbY + 1.dp.toPx()),
                                    end = Offset(thumbX + thumbW - 2.dp.toPx(), thumbY + 1.dp.toPx()),
                                    strokeWidth = 1.dp.toPx()
                                )
                                // Knob border
                                drawRoundRect(
                                    color = Color(0x66000000),
                                    topLeft = Offset(thumbX, thumbY),
                                    size = Size(thumbW, thumbH),
                                    style = Stroke(width = 0.5f.dp.toPx()),
                                    cornerRadius = CornerRadius(2.dp.toPx())
                                )
                            }
                    ) {
                        // Slider interactive component to adjust band level
                        Slider(
                            value = level,
                            onValueChange = { viewModel.setEqualizerBand(i, it) },
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(0.dp) // Hide default compose slider visual track, we custom draw it!
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Band Decibel value label
                    val dbVal = ((level - 0.5f) * 24).toInt() // Range -12dB to +12dB
                    Text(
                        text = "${if (dbVal > 0) "+" else ""}$dbVal dB",
                        color = AeroColor.VistaCyan,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Brief Band frequency label
                    Text(
                        text = when(i) {
                            0 -> "BASS"
                            1 -> "MID-B"
                            2 -> "MID"
                            3 -> "PRES"
                            else -> "TREB"
                        },
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Reset EQ Button
        AeroButton(
            onClick = {
                for (i in 0 until 5) {
                    viewModel.setEqualizerBand(i, 0.5f) // Reset to center
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Filled.SettingsBackupRestore, null, tint = Color.White, modifier = Modifier.size(16.dp))
                Text("Restore Flat EQ", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Interactive WMP-style Settings Dialog
 */
@Composable
fun AeroSettingsDialog(
    viewModel: PlayerViewModel,
    onDismiss: () -> Unit
) {
    val theme = LocalPlayerTheme.current
    val currentThemeSelected by viewModel.currentTheme.collectAsState()
    val autoPlayNext by viewModel.autoPlayNext.collectAsState()
    val bassBoostEnabled by viewModel.bassBoostEnabled.collectAsState()
    val reverbEnabled by viewModel.reverbEnabled.collectAsState()
    val visualizerStyleSelected by viewModel.visualizerStyle.collectAsState()
    val visualizerBandsSelected by viewModel.visualizerBands.collectAsState()
    val backgroundTypeSelected by viewModel.backgroundType.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            AeroGlassWindow(
                title = "Aero Player Settings",
                modifier = Modifier.fillMaxWidth(),
                onCloseClick = onDismiss
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // SECTION 1: VISUAL THEMES
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "AERO COLOR & WALLPAPER THEME",
                            color = theme.primaryGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0x30FFFFFF))
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PlayerTheme.values().forEach { t ->
                                val isSelected = currentThemeSelected == t
                                AeroButton(
                                    onClick = { viewModel.setTheme(t) },
                                    modifier = Modifier.widthIn(min = 120.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(t.primaryGlow)
                                        )
                                        Text(
                                            text = when(t) {
                                                PlayerTheme.VISTA_AERO -> "Aero Sky"
                                                PlayerTheme.COBALT_BLUE -> "Cobalt Blue"
                                                PlayerTheme.NEON_CYBER -> "Neon Cyber"
                                                PlayerTheme.CARBON_SLATE -> "Carbon Slate"
                                                PlayerTheme.SUNSET_VISTA -> "Sunset Vista"
                                            },
                                            color = if (isSelected) theme.primaryGlow else Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // SECTION 1.5: BACKGROUND WALLPAPERS
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "DESKTOP WALLPAPER BACKGROUND",
                            color = theme.primaryGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0x30FFFFFF))
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Pair(0, "Aero Aurora"),
                                Pair(1, "Neon Cyber"),
                                Pair(2, "Cosmic Nebula")
                            ).forEach { (typeId, label) ->
                                val isSelected = backgroundTypeSelected == typeId
                                AeroButton(
                                    onClick = { viewModel.setBackgroundType(typeId) },
                                    modifier = Modifier.widthIn(min = 120.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) theme.primaryGlow else Color.Gray)
                                        )
                                        Text(
                                            text = label,
                                            color = if (isSelected) theme.primaryGlow else Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // SECTION 2: AUDIO ENGINE TWEAKS
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "AUDIO ENGINE TWEAKS",
                            color = theme.primaryGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0x30FFFFFF))
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Auto-play toggle
                            AeroButton(
                                onClick = { viewModel.setAutoPlayNext(!autoPlayNext) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (autoPlayNext) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (autoPlayNext) theme.activeGreen else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Auto-Play Next",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Bass Boost Toggle
                            AeroButton(
                                onClick = { viewModel.setBassBoostEnabled(!bassBoostEnabled) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (bassBoostEnabled) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (bassBoostEnabled) theme.activeGreen else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Bass Boost",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Reverb Toggle
                            AeroButton(
                                onClick = { viewModel.setReverbEnabled(!reverbEnabled) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (reverbEnabled) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (reverbEnabled) theme.activeGreen else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Reverb Hall",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Dummy spacer to balance the layout
                            Box(modifier = Modifier.weight(1f))
                        }
                    }

                    // SECTION 3: VISUALIZER PREFERENCES
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "VISUALIZER CONFIGURATION",
                            color = theme.primaryGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0x30FFFFFF))
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Style Select
                            listOf("bars", "lines").forEach { style ->
                                val isSelected = visualizerStyleSelected == style
                                AeroButton(
                                    onClick = { viewModel.setVisualizerStyle(style) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) theme.primaryGlow else Color.White)
                                        )
                                        Text(
                                            text = if (style == "bars") "Classic Bars" else "Aero Wave",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "FREQUENCY RESOLUTION",
                            color = theme.primaryGlow,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(8, 12, 16).forEach { bands ->
                                val isSelected = visualizerBandsSelected == bands
                                AeroButton(
                                    onClick = { viewModel.setVisualizerBands(bands) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "$bands Bands",
                                        color = if (isSelected) theme.primaryGlow else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Close Settings Button
                    AeroButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        Text(
                            text = "APPLY & CLOSE",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Triggers storage scan, handling different SDK versions
 */
private fun triggerMusicScan(
    context: android.content.Context,
    launcher: androidx.activity.result.ActivityResultLauncher<String>,
    viewModel: PlayerViewModel
) {
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
        // Run background scan task
        viewModel.scanDeviceMusic(context)
        Toast.makeText(context, "Scanning local songs...", Toast.LENGTH_SHORT).show()
    } else {
        launcher.launch(permission)
    }
}

/**
 * Milliseconds to MM:SS string formatter
 */
fun formatMillis(ms: Long): String {
    val seconds = (ms / 1000) % 60
    val minutes = (ms / (1000 * 60)) % 60
    return String.format("%02d:%02d", minutes, seconds)
}
