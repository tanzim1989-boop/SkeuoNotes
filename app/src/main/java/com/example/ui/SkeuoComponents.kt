package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import coil.compose.AsyncImage
import java.io.File
import com.example.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

import com.example.ui.theme.PlayerTheme

val LocalPlayerTheme = staticCompositionLocalOf { PlayerTheme.VISTA_AERO }

// --- WINDOWS VISTA AERO COLOR SYSTEM ---
object AeroColor {
    // Frosted Glass Colors
    val GlassBase = Color(0x2E101D2C) // Translucent deep teal-gray
    val GlassLight = Color(0x3BFFFFFF) // White glass highlight
    val GlassDark = Color(0x8C000000) // Translucent dark borders
    val GlassHighlight = Color(0x1A64B5F6) // Soft Aero blue tint
    
    // Glowing Vista Colors
    val VistaCyan = Color(0xFF00E5FF) // Aqua blue glow
    val VistaBlue = Color(0xFF0091EA) // Deep Aero blue
    val VistaGreen = Color(0xFF00E676) // Active playing green
    val VistaYellow = Color(0xFFFFD600) // Visualizer peaks
    val VistaOrange = Color(0xFFFF6D00) // Warning / Peak levels
    val VistaRed = Color(0xFFD50000)

    // Aurora Wallpaper Colors (Classic Vista background)
    val AuroraTeal = Color(0xFF004D40)
    val AuroraBlue = Color(0xFF0D47A1)
    val AuroraPurple = Color(0xFF311B92)
    val AuroraGreen = Color(0xFF1B5E20)
    val AuroraBlack = Color(0xFF0A0F14)
}

/**
 * 1. Windows Vista Aurora Wallpaper Background
 * Draws a gorgeous, authentic Windows Vista desktop wallpaper with flowing glassy rays.
 */
