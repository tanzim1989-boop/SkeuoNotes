package com.example.ui.theme

import androidx.compose.ui.graphics.Color

enum class PlayerTheme(
    val displayName: String,
    val backgroundBase: Color,
    val primaryGlow: Color,
    val secondaryGlow: Color,
    val peakColor: Color,
    val activeGreen: Color,
    val radial1: Color,
    val radial2: Color,
    val radial3: Color,
    val pathSheen: Color
) {
    VISTA_AERO(
        displayName = "Aero Sky (Classic)",
        backgroundBase = Color(0xFF040A12),
        primaryGlow = Color(0xFF00E5FF),
        secondaryGlow = Color(0xFF0091EA),
        peakColor = Color(0xFFFFD600),
        activeGreen = Color(0xFF00E676),
        radial1 = Color(0x9900BFA5),
        radial2 = Color(0xB20091EA),
        radial3 = Color(0x808E24AA),
        pathSheen = Color(0x3300E5FF)
    ),
    COBALT_BLUE(
        displayName = "Cobalt Blue (WMP 12)",
        backgroundBase = Color(0xFF010714),
        primaryGlow = Color(0xFF2979FF),
        secondaryGlow = Color(0xFF1565C0),
        peakColor = Color(0xFFFF5252),
        activeGreen = Color(0xFF00E5FF),
        radial1 = Color(0x990D47A1),
        radial2 = Color(0xB21565C0),
        radial3 = Color(0x8000E5FF),
        pathSheen = Color(0x332979FF)
    ),
    NEON_CYBER(
        displayName = "Neon Cyber (Winamp)",
        backgroundBase = Color(0xFF0E021A),
        primaryGlow = Color(0xFFE040FB),
        secondaryGlow = Color(0xFF651FFF),
        peakColor = Color(0xFF00E676),
        activeGreen = Color(0xFFD500F9),
        radial1 = Color(0x994A148C),
        radial2 = Color(0xB2311B92),
        radial3 = Color(0x80D500F9),
        pathSheen = Color(0x33E040FB)
    ),
    CARBON_SLATE(
        displayName = "Carbon Glass (Slate)",
        backgroundBase = Color(0xFF0D0F12),
        primaryGlow = Color(0xFFCFD8DC),
        secondaryGlow = Color(0xFF455A64),
        peakColor = Color(0xFFFFC107),
        activeGreen = Color(0xFFB0BEC5),
        radial1 = Color(0x7737474F),
        radial2 = Color(0x77455A64),
        radial3 = Color(0x5590A4AE),
        pathSheen = Color(0x22FFFFFF)
    ),
    SUNSET_VISTA(
        displayName = "Sunset Vista (Amber)",
        backgroundBase = Color(0xFF140702),
        primaryGlow = Color(0xFFFF6D00),
        secondaryGlow = Color(0xFFFF3D00),
        peakColor = Color(0xFFFFD600),
        activeGreen = Color(0xFFFF9100),
        radial1 = Color(0x99E65100),
        radial2 = Color(0xB2BF360C),
        radial3 = Color(0x80FFAB40),
        pathSheen = Color(0x33FF6D00)
    )
}
