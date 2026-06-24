package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long, // in milliseconds
    val uri: String, // File path or streaming URL
    val isFavorite: Boolean = false,
    val albumArtUri: String? = null,
    val format: String = "MP3", // e.g., MP3, WAV, AAC, FLAC, M4A
    val isDemo: Boolean = false,
    val lyrics: String? = null
)
