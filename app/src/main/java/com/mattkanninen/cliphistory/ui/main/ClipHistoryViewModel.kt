package com.mattkanninen.cliphistory.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mattkanninen.cliphistory.data.ClipboardEntry
import com.mattkanninen.cliphistory.data.repository.ClipRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClipHistoryUiState(
    val clips: List<ClipboardEntry> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val isLoading: Boolean = false
)

@HiltViewModel
class ClipHistoryViewModel @Inject constructor(
    private val repository: ClipRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow("All")

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _clips = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            repository.observeAll()
        } else {
            repository.search(query)
        }
    }

    val uiState: StateFlow<ClipHistoryUiState> = combine(
        _clips,
        _searchQuery,
        _selectedCategory
    ) { clips: List<ClipboardEntry>, query: String, category: String ->
        val filtered = if (category == "All") {
            clips
        } else {
            clips.filter { entry ->
                when (category) {
                    "URL" -> entry.content.startsWith("http://") || entry.content.startsWith("https://")
                    "Code" -> entry.content.contains("{") || entry.content.contains("val ") || entry.content.contains("fun ")
                    else -> true
                }
            }
        }
        ClipHistoryUiState(
            clips = filtered,
            searchQuery = query,
            selectedCategory = category
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ClipHistoryUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    fun onTogglePin(clip: ClipboardEntry) {
        viewModelScope.launch {
            repository.togglePin(clip.id, clip.isPinned)
        }
    }

    fun onDeleteClip(clip: ClipboardEntry) {
        viewModelScope.launch {
            repository.deleteById(clip.id)
        }
    }

    fun onClearHistory() {
        viewModelScope.launch {
            repository.clearUnpinned()
        }
    }
}