@Composable
fun AeroAuroraBackground(
    backgroundType: Int = 0,
    customBackgroundPath: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val theme = LocalPlayerTheme.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (backgroundType == 0) {
                    Modifier.drawBehind {
                        // Main dark background
                        drawRect(theme.backgroundBase)

                        // Flowing Radial Glow 1
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(theme.radial1, Color.Transparent),
                                center = Offset(size.width * 0.1f, size.height * 0.2f),
                                radius = size.maxDimension * 0.7f
                            )
                        )

                        // Flowing Radial Glow 2
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(theme.radial2, Color.Transparent),
                                center = Offset(size.width * 0.9f, size.height * 0.7f),
                                radius = size.maxDimension * 0.8f
                            )
                        )

                        // Flowing Radial Glow 3
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(theme.radial3, Color.Transparent),
                                center = Offset(size.width * 0.5f, size.height * 0.9f),
                                radius = size.maxDimension * 0.6f
                            )
                        )

                        // Aurora light sweeps (glowing curves)
                        val path1 = Path().apply {
                            moveTo(0f, size.height * 0.4f)
                            cubicTo(
                                size.width * 0.3f, size.height * 0.2f,
                                size.width * 0.7f, size.height * 0.6f,
                                size.width, size.height * 0.3f
                            )
                        }
                        drawPath(
                            path = path1,
                            brush = Brush.linearGradient(
                                colors = listOf(theme.pathSheen, theme.primaryGlow.copy(alpha = 0.15f), Color.Transparent),
                                start = Offset(0f, size.height * 0.4f),
                                end = Offset(size.width, size.height * 0.3f)
                            ),
                            style = Stroke(width = 80.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                } else {
                    Modifier.background(Color.Black)
                }
            )
    ) {
        if (backgroundType == 1) {
            Image(
                painter = painterResource(id = R.drawable.img_bg_cyber_1782298148407),
                contentDescription = "Cyberpunk Background Wallpaper",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else if (backgroundType == 2) {
            Image(
                painter = painterResource(id = R.drawable.img_bg_cosmic_1782298167295),
                contentDescription = "Cosmic Background Wallpaper",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else if (backgroundType == 3 && customBackgroundPath != null) {
            AsyncImage(
                model = File(customBackgroundPath),
                contentDescription = "Custom Background Wallpaper",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Box(modifier = Modifier.fillMaxSize(), content = content)
    }
}

/**
 * 2. Aero Glass Window Panel
 * Generates the classic Vista glassy frame, double highlight borders, and a title bar.
 */
@Composable
fun AeroGlassWindow(
    title: String,
    modifier: Modifier = Modifier,
    onCloseClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val theme = LocalPlayerTheme.current
    Column(
        modifier = modifier
            .padding(12.dp)
            .drawBehind {
                val r = 16.dp.toPx()
                
                // 1. Soft Window Shadow
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.5f),
                    topLeft = Offset(4.dp.toPx(), 6.dp.toPx()),
                    size = size,
                    cornerRadius = CornerRadius(r)
                )

                // 2. Main Glass Translucent Body (Extremely transparent frosted glass with blue tint)
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x22FFFFFF), // Super clear frosted top
                            Color(0x1F1A2F4C), // Translucent steel blue middle
                            Color(0x1A0D1726)  // Very light translucent dark bottom
                        )
                    ),
                    topLeft = Offset.Zero,
                    size = size,
                    cornerRadius = CornerRadius(r)
                )

                // 3. Diagonal Gloss Glare (reflection sweep)
                val glarePath = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width * 0.45f, 0f)
                    lineTo(0f, size.height * 0.7f)
                    close()
                }
                drawPath(
                    path = glarePath,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0x1EFFFFFF), Color(0x00FFFFFF)),
                        start = Offset(0f, 0f),
                        end = Offset(size.width * 0.45f, size.height * 0.7f)
                    )
                )

                // Horizontal sheen line below title bar
                val titleBarHeight = 44.dp.toPx()
                drawLine(
                    color = Color(0x22FFFFFF),
                    start = Offset(0f, titleBarHeight),
                    end = Offset(size.width, titleBarHeight),
                    strokeWidth = 1.dp.toPx()
                )

                // 4. White Inner Highlight Edge (Double-refraction look)
                drawRoundRect(
                    color = Color(0x40FFFFFF),
                    topLeft = Offset(1.dp.toPx(), 1.dp.toPx()),
                    size = Size(size.width - 2.dp.toPx(), size.height - 2.dp.toPx()),
                    cornerRadius = CornerRadius(r - 1.dp.toPx()),
                    style = Stroke(width = 1.2.dp.toPx())
                )

                // 5. Dark Outer Border
                drawRoundRect(
                    color = Color(0x80000000),
                    topLeft = Offset.Zero,
                    size = size,
                    cornerRadius = CornerRadius(r),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            .padding(3.dp) // Offset content inside borders
    ) {
        // Window Title Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Little glowing Vista-style round app emblem
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(theme.primaryGlow, theme.secondaryGlow),
                                center = Offset(size.width / 3, size.height / 3),
                                radius = size.width * 0.8f
                            )
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.5f),
                            radius = 3.dp.toPx(),
                            center = Offset(size.width * 0.35f, size.height * 0.35f)
                        )
                    }
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Text with glass reflection and Aero Glowing Halo (2010s vibe)
            Box(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.SansSerif,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = theme.primaryGlow.copy(alpha = 0.85f),
                            offset = Offset(0f, 0f),
                            blurRadius = 14f
                        )
                    )
                )
            }

            // Glassy Window Controls (Minimize, Maximize, Close red glow)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Minimize Button
                AeroWindowControlMiniButton()
                // Close Button (Iconic Vista glossy red)
                AeroWindowControlCloseButton(onClick = onCloseClick)
            }
        }

        // Window Inner Content Layout
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(12.dp),
            content = content
        )
    }
}

/**
 * 2a. Glassy Window Control Buttons
 */
@Composable
fun AeroWindowControlMiniButton() {
    Box(
        modifier = Modifier
            .size(26.dp, 16.dp)
            .drawBehind {
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0x33FFFFFF), Color(0x11FFFFFF))
                    ),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
                drawRoundRect(
                    color = Color(0x44FFFFFF),
                    style = Stroke(width = 1.dp.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
                // Minimize Dash
                drawRect(
                    color = Color.White,
                    topLeft = Offset(size.width * 0.3f, size.height * 0.6f),
                    size = Size(size.width * 0.4f, 2.dp.toPx())
                )
            }
    )
}

@Composable
fun AeroWindowControlCloseButton(onClick: (() -> Unit)? = null) {
    Box(
        modifier = Modifier
            .size(32.dp, 16.dp)
            .clip(RoundedCornerShape(2.dp))
            .clickable(enabled = onClick != null, onClick = onClick ?: {})
            .drawBehind {
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFE53935), Color(0xFFB71C1C))
                    )
                )
                // Double white gloss reflections
                drawRoundRect(
                    color = Color(0x60FFFFFF),
                    style = Stroke(width = 1.dp.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
                // "X" character simulation
                drawLine(
                    color = Color.White,
                    start = Offset(size.width * 0.4f, size.height * 0.3f),
                    end = Offset(size.width * 0.6f, size.height * 0.7f),
                    strokeWidth = 1.5f.dp.toPx()
                )
                drawLine(
                    color = Color.White,
                    start = Offset(size.width * 0.6f, size.height * 0.3f),
                    end = Offset(size.width * 0.4f, size.height * 0.7f),
                    strokeWidth = 1.5f.dp.toPx()
                )
            }
    )
}

