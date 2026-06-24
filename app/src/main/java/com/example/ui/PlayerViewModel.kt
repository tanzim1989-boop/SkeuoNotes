package com.example.ui

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.media.MediaPlayer
import android.media.audiofx.Equalizer
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Song
import com.example.data.SongRepository
import com.example.ui.theme.PlayerTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val repository: SongRepository,
    context: Context
) : ViewModel() {

    // Audio Playback
    private var mediaPlayer: MediaPlayer? = null
    private var realEqualizer: Equalizer? = null

    // UI States
    private val _allSongsState = MutableStateFlow<List<Song>>(emptyList())
    val allSongsState: StateFlow<List<Song>> = _allSongsState.asStateFlow()

    private val _favoriteSongsState = MutableStateFlow<List<Song>>(emptyList())
    val favoriteSongsState: StateFlow<List<Song>> = _favoriteSongsState.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _shuffleEnabled = MutableStateFlow(false)
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled.asStateFlow()

    private val _repeatEnabled = MutableStateFlow(false)
    val repeatEnabled: StateFlow<Boolean> = _repeatEnabled.asStateFlow()

    // 5-Band Equalizer levels: Sliders (0.0 to 1.0)
    val equalizerBands = mutableStateListOf(0.5f, 0.5f, 0.5f, 0.5f, 0.5f)
    val equalizerLabels = listOf("Bass (60Hz)", "Mid-Bass (230Hz)", "Mid (910Hz)", "Presence (4kHz)", "Brilliance (14kHz)")

    // 2010s Custom Themes & Audio Customization Preferences (Shared Preferences integration)
    private val sharedPrefs = context.getSharedPreferences("aero_music_player_prefs", Context.MODE_PRIVATE)

    private val _currentTheme = MutableStateFlow(
        try {
            PlayerTheme.valueOf(sharedPrefs.getString("current_theme", PlayerTheme.VISTA_AERO.name) ?: PlayerTheme.VISTA_AERO.name)
        } catch (e: Exception) {
            PlayerTheme.VISTA_AERO
        }
    )
    val currentTheme: StateFlow<PlayerTheme> = _currentTheme.asStateFlow()

    private val _autoPlayNext = MutableStateFlow(sharedPrefs.getBoolean("auto_play_next", true))
    val autoPlayNext: StateFlow<Boolean> = _autoPlayNext.asStateFlow()

    private val _bassBoostEnabled = MutableStateFlow(sharedPrefs.getBoolean("bass_boost_enabled", false))
    val bassBoostEnabled: StateFlow<Boolean> = _bassBoostEnabled.asStateFlow()

    private val _reverbEnabled = MutableStateFlow(sharedPrefs.getBoolean("reverb_enabled", false))
    val reverbEnabled: StateFlow<Boolean> = _reverbEnabled.asStateFlow()

    private val _visualizerStyle = MutableStateFlow(sharedPrefs.getString("visualizer_style", "bars") ?: "bars")
    val visualizerStyle: StateFlow<String> = _visualizerStyle.asStateFlow()

    private val _visualizerBands = MutableStateFlow(sharedPrefs.getInt("visualizer_bands", 12))
    val visualizerBands: StateFlow<Int> = _visualizerBands.asStateFlow()

    private val _backgroundType = MutableStateFlow(sharedPrefs.getInt("background_type", 0))
    val backgroundType: StateFlow<Int> = _backgroundType.asStateFlow()

    private val _customBackgroundPath = MutableStateFlow(sharedPrefs.getString("custom_background_path", null))
    val customBackgroundPath: StateFlow<String?> = _customBackgroundPath.asStateFlow()

    // Lyrics AI State
    private val _lyricsLoadingState = MutableStateFlow(false)
    val lyricsLoadingState: StateFlow<Boolean> = _lyricsLoadingState.asStateFlow()

    private val _lyricsErrorState = MutableStateFlow<String?>(null)
    val lyricsErrorState: StateFlow<String?> = _lyricsErrorState.asStateFlow()

    fun setTheme(theme: PlayerTheme) {
        _currentTheme.value = theme
        sharedPrefs.edit().putString("current_theme", theme.name).apply()
    }

    fun setAutoPlayNext(enabled: Boolean) {
        _autoPlayNext.value = enabled
        sharedPrefs.edit().putBoolean("auto_play_next", enabled).apply()
    }

    fun setBassBoostEnabled(enabled: Boolean) {
        _bassBoostEnabled.value = enabled
        sharedPrefs.edit().putBoolean("bass_boost_enabled", enabled).apply()
        if (enabled) {
            setEqualizerBand(0, 0.95f) // software boost lowest frequency
            setEqualizerBand(1, 0.80f) // software boost mid-bass frequency
        } else {
            setEqualizerBand(0, 0.50f)
            setEqualizerBand(1, 0.50f)
        }
    }

    fun setReverbEnabled(enabled: Boolean) {
        _reverbEnabled.value = enabled
        sharedPrefs.edit().putBoolean("reverb_enabled", enabled).apply()
    }

    fun setVisualizerStyle(style: String) {
        _visualizerStyle.value = style
        sharedPrefs.edit().putString("visualizer_style", style).apply()
    }

    fun setVisualizerBands(bands: Int) {
        _visualizerBands.value = bands
        sharedPrefs.edit().putInt("visualizer_bands", bands).apply()
    }

    fun setBackgroundType(type: Int) {
        _backgroundType.value = type
        sharedPrefs.edit().putInt("background_type", type).apply()
    }

    fun saveCustomBackground(context: Context, uri: Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@launch
                val bytes = inputStream.readBytes()
                inputStream.close()
                if (bytes.isEmpty()) return@launch

                val options = android.graphics.BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

                val reqWidth = 1080
                val reqHeight = 1920
                var inSampleSize = 1
                val height = options.outHeight
                val width = options.outWidth
                if (height > reqHeight || width > reqWidth) {
                    val halfHeight = height / 2
                    val halfWidth = width / 2
                    while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                        inSampleSize *= 2
                    }
                }

                val decodeOptions = android.graphics.BitmapFactory.Options().apply {
                    this.inSampleSize = inSampleSize
                }
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions) ?: return@launch

                val file = java.io.File(context.filesDir, "custom_background.jpg")
                val outputStream = java.io.FileOutputStream(file)
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, outputStream)
                outputStream.flush()
                outputStream.close()
                bitmap.recycle()

                val path = file.absolutePath
                _customBackgroundPath.value = path
                sharedPrefs.edit().putString("custom_background_path", path).apply()
                setBackgroundType(3)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private var progressUpdateJob: Job? = null
    private var songQueue: List<Song> = emptyList()

    init {
        // Collect database updates
        viewModelScope.launch {
            repository.allSongs.collectLatest { songs ->
                _allSongsState.value = songs
                songQueue = songs
                // If there is no current song, select the first one
                if (_currentSong.value == null && songs.isNotEmpty()) {
                    _currentSong.value = songs.first()
                    _duration.value = songs.first().duration
                }
            }
        }

        viewModelScope.launch {
            repository.favoriteSongs.collectLatest { favorites ->
                _favoriteSongsState.value = favorites
            }
        }
    }

    /**
     * Preloads high-quality sample streams if database is empty
     */
    fun checkAndPreloadDemoSongs() {
        viewModelScope.launch {
            val count = repository.getSongCount()
            if (count == 0) {
                val demoSongs = listOf(
                    Song(
                        title = "Cyberpunk Horizon",
                        artist = "SoundHelix Orchestra",
                        album = "Windows Aero Hits Vol. I",
                        duration = 372000L,
                        uri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                        isFavorite = false,
                        format = "MP3",
                        isDemo = true,
                        lyrics = """[00:00] (Synth Introduction - Ambient & Low-fi)
[00:15] Across the neon skyline, the grid begins to glow...
[00:30] Running through the translucent streams of glass and metal...
[00:45] We search for a digital memory, lost in the shadows.
[01:10] (Electric Guitar Solo - Cyberpunk Horizon theme)
[01:40] Floating on a wave of light, higher than the sky,
[02:00] Refracting colors through the glass, we watch the shadows fly.
[02:30] A system reboot, a new horizon, translucent and clear...
[03:00] (Bass Drop & High-speed Arpeggiator)
[04:00] (Outro - Soft glassy delay fading out...)"""
                    ),
                    Song(
                        title = "Neon Dreams",
                        artist = "SoundHelix Orchestra",
                        album = "Windows Aero Hits Vol. II",
                        duration = 423000L,
                        uri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                        isFavorite = true,
                        format = "MP3",
                        isDemo = true,
                        lyrics = """[00:00] (Opening Pads - Atmospheric Synth)
[00:20] Dreaming in shades of electric blue and violet glow...
[00:40] Underneath the Aero sky, we let the music flow.
[01:00] Every beat is a pulse in the network, a signal in the dark.
[01:30] (Techno Drums kick in - Driving rhythm)
[02:00] Translucent windows to another world, where dreams never fade,
[02:30] Locked inside this high-contrast world of light and shade.
[03:30] (Keyboard Synth Harmony - Melodious Interlude)
[04:30] Feel the digital pulse, neon dreams forever in our eyes...
[05:30] (Outro - Soft rhythmic mechanical beats)"""
                    ),
                    Song(
                        title = "Aero Chill ambient",
                        artist = "Glassy Synth Pro",
                        album = "Vista Chillout",
                        duration = 502000L,
                        uri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                        isFavorite = false,
                        format = "WAV",
                        isDemo = true,
                        lyrics = """[00:00] (Soft Ambient Chimes - Water droplets and glass effects)
[00:30] Breathe in the cool, clear mountain breeze...
[01:00] Floating above the translucent white clouds, in perfect peace.
[01:45] The window glass is soft and cool, reflecting the silent night,
[02:30] Let the glassy ambient chords carry you into the starlight.
[03:30] (Delicate Acoustic Guitar plucking & Glass marimba)
[04:30] Aero chill, a quiet sanctuary in a noisy universe...
[05:30] (Slow peaceful fade out)"""
                    ),
                    Song(
                        title = "Futuristic Skyline",
                        artist = "SoundHelix Orchestra",
                        album = "Vista Chillout",
                        duration = 302000L,
                        uri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                        isFavorite = false,
                        format = "FLAC",
                        isDemo = true,
                        lyrics = """[00:00] (Fast Sci-Fi Synth Arpeggio)
[00:15] Rocketing past the hyper-structures of the glass metropolis,
[00:35] Shimmering reflections in the solar breeze...
[01:00] We build a future out of chrome and crystalline dreams,
[01:30] Nothing is ever as far or as dark as it seems.
[02:00] (Heavy Drums & Bassline groove)
[03:00] Skyline of tomorrow, soaring higher into the cosmic stream...
[04:00] (Outro - Echoing synthesizer tones)"""
                    ),
                    Song(
                        title = "Melodic Glass Reflection",
                        artist = "Aqua Symphony",
                        album = "Dreaming in Glass",
                        duration = 354000L,
                        uri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
                        isFavorite = false,
                        format = "AAC",
                        isDemo = true,
                        lyrics = """[00:00] (Shimmering Crystalline Keyboards)
[00:20] Mirror, mirror in the digital glass, reflect the sounds...
[00:45] A symphony of pure light, echoing in sweet refractions.
[01:15] (Orchestral Strings & Woodwinds)
[01:45] Every note is a fragment of glass, falling into place,
[02:15] Creating a gorgeous portrait of time and space.
[03:00] Reflection of the soul, shining bright through the Aero window pane...
[04:30] (Outro - Melodic piano outro fading slowly)"""
                    )
                )
                repository.insertSongs(demoSongs)
            }
        }
    }

    /**
     * Initializes the MediaPlayer and plays a selected song
     */
    fun selectAndPlaySong(song: Song) {
        _currentSong.value = song
        _currentPosition.value = 0L
        _duration.value = song.duration

        // Release previous media player
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        try {
            mediaPlayer = MediaPlayer().apply {
                // Set audio stream / source
                setDataSource(song.uri)
                prepareAsync()
                
                setOnPreparedListener { mp ->
                    mp.start()
                    _isPlaying.value = true
                    _duration.value = mp.duration.toLong()
                    startUpdatingProgress()
                    initEqualizer(mp.audioSessionId)
                }

                setOnCompletionListener {
                    handleSongCompletion()
                }

                setOnErrorListener { _, _, _ ->
                    _isPlaying.value = false
                    stopUpdatingProgress()
                    true // Recover gracefully
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _isPlaying.value = false
        }
    }

    /**
     * Toggles Play or Pause state
     */
    fun togglePlayPause() {
        val current = _currentSong.value ?: return
        val player = mediaPlayer

        if (player == null) {
            // Re-initialize and play
            selectAndPlaySong(current)
        } else {
            if (player.isPlaying) {
                player.pause()
                _isPlaying.value = false
                stopUpdatingProgress()
            } else {
                player.start()
                _isPlaying.value = true
                startUpdatingProgress()
            }
        }
    }

    /**
     * Seek to position in percentage (0f to 1f)
     */
    fun seekTo(progress: Float) {
        val player = mediaPlayer ?: return
        val seekTarget = (progress * _duration.value).toInt()
        player.seekTo(seekTarget)
        _currentPosition.value = seekTarget.toLong()
    }

    /**
     * Seek forward by 10 seconds
     */
    fun forward10Seconds() {
        val player = mediaPlayer ?: return
        val newPos = (player.currentPosition + 10000).coerceAtMost(_duration.value.toInt())
        player.seekTo(newPos)
        _currentPosition.value = newPos.toLong()
    }

    /**
     * Seek backward by 10 seconds
     */
    fun backward10Seconds() {
        val player = mediaPlayer ?: return
        val newPos = (player.currentPosition - 10000).coerceAtLeast(0)
        player.seekTo(newPos)
        _currentPosition.value = newPos.toLong()
    }

    /**
     * Skips to the next song in queue
     */
    fun playNextSong() {
        val queue = songQueue
        if (queue.isEmpty()) return

        val current = _currentSong.value
        val currentIndex = queue.indexOfFirst { it.id == current?.id }

        val nextSong = if (_shuffleEnabled.value) {
            queue.random()
        } else {
            val nextIndex = (currentIndex + 1) % queue.size
            queue[nextIndex]
        }
        selectAndPlaySong(nextSong)
    }

    /**
     * Plays previous song or restarts current if progress > 3 seconds
     */
    fun playPreviousSong() {
        val player = mediaPlayer
        if (player != null && player.currentPosition > 3000) {
            player.seekTo(0)
            _currentPosition.value = 0L
            return
        }

        val queue = songQueue
        if (queue.isEmpty()) return

        val current = _currentSong.value
        val currentIndex = queue.indexOfFirst { it.id == current?.id }

        val prevSong = if (_shuffleEnabled.value) {
            queue.random()
        } else {
            var prevIndex = currentIndex - 1
            if (prevIndex < 0) prevIndex = queue.size - 1
            queue[prevIndex]
        }
        selectAndPlaySong(prevSong)
    }

    /**
     * Toggle favorites
     */
    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            val updated = song.copy(isFavorite = !song.isFavorite)
            repository.updateSong(updated)
            if (_currentSong.value?.id == song.id) {
                _currentSong.value = updated
            }
        }
    }

    fun generateAILyricsForCurrentSong() {
        val song = _currentSong.value ?: return
        viewModelScope.launch {
            _lyricsLoadingState.value = true
            _lyricsErrorState.value = null
            try {
                val generatedLyrics = GeminiLyricsService.generateLyrics(song.title, song.artist)
                if (generatedLyrics != null) {
                    val updatedSong = song.copy(lyrics = generatedLyrics)
                    repository.updateSong(updatedSong)
                    _currentSong.value = updatedSong
                } else {
                    _lyricsErrorState.value = "Failed to generate lyrics. Please check your internet connection or API key."
                }
            } catch (e: Exception) {
                _lyricsErrorState.value = "Error: ${e.localizedMessage}"
            } finally {
                _lyricsLoadingState.value = false
            }
        }
    }

    fun updateLyrics(song: Song, newLyrics: String) {
        viewModelScope.launch {
            val updated = song.copy(lyrics = newLyrics)
            repository.updateSong(updated)
            if (_currentSong.value?.id == song.id) {
                _currentSong.value = updated
            }
        }
    }

    /**
     * Toggle shuffle mode
     */
    fun toggleShuffle() {
        _shuffleEnabled.value = !_shuffleEnabled.value
    }

    /**
     * Toggle repeat mode
     */
    fun toggleRepeat() {
        _repeatEnabled.value = !_repeatEnabled.value
    }

    /**
     * Set equalizer band level
     */
    fun setEqualizerBand(bandIndex: Int, value: Float) {
        equalizerBands[bandIndex] = value
        try {
            realEqualizer?.let { eq ->
                val minLevel = eq.bandLevelRange[0]
                val maxLevel = eq.bandLevelRange[1]
                val range = maxLevel - minLevel
                val targetLevel = (minLevel + (range * value)).toInt().toShort()
                eq.setBandLevel(bandIndex.toShort(), targetLevel)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleSongCompletion() {
        if (_repeatEnabled.value) {
            val current = _currentSong.value
            if (current != null) {
                selectAndPlaySong(current)
            }
        } else if (_autoPlayNext.value) {
            playNextSong()
        } else {
            _isPlaying.value = false
            stopUpdatingProgress()
        }
    }

    private fun startUpdatingProgress() {
        progressUpdateJob?.cancel()
        progressUpdateJob = viewModelScope.launch {
            while (_isPlaying.value) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        _currentPosition.value = mp.currentPosition.toLong()
                    }
                }
                delay(250)
            }
        }
    }

    private fun stopUpdatingProgress() {
        progressUpdateJob?.cancel()
    }

    private fun initEqualizer(audioSessionId: Int) {
        try {
            realEqualizer?.release()
            realEqualizer = Equalizer(0, audioSessionId).apply {
                enabled = true
            }
            // Apply current UI sliders levels to physical equalizer bands
            val bandsCount = realEqualizer?.numberOfBands?.toInt() ?: 5
            for (i in 0 until minOf(bandsCount, equalizerBands.size)) {
                val value = equalizerBands[i]
                val minLevel = realEqualizer?.bandLevelRange?.get(0) ?: -1500
                val maxLevel = realEqualizer?.bandLevelRange?.get(1) ?: 1500
                val range = maxLevel - minLevel
                val targetLevel = (minLevel + (range * value)).toInt().toShort()
                realEqualizer?.setBandLevel(i.toShort(), targetLevel)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            realEqualizer = null
        }
    }

    /**
     * Scans MediaStore on device to find all local MP3 and other formats and populates our db
     */
    fun scanDeviceMusic(context: Context) {
        viewModelScope.launch {
            val contentResolver: ContentResolver = context.contentResolver
            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
            )

            val cursor: Cursor? = contentResolver.query(uri, projection, selection, null, null)
            if (cursor != null) {
                val songsList = mutableListOf<Song>()
                while (cursor.moveToNext()) {
                    val titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                    val title = if (titleIndex != -1) cursor.getString(titleIndex) ?: "Unknown Track" else "Unknown Track"

                    val artistIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                    val artist = if (artistIndex != -1) cursor.getString(artistIndex) ?: "Unknown Artist" else "Unknown Artist"

                    val albumIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                    val album = if (albumIndex != -1) cursor.getString(albumIndex) ?: "Unknown Album" else "Unknown Album"

                    val durationIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                    val duration = if (durationIndex != -1) cursor.getLong(durationIndex) else 0L

                    val dataIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                    val filePath = if (dataIndex != -1) cursor.getString(dataIndex) ?: "" else ""

                    if (filePath.isEmpty()) continue

                    // Guess file format from path suffix
                    val format = when {
                        filePath.endsWith(".wav", ignoreCase = true) -> "WAV"
                        filePath.endsWith(".aac", ignoreCase = true) -> "AAC"
                        filePath.endsWith(".flac", ignoreCase = true) -> "FLAC"
                        filePath.endsWith(".ogg", ignoreCase = true) -> "OGG"
                        filePath.endsWith(".m4a", ignoreCase = true) -> "M4A"
                        else -> "MP3"
                    }

                    songsList.add(
                        Song(
                            title = title,
                            artist = if (artist == "<unknown>") "Unknown Artist" else artist,
                            album = if (album == "<unknown>") "Unknown Album" else album,
                            duration = if (duration <= 0) 240000L else duration,
                            uri = filePath,
                            isFavorite = false,
                            format = format,
                            isDemo = false
                        )
                    )
                }
                cursor.close()

                if (songsList.isNotEmpty()) {
                    // Remove demo files first or merge? Let's just insert all scanned songs
                    repository.insertSongs(songsList)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        realEqualizer?.release()
    }

    class Factory(
        private val repository: SongRepository,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
                return PlayerViewModel(repository, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
