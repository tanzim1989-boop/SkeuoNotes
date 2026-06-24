package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.NoteDatabase
import com.example.data.NoteRepository
import com.example.ui.NoteViewModel
import com.example.ui.screens.NoteEditScreen
import com.example.ui.screens.NotesListScreen
import com.example.ui.theme.MyApplicationTheme

sealed class Screen {
    object NotesList : Screen()
    data class EditNote(val noteId: Int) : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Core room persistence components
        val database = NoteDatabase.getDatabase(applicationContext)
        val repository = NoteRepository(database.noteDao)
        val viewModel = ViewModelProvider(this, NoteViewModel.Factory(repository))[NoteViewModel::class.java]

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.NotesList) }

                    // Animate between screens with elegant fade
                    AnimatedContent(
                        targetState = currentScreen,
                        label = "ScreenTransition"
                    ) { screen ->
                        when (screen) {
                            is Screen.NotesList -> {
                                NotesListScreen(
                                    viewModel = viewModel,
                                    onNavigateToEdit = { id ->
                                        currentScreen = Screen.EditNote(id)
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            is Screen.EditNote -> {
                                NoteEditScreen(
                                    viewModel = viewModel,
                                    noteId = screen.noteId,
                                    onNavigateBack = {
                                        currentScreen = Screen.NotesList
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
