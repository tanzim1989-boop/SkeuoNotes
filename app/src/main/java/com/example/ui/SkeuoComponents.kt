package com.example.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- COLOR PALETTE FOR SKEUOMORPHISM ---
object SkeuoColor {
    // Desk colors (rich wood / dark leather)
    val DeskBg = Color(0xFF2C1E14)
    val DeskLight = Color(0xFF422E1F)
    val DeskDark = Color(0xFF160F09)
    val StitchColor = Color(0xFF5D4037)

    // Paper Base Colors (Yellow pads, Warm Whites, Blue notepad)
    val StickyYellow = Color(0xFFFFF9C4)
    val StickyYellowDark = Color(0xFFFFF176)
    val PaperLineBlue = Color(0xFFB3E5FC)
    val PaperMarginRed = Color(0xFFFF8A80)

    val PaperBlue = Color(0xFFE3F2FD)
    val PaperBlueDark = Color(0xFF90CAF9)

    val PaperGreen = Color(0xFFE8F5E9)
    val PaperGreenDark = Color(0xFFA5D6A7)

    val PaperPink = Color(0xFFFCE4EC)
    val PaperPinkDark = Color(0xFFF48FB1)

    val PaperWhite = Color(0xFFFFFDF9)
    val PaperWhiteDark = Color(0xFFECEFF1)

    // Button shading
    val HighlightWhite = Color(0x60FFFFFF)
    val ShadowDark = Color(0x70000000)

    // Pushpin colors
    val PinRed = Color(0xFFE53935)
    val PinRedDark = Color(0xFFB71C1C)
    val PinBlue = Color(0xFF1E88E5)
    val PinBlueDark = Color(0xFF0D47A1)
    val PinMetal = Color(0xFFB0BEC5)
    val PinMetalDark = Color(0xFF546E7A)
}

/**
 * 1. Rich Desk Backdrop with leather stitch and radial lighting
 */
@Composable
fun SkeuomorphicDeskBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                // Draw rich wood/leather desk gradient
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(SkeuoColor.DeskLight, SkeuoColor.DeskBg, SkeuoColor.DeskDark),
                        center = Offset(size.width / 2, size.height / 3),
                        radius = size.maxDimension * 0.8f
                    )
                )

                // Add a cute border stitching effect to simulate a real desk blotter!
                val inset = 16.dp.toPx()
                val width = size.width - inset * 2
                val height = size.height - inset * 2

                // Draw leather stitching lines (dashed)
                drawRoundRect(
                    color = SkeuoColor.StitchColor,
                    topLeft = Offset(inset, inset),
                    size = Size(width, height),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(12f, 8f), 0f
                        )
                    )
                )
            },
        content = content
    )
}

/**
 * 2. Skeuomorphic Paper Component
 * Draws ruled legal pad lines, a red margin line, a torn edge effect at the top or bottom, and 3D stacking shadows.
 */
