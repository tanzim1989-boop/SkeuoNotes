package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Note
import com.example.data.NoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val notes: StateFlow<List<Note>> = repository.allNotes
        .combine(_searchQuery) { notesList, query ->
            if (query.isBlank()) {
                notesList
            } else {
                notesList.filter {
                    it.title.contains(query, ignoreCase = true) ||
                    it.content.contains(query, ignoreCase = true)
                }
            }
        }
        .combine(_selectedCategory) { notesList, cat ->
            if (cat == "All") {
                notesList
            } else {
                notesList.filter { it.category.equals(cat, ignoreCase = true) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(cat: String) {
        _selectedCategory.value = cat
    }

    fun saveNote(id: Int, title: String, content: String, colorHex: Long, isPinned: Boolean, fontType: String, category: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            if (id == 0) {
                // New Note
                val newNote = Note(
                    title = title,
                    content = content,
                    colorHex = colorHex,
                    isPinned = isPinned,
                    fontType = fontType,
                    category = category
                )
                repository.insertNote(newNote)
            } else {
                // Existing Note
                val existingNote = Note(
                    id = id,
                    title = title,
                    content = content,
                    colorHex = colorHex,
                    isPinned = isPinned,
                    fontType = fontType,
                    category = category
                )
                repository.updateNote(existingNote)
            }
            onComplete()
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isPinned = !note.isPinned))
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    class Factory(private val repository: NoteRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NoteViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