/**
 * 3. Iconic Glowing Blue Play/Pause Orb Button
 * Authentic Windows Media Player 11 styling with spherical shading, crescent glint, and cyan outer glow.
 */
@Composable
fun AeroOrbButton(
    onClick: () -> Unit,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 68.dp
) {
    val theme = LocalPlayerTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val orbScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        label = "OrbScale"
    )

    val glowAlpha by rememberInfiniteTransition(label = "GlowLoop").animateFloat(
        initialValue = 0.5f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbGlow"
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = orbScale
                scaleY = orbScale
            }
            // Glow backdrop
            .drawBehind {
                val glowRadius = this.size.width * 0.6f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            theme.primaryGlow.copy(alpha = if (isPlaying) glowAlpha else 0.4f),
                            Color.Transparent
                        )
                    ),
                    radius = glowRadius,
                    center = Offset(this.size.width / 2, this.size.height / 2)
                )
            }
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .drawBehind {
                val r = this.size.width / 2

                // 1. Bottom Dark Rim Base
                drawCircle(Color(0xFF0D1B2A))

                // 2. Spherical Deep Aqua Gradient
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00E5FF), // Super cyan center
                            Color(0xFF0091EA), // Rich royal blue
                            Color(0xFF01579B), // Dark outer blue
                            Color(0xFF0A141D)  // Dark rim edge
                        ),
                        center = Offset(r, r),
                        radius = r
                    )
                )

                // 3. Highlight Crescent Glint (Creates the 3D spherical glass look on top)
                val glossPath = Path().apply {
                    addOval(
                        androidx.compose.ui.geometry.Rect(
                            left = r * 0.25f,
                            top = r * 0.08f,
                            right = r * 1.75f,
                            bottom = r * 0.9f
                        )
                    )
                }
                drawPath(
                    path = glossPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xCCFFFFFF), Color(0x00FFFFFF)),
                        startY = r * 0.08f,
                        endY = r * 0.9f
                    )
                )

                // 4. Subtle bottom reflected light
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x5500E5FF), Color.Transparent),
                        center = Offset(r, r * 1.7f),
                        radius = r * 0.6f
                    )
                )

                // 5. Hard Highlight Line at the very top edge
                drawCircle(
                    color = Color.White.copy(alpha = 0.4f),
                    radius = r - 1.dp.toPx(),
                    style = Stroke(width = 1.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Play / Pause Icon with glassy reflection
        if (isPlaying) {
            // Pause columns (WMP style metallic pill bars)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp, 20.dp)
                        .background(Color.White, RoundedCornerShape(1.dp))
                )
                Box(
                    modifier = Modifier
                        .size(6.dp, 20.dp)
                        .background(Color.White, RoundedCornerShape(1.dp))
                )
            }
        } else {
            // Play Arrow
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Play",
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .offset(x = 2.dp)
            )
        }
    }
}

/**
 * 4. Rectangular Glassy Button
 * Perfectly matches WMP 11 smaller action items with subtle click responses.
 */
@Composable
fun AeroButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val theme = LocalPlayerTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val clickOffsetY by animateDpAsState(
        targetValue = if (isPressed) 1.dp else 0.dp,
        label = "AeroBtnPress"
    )

    Box(
        modifier = modifier
            .height(38.dp)
            .offset(y = clickOffsetY)
            .drawBehind {
                val r = 6.dp.toPx()
                // Glass Base
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0x40FFFFFF), Color(0x10FFFFFF))
                    ),
                    cornerRadius = CornerRadius(r)
                )
                // Glass gloss reflection
                val reflectHeight = size.height * 0.45f
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0x22FFFFFF), Color(0x00FFFFFF))
                    ),
                    size = Size(size.width, reflectHeight),
                    cornerRadius = CornerRadius(r, r)
                )
                // Glow on pressed
                if (isPressed) {
                    drawRoundRect(
                        color = theme.primaryGlow.copy(alpha = 0.2f),
                        cornerRadius = CornerRadius(r)
                    )
                }
                // Border lines
                drawRoundRect(
                    color = Color(0x30FFFFFF),
                    style = Stroke(width = 1.dp.toPx()),
                    cornerRadius = CornerRadius(r)
                )
                drawRoundRect(
                    color = Color(0x20000000),
                    style = Stroke(width = 1.dp.toPx()),
                    cornerRadius = CornerRadius(r)
                )
            }
            .clip(RoundedCornerShape(6.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            content = content
        )
    }
}