@Composable
fun SkeuomorphicPaper(
    modifier: Modifier = Modifier,
    paperColor: Color = SkeuoColor.StickyYellow,
    hasLines: Boolean = true,
    hasSpiral: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val baseModifier = if (onClick != null) {
        modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    } else {
        modifier
    }

    Box(
        modifier = baseModifier
            .padding(8.dp)
            // Multi-layered stack shadow
            .drawBehind {
                val shadowOffset1 = 4.dp.toPx()
                val shadowOffset2 = 8.dp.toPx()

                // Bottom shadow page layer
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.15f),
                    topLeft = Offset(shadowOffset2, shadowOffset2),
                    size = size,
                    cornerRadius = CornerRadius(10.dp.toPx())
                )

                // Middle shadow page layer
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.2f),
                    topLeft = Offset(shadowOffset1, shadowOffset1),
                    size = size,
                    cornerRadius = CornerRadius(8.dp.toPx())
                )

                // Background paper body
                drawRoundRect(
                    color = paperColor,
                    topLeft = Offset(0f, 0f),
                    size = size,
                    cornerRadius = CornerRadius(6.dp.toPx())
                )

                // Lined paper rules (Blue horizontal lines)
                if (hasLines) {
                    val lineSpacing = 28.dp.toPx()
                    val marginLineX = 40.dp.toPx()
                    val totalLines = (size.height / lineSpacing).toInt()

                    for (i in 1..totalLines) {
                        val y = i * lineSpacing
                        // Draw horizontal rule line
                        drawLine(
                            color = SkeuoColor.PaperLineBlue,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Left vertical margin line (Red)
                    drawLine(
                        color = SkeuoColor.PaperMarginRed,
                        start = Offset(marginLineX, 0f),
                        end = Offset(marginLineX, size.height),
                        strokeWidth = 1.5f.dp.toPx()
                    )
                }

                // If spiral-bound, draw binder clip holes at the top
                if (hasSpiral) {
                    val holeRadius = 5.dp.toPx()
                    val holeSpacing = 40.dp.toPx()
                    val startX = 30.dp.toPx()
                    val y = 20.dp.toPx()
                    var currentX = startX
                    while (currentX < size.width - 20.dp.toPx()) {
                        drawCircle(
                            color = SkeuoColor.DeskBg,
                            radius = holeRadius,
                            center = Offset(currentX, y)
                        )
                        currentX += holeSpacing
                    }
                }
            }
            .padding(
                start = if (hasLines) 48.dp else 16.dp,
                end = 16.dp,
                top = if (hasSpiral) 40.dp else 16.dp,
                bottom = 16.dp
            )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

/**
 * 3. Skeuomorphic Spiral Binding Component
 * Connects the page holes to the top header with glossy metallic coils.
 */
@Composable
fun SkeuomorphicSpiralBinding(
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
    ) {
        val loopSpacing = 40.dp.toPx()
        val startX = 30.dp.toPx()
        val loopWidth = 12.dp.toPx()
        val loopHeight = 28.dp.toPx()

        var currentX = startX
        while (currentX < size.width - 20.dp.toPx()) {
            // Draw a realistic shiny silver coil link
            // Bottom shadow of coil
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.4f),
                topLeft = Offset(currentX - 2.dp.toPx(), 2.dp.toPx()),
                size = Size(loopWidth, loopHeight),
                cornerRadius = CornerRadius(4.dp.toPx())
            )

            // Outer coil body (metallic brush)
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        SkeuoColor.PinMetalDark,
                        SkeuoColor.PinMetal,
                        Color.White,
                        SkeuoColor.PinMetalDark
                    ),
                    start = Offset(currentX, 0f),
                    end = Offset(currentX + loopWidth, loopHeight)
                ),
                topLeft = Offset(currentX, 0f),
                size = Size(loopWidth, loopHeight),
                cornerRadius = CornerRadius(4.dp.toPx())
            )

            currentX += loopSpacing
        }
    }
}

/**
 * 4. Realistic Pushpin component
 * Displays a 3D red or blue pushpin with a realistic transparent drop shadow.
 */
@Composable
fun SkeuomorphicPushpin(
    modifier: Modifier = Modifier,
    color: Color = SkeuoColor.PinRed,
    colorDark: Color = SkeuoColor.PinRedDark
) {
    Canvas(
        modifier = modifier
            .size(36.dp)
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 3

        // 1. Cast shadow of pushpin (soft and offset down and right)
        val shadowOffset = Offset(10.dp.toPx(), 12.dp.toPx())
        val pinShadowPath = Path().apply {
            moveTo(centerX + shadowOffset.x, centerY + shadowOffset.y)
            lineTo(centerX + shadowOffset.x - 3.dp.toPx(), centerY + shadowOffset.y + 12.dp.toPx()) // Needle shadow
            lineTo(centerX + shadowOffset.x + 3.dp.toPx(), centerY + shadowOffset.y + 12.dp.toPx())
            close()
        }
        drawPath(pinShadowPath, Color.Black.copy(alpha = 0.35f))
        drawCircle(
            color = Color.Black.copy(alpha = 0.4f),
            radius = 7.dp.toPx(),
            center = Offset(centerX + shadowOffset.x, centerY + shadowOffset.y - 2.dp.toPx())
        )

        // 2. Silver metal needle (point)
        val needlePath = Path().apply {
            moveTo(centerX, centerY)
            lineTo(centerX - 1.5f.dp.toPx(), centerY + 14.dp.toPx())
            lineTo(centerX + 1.5f.dp.toPx(), centerY + 14.dp.toPx())
            close()
        }
        drawPath(needlePath, SkeuoColor.PinMetal)
        drawPath(needlePath, SkeuoColor.PinMetalDark, style = Stroke(0.5f.dp.toPx()))

        // 3. Plastic Pin Body
        // Pin head top circle
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, color, colorDark),
                center = Offset(centerX - 2.dp.toPx(), centerY - 5.dp.toPx()),
                radius = 7.dp.toPx()
            ),
            radius = 7.dp.toPx(),
            center = Offset(centerX, centerY - 4.dp.toPx())
        )

        // Pin collar/base
        val collarWidth = 14.dp.toPx()
        val collarHeight = 5.dp.toPx()
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(colorDark, color, Color.White, colorDark)
            ),
            topLeft = Offset(centerX - collarWidth / 2, centerY),
            size = Size(collarWidth, collarHeight),
            cornerRadius = CornerRadius(2.dp.toPx())
        )

        // Pin grip handle (middle block)
        val gripWidth = 10.dp.toPx()
        val gripHeight = 8.dp.toPx()
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(colorDark, color, Color.White, colorDark)
            ),
            topLeft = Offset(centerX - gripWidth / 2, centerY - gripHeight),
            size = Size(gripWidth, gripHeight),
            cornerRadius = CornerRadius(1.dp.toPx())
        )
    }
}

