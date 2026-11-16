package com.mattkanninen.cliphistory.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mattkanninen.cliphistory.data.ClipboardEntry
import com.mattkanninen.cliphistory.data.repository.ClipRepository
import com.mattkanninen.cliphistory.service.OverlayWindowService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    val isSearchActive: Boolean = false,
    val bubblesEnabled: Boolean = true,
    val isLoading: Boolean = false
)

@HiltViewModel
class ClipHistoryViewModel @Inject constructor(
    private val repository: ClipRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val prefs = appContext.getSharedPreferences(
        OverlayWindowService.PREFS_NAME, Context.MODE_PRIVATE
    )

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow("All")
    private val _isSearchActive = MutableStateFlow(false)
    private val _bubblesEnabled = MutableStateFlow(
        prefs.getBoolean(OverlayWindowService.KEY_BUBBLES_ENABLED, false)
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _clips = combine(_searchQuery, _isSearchActive) { query, active ->
        query to active
    }.flatMapLatest { (query, active) ->
        if (active && query.isNotBlank()) {
            repository.search(query)
        } else {
            repository.observeAll()
        }
    }

    val uiState: StateFlow<ClipHistoryUiState> = combine(
        _clips,
        _searchQuery,
        _selectedCategory,
        _isSearchActive,
        _bubblesEnabled
    ) { clips: List<ClipboardEntry>, query: String, category: String, searchActive: Boolean, bubblesOn: Boolean ->
        val filtered = if (category == "All") {
            clips
        } else {
            clips.filter { entry ->
                when (category) {
                //TODO less magic strings
                    "URL" -> entry.content.startsWith("http://") || entry.content.startsWith("https://")
                    "Code" -> entry.content.contains("{") || entry.content.contains("val ") || entry.content.contains("fun ")
                    "Pinned" -> entry.isPinned
                    "Unpinned" -> !entry.isPinned
                    else -> true
                }
            }
        }
        ClipHistoryUiState(
            clips = filtered,
            searchQuery = query,
            selectedCategory = category,
            isSearchActive = searchActive,
            bubblesEnabled = bubblesOn
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ClipHistoryUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onSearchActivated() {
        _isSearchActive.value = true
        _selectedCategory.value = "All"
    }

    fun onSearchDeactivated() {
        _isSearchActive.value = false
        _searchQuery.value = ""
        _selectedCategory.value = "All"
    }

    fun onToggleBubbles() {
        val next = !_bubblesEnabled.value
        prefs.edit().putBoolean(OverlayWindowService.KEY_BUBBLES_ENABLED, next).apply()
        _bubblesEnabled.value = next
    }

    fun onAddClip(content: String) {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.saveClip(trimmed)
        }
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    fun onResumeClipboard(content: String?) {
        val trimmed = content?.trim().orEmpty()
        if (trimmed.isEmpty()) return
        if (uiState.value.clips.firstOrNull()?.content == trimmed) return
        viewModelScope.launch {
            repository.saveClip(trimmed)
        }
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
