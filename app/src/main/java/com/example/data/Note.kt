package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val colorHex: Long = 0xFFFFF9C4, // Default soft yellow sticky note
    val isPinned: Boolean = false,
    val fontType: String = "Serif",
    val category: String = "Personal"
)
