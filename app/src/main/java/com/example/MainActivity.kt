package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.SongDatabase
import com.example.data.SongRepository
import com.example.ui.AeroAuroraBackground
import com.example.ui.PlayerViewModel
import com.example.ui.screens.PlayerScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Room persistence database and repository setup
        val database = SongDatabase.getDatabase(applicationContext)
        val repository = SongRepository(database.songDao())
        val viewModel = ViewModelProvider(this, PlayerViewModel.Factory(repository, applicationContext))[PlayerViewModel::class.java]

        setContent {
            MyApplicationTheme {
                val backgroundType by viewModel.backgroundType.collectAsState()
                val customBackgroundPath by viewModel.customBackgroundPath.collectAsState()
                // Render the complete app inside the beautiful Aero Aurora background
                AeroAuroraBackground(
                    backgroundType = backgroundType,
                    customBackgroundPath = customBackgroundPath,
                    modifier = Modifier.fillMaxSize()
                ) {
                    PlayerScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