/**
 * 5. Glassy Aero Media Seek Slider
 * Displays a glowing progress bar track, elapsed duration, and a glossy turquoise glass slider thumb.
 */
@Composable
fun AeroSlider(
    value: Float, // 0.0f to 1.0f
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    elapsedText: String = "00:00",
    remainingText: String = "00:00"
) {
    val theme = LocalPlayerTheme.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column {
            // Track layout
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val width = this.size.width.toFloat()
                            if (width > 0f) {
                                val fraction = (offset.x / width).coerceIn(0f, 1f)
                                onValueChange(fraction)
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            val width = this.size.width.toFloat()
                            if (width > 0f) {
                                val fraction = (change.position.x / width).coerceIn(0f, 1f)
                                onValueChange(fraction)
                                change.consume()
                            }
                        }
                    }
                    .drawBehind {
                        val trackHeight = 6.dp.toPx()
                        val y = size.height / 2 - trackHeight / 2
                        val r = 3.dp.toPx()

                        // 1. Dark Glass Slider Track Base
                        drawRoundRect(
                            color = Color(0x66000000),
                            topLeft = Offset(0f, y),
                            size = Size(size.width, trackHeight),
                            cornerRadius = CornerRadius(r)
                        )
                        // Inner reflection line for track
                        drawRoundRect(
                            color = Color(0x15FFFFFF),
                            topLeft = Offset(1.dp.toPx(), y + 1.dp.toPx()),
                            size = Size(size.width - 2.dp.toPx(), trackHeight - 2.dp.toPx()),
                            cornerRadius = CornerRadius(r)
                        )

                        // 2. Active Progress Glowing Green (Vista-style)
                        val activeWidth = size.width * value
                        if (activeWidth > 0) {
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(theme.activeGreen, theme.secondaryGlow)
                                ),
                                topLeft = Offset(0f, y),
                                size = Size(activeWidth, trackHeight),
                                cornerRadius = CornerRadius(r)
                            )
                            // Bright horizontal core glint in progress bar
                            drawLine(
                                color = Color.White.copy(alpha = 0.5f),
                                start = Offset(0f, y + 1.dp.toPx()),
                                end = Offset(activeWidth, y + 1.dp.toPx()),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    }
            ) {
                // Interactive Thumb
                val maxPx = constraints.maxWidth.toFloat()
                val thumbSize = 14.dp
                val thumbOffset = (maxPx * value) - (thumbSize.value * LocalContext.current.resources.displayMetrics.density / 2)
                
                // Seek slider thumb (WMP 11 glossy orb thumb)
                Box(
                    modifier = Modifier
                        .offset(x = (maxPx * value / (LocalContext.current.resources.displayMetrics.density)).dp - 7.dp)
                        .align(Alignment.CenterStart)
                        .size(14.dp, 14.dp)
                        .drawBehind {
                            val r = size.width / 2
                            // Outer circle
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color.White, theme.primaryGlow, theme.secondaryGlow),
                                    center = Offset(r, r),
                                    radius = r
                                )
                            )
                            // Glass crescent highlight
                            drawCircle(
                                color = Color.White.copy(alpha = 0.7f),
                                radius = r * 0.4f,
                                center = Offset(r * 0.7f, r * 0.7f)
                            )
                            // Outer drop ring
                            drawCircle(
                                color = Color(0x80000000),
                                radius = r,
                                style = Stroke(width = 0.8f.dp.toPx())
                            )
                        }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Timers row (Classic Aero cyan fluorescent text)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = elapsedText,
                    color = theme.primaryGlow,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = remainingText,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * 6. Live Moving Audio Visualizer Spectrum
 * Visualizes the audio frequencies using 12 moving glassy bands that react automatically!
 */