/**
 * 5. Skeuomorphic Beveled Tactile Button
 * Looks like a real clicky physical plastic key or a wooden stamp button.
 * Physically sinks down slightly when pressed and updates the beveled edge thickness.
 */
@Composable
fun SkeuomorphicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    baseColor: Color = Color(0xFFD7CCC8), // Soft classic warm wood-plastic
    darkColor: Color = Color(0xFF8D6E63),
    lightColor: Color = Color(0xFFEFEBE9),
    contentColor: Color = Color(0xFF4E342E),
    isEnabled: Boolean = true,
    height: Dp = 48.dp,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Tactile animations
    val offsetAnimation by animateDpAsState(
        targetValue = if (isPressed && isEnabled) 3.dp else 0.dp,
        label = "ButtonPressOffset"
    )
    val shadowOpacity by animateFloatAsState(
        targetValue = if (isPressed && isEnabled) 0.15f else 0.35f,
        label = "ButtonShadowOpacity"
    )

    Box(
        modifier = modifier
            .height(height)
            .padding(horizontal = 4.dp)
            // Outer 3D Drop Shadow on desk/paper
            .drawBehind {
                if (isEnabled) {
                    val shadowOffset = if (isPressed) 2.dp.toPx() else 5.dp.toPx()
                    drawRoundRect(
                        color = Color.Black.copy(alpha = shadowOpacity),
                        topLeft = Offset(shadowOffset, shadowOffset),
                        size = size,
                        cornerRadius = CornerRadius(8.dp.toPx())
                    )
                }
            }
            // Sinking Offset
            .offset(y = offsetAnimation, x = offsetAnimation)
            .clip(RoundedCornerShape(8.dp))
            .background(baseColor)
            // Bevel edges (light top, dark bottom)
            .drawBehind {
                val bevelSize = 2.dp.toPx()
                // Top Highlight (beveled)
                drawLine(
                    color = if (isPressed) darkColor else lightColor,
                    start = Offset(0f, bevelSize / 2),
                    end = Offset(size.width, bevelSize / 2),
                    strokeWidth = bevelSize
                )
                // Left Highlight
                drawLine(
                    color = if (isPressed) darkColor else lightColor,
                    start = Offset(bevelSize / 2, 0f),
                    end = Offset(bevelSize / 2, size.height),
                    strokeWidth = bevelSize
                )
                // Bottom Shadow
                drawLine(
                    color = if (isPressed) lightColor else darkColor,
                    start = Offset(0f, size.height - bevelSize / 2),
                    end = Offset(size.width, size.height - bevelSize / 2),
                    strokeWidth = bevelSize
                )
                // Right Shadow
                drawLine(
                    color = if (isPressed) lightColor else darkColor,
                    start = Offset(size.width - bevelSize / 2, 0f),
                    end = Offset(size.width - bevelSize / 2, size.height),
                    strokeWidth = bevelSize
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = isEnabled,
                onClick = onClick
            )
            .padding(horizontal = 16.dp),
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
 * 6. Leather Tag Category Selector
 * Looks like a physical leather bookmark tab attached to the page edge.
 */
@Composable
fun SkeuomorphicLeatherTag(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFFC2185B),
    inactiveColor: Color = Color(0xFF78909C)
) {
    val offsetAnimation by animateDpAsState(
        targetValue = if (isSelected) 4.dp else 0.dp,
        label = "LeatherTagOffset"
    )

    Box(
        modifier = modifier
            .offset(y = -offsetAnimation)
            .width(64.dp)
            .height(44.dp)
            .drawBehind {
                val shadowOffset = if (isSelected) 3.dp.toPx() else 1.dp.toPx()
                // Cast Shadow
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.3f),
                    topLeft = Offset(2.dp.toPx(), shadowOffset),
                    size = size,
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }
            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
            .background(if (isSelected) activeColor else inactiveColor)
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.SansSerif
        )
    }
}

