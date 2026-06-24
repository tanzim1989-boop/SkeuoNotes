package com.example.data

import kotlinx.coroutines.flow.Flow

class SongRepository(private val songDao: SongDao) {
    val allSongs: Flow<List<Song>> = songDao.getAllSongs()
    val favoriteSongs: Flow<List<Song>> = songDao.getFavoriteSongs()

    suspend fun insertSong(song: Song) = songDao.insertSong(song)
    suspend fun insertSongs(songs: List<Song>) = songDao.insertSongs(songs)
    suspend fun updateSong(song: Song) = songDao.updateSong(song)
    suspend fun deleteSong(song: Song) = songDao.deleteSong(song)
    suspend fun getSongCount(): Int = songDao.getSongCount()
}