@Composable
fun AeroVisualizer(
    isPlaying: Boolean,
    style: String = "bars",
    bandsCount: Int = 12,
    modifier: Modifier = Modifier
) {
    val theme = LocalPlayerTheme.current

    // Keep state tracking responsive to changes in bandsCount
    val animStates = remember(bandsCount) {
        List(bandsCount) { Animatable(0.1f) }
    }
    val peakStates = remember(bandsCount) {
        List(bandsCount) { mutableStateOf(0.1f) }
    }

    if (isPlaying) {
        LaunchedEffect(key1 = isPlaying, key2 = bandsCount) {
            while (isPlaying) {
                for (i in 0 until bandsCount) {
                    val nextVal = Random.nextFloat() * 0.85f + 0.15f
                    launch {
                        animStates[i].animateTo(
                            targetValue = nextVal,
                            animationSpec = tween(Random.nextInt(120, 240), easing = FastOutSlowInEasing)
                        )
                    }
                }
                delay(150)
            }
        }
        // Peak decay effect loop
        LaunchedEffect(key1 = isPlaying, key2 = bandsCount) {
            while (isPlaying) {
                for (i in 0 until bandsCount) {
                    val currentVal = animStates[i].value
                    val currentPeak = peakStates[i].value
                    if (currentVal > currentPeak) {
                        peakStates[i].value = currentVal
                    } else {
                        peakStates[i].value = maxOf(0.05f, currentPeak - 0.04f)
                    }
                }
                delay(60)
            }
        }
    } else {
        // Slowly collapse bars back to 0.05
        LaunchedEffect(key1 = isPlaying, key2 = bandsCount) {
            for (i in 0 until bandsCount) {
                launch {
                    animStates[i].animateTo(
                        targetValue = 0.05f,
                        animationSpec = tween(500)
                    )
                }
                peakStates[i].value = 0.05f
            }
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .border(1.dp, Color(0x30FFFFFF), RoundedCornerShape(4.dp))
            .background(Color(0x50000000), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        val spacing = 4.dp.toPx()
        val totalSpacing = spacing * (bandsCount - 1)
        val barWidth = (size.width - totalSpacing) / bandsCount

        if (style == "bars") {
            // --- DRAW BARS STYLE ---
            for (i in 0 until bandsCount) {
                val h = size.height * animStates[i].value
                val x = i * (barWidth + spacing)
                val y = size.height - h

                // Dynamic gradient based on active theme
                val gradient = Brush.verticalGradient(
                    colors = listOf(
                        theme.peakColor,           // Peak Top
                        theme.primaryGlow,         // Active Mid
                        theme.secondaryGlow        // Base
                    ),
                    startY = y,
                    endY = size.height
                )

                // Draw visualizer bar
                drawRoundRect(
                    brush = gradient,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, h),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )

                // Draw Peak Hold indicator block above the bar
                val peakH = size.height * peakStates[i].value
                val peakY = size.height - peakH
                if (peakY < y - 2.dp.toPx()) {
                    drawRect(
                        color = theme.peakColor,
                        topLeft = Offset(x, peakY),
                        size = Size(barWidth, 2.dp.toPx())
                    )
                }

                // Highlighting glossy line inside the visualizer bar
                if (barWidth > 3f) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.4f),
                        start = Offset(x + 1.dp.toPx(), y + 1.dp.toPx()),
                        end = Offset(x + 1.dp.toPx(), size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
        } else {
            // --- DRAW LINES / WAVE STYLE ---
            val linePath = Path()
            val points = mutableListOf<Offset>()

            for (i in 0 until bandsCount) {
                val h = size.height * animStates[i].value
                val x = i * (barWidth + spacing) + barWidth / 2
                val y = size.height - h
                points.add(Offset(x, y))
            }

            if (points.isNotEmpty()) {
                linePath.moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    val prev = points[i - 1]
                    val curr = points[i]
                    linePath.cubicTo(
                        (prev.x + curr.x) / 2, prev.y,
                        (prev.x + curr.x) / 2, curr.y,
                        curr.x, curr.y
                    )
                }

                // Draw smooth wave path
                drawPath(
                    path = linePath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(theme.primaryGlow, theme.secondaryGlow, theme.primaryGlow)
                    ),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw glowing aura under the path
                val filledPath = Path().apply {
                    addPath(linePath)
                    lineTo(points.last().x, size.height)
                    lineTo(points.first().x, size.height)
                    close()
                }
                drawPath(
                    path = filledPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(theme.primaryGlow.copy(alpha = 0.25f), Color.Transparent)
                    )
                )

                // Draw dots on peaks
                for (pt in points) {
                    drawCircle(
                        color = theme.peakColor,
                        radius = 3.dp.toPx(),
                        center = pt
                    )
                }
            }
        }
    }
}