/**
 * 7. Skeuomorphic Paperclip Component
 * Draws a realistic, color-coded 3D paperclip overlapping the note page.
 */
@Composable
fun SkeuomorphicPaperclip(
    category: String,
    modifier: Modifier = Modifier
) {
    val clipColor = when (category) {
        "Work" -> Color(0xFF29B6F6) // Bright Sky Blue
        "Personal" -> Color(0xFFEF5350) // Warm Red
        "Ideas" -> Color(0xFFFFCA28) // Amber/Gold
        "Tasks" -> Color(0xFF66BB6A) // Green
        else -> Color(0xFFAB47BC) // Purple
    }

    val clipColorDark = when (category) {
        "Work" -> Color(0xFF0288D1)
        "Personal" -> Color(0xFFD32F2F)
        "Ideas" -> Color(0xFFF57C00)
        "Tasks" -> Color(0xFF388E3C)
        else -> Color(0xFF7B1FA2)
    }

    Canvas(
        modifier = modifier
            .width(28.dp)
            .height(54.dp)
    ) {
        val w = size.width
        val h = size.height

        // 1. Soft drop shadow cast onto paper
        val shadowOffset = Offset(3.dp.toPx(), 4.dp.toPx())
        val shadowPath = Path().apply {
            moveTo(w * 0.35f + shadowOffset.x, h * 0.45f + shadowOffset.y)
            lineTo(w * 0.35f + shadowOffset.x, h * 0.25f + shadowOffset.y)
            cubicTo(
                w * 0.35f + shadowOffset.x, h * 0.15f + shadowOffset.y,
                w * 0.65f + shadowOffset.x, h * 0.15f + shadowOffset.y,
                w * 0.65f + shadowOffset.x, h * 0.25f + shadowOffset.y
            )
            lineTo(w * 0.65f + shadowOffset.x, h * 0.75f + shadowOffset.y)
            cubicTo(
                w * 0.65f + shadowOffset.x, h * 0.90f + shadowOffset.y,
                w * 0.15f + shadowOffset.x, h * 0.90f + shadowOffset.y,
                w * 0.15f + shadowOffset.x, h * 0.75f + shadowOffset.y
            )
            lineTo(w * 0.15f + shadowOffset.x, h * 0.25f + shadowOffset.y)
            cubicTo(
                w * 0.15f + shadowOffset.x, h * 0.05f + shadowOffset.y,
                w * 0.85f + shadowOffset.x, h * 0.05f + shadowOffset.y,
                w * 0.85f + shadowOffset.x, h * 0.25f + shadowOffset.y
            )
            lineTo(w * 0.85f + shadowOffset.x, h * 0.55f + shadowOffset.y)
        }
        drawPath(
            path = shadowPath,
            color = Color.Black.copy(alpha = 0.25f),
            style = Stroke(width = 4.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )

        // 2. Outer paperclip shape (with beveled/shiny metal effect)
        val clipPath = Path().apply {
            moveTo(w * 0.35f, h * 0.45f)
            lineTo(w * 0.35f, h * 0.25f)
            cubicTo(
                w * 0.35f, h * 0.15f,
                w * 0.65f, h * 0.15f,
                w * 0.65f, h * 0.25f
            )
            lineTo(w * 0.65f, h * 0.75f)
            cubicTo(
                w * 0.65f, h * 0.90f,
                w * 0.15f, h * 0.90f,
                w * 0.15f, h * 0.75f
            )
            lineTo(w * 0.15f, h * 0.25f)
            cubicTo(
                w * 0.15f, h * 0.05f,
                w * 0.85f, h * 0.05f,
                w * 0.85f, h * 0.25f
            )
            lineTo(w * 0.85f, h * 0.55f)
        }

        // Clip Base
        drawPath(
            path = clipPath,
            color = clipColorDark,
            style = Stroke(width = 4.5f.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )

        // Clip Core Highlight (makes it look 3D and shiny)
        drawPath(
            path = clipPath,
            color = clipColor,
            style = Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )

        // Highlight glint line
        drawPath(
            path = clipPath,
            color = Color.White.copy(alpha = 0.6f),
            style = Stroke(width = 1.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
    }
}

