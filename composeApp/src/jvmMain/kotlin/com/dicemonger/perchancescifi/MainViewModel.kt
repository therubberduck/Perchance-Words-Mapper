package com.dicemonger.perchancescifi

import androidx.compose.material.Checkbox
import androidx.lifecycle.ViewModel
import com.dicemonger.perchancescifi.model.Entry
import com.dicemonger.perchancescifi.model.Tag
import com.dicemonger.perchancescifi.model.TagsCreator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.android.annotation.KoinViewModel
import kotlin.math.E

@KoinViewModel
class MainViewModel() : ViewModel() {
    private val _templateName: MutableStateFlow<String> = MutableStateFlow("")
    val templateName = _templateName.asStateFlow()

    private val _allTags: MutableStateFlow<List<Tag>> = MutableStateFlow(emptyList())
    val allTags = _allTags.asStateFlow()

    private val _allEntries: MutableStateFlow<List<Entry>> = MutableStateFlow(emptyList())
    val allEntries = _allEntries.asStateFlow()

    private val _currentEntry: MutableStateFlow<Entry> = MutableStateFlow(Entry("", "", emptyList()))
    val currentEntry = _currentEntry.asStateFlow()

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        _isLoading.value = true

        _allTags.value = TagsCreator().CreateTags()

        _allEntries.value = listOf(
            Entry(
                title = "Sample Entry",
                desc = "This is a sample entry description.",
                tags = listOf(
                    _allTags.value[0],
                    _allTags.value[1]
                )
            ),
            Entry(
                title = "Another Sample Entry",
                desc = "This is another sample entry description.",
                tags = listOf(
                    _allTags.value[0],
                    _allTags.value[2]
                )
            )
        )

        _currentEntry.value = _allEntries.value[0]
        _isLoading.value = false
    }

    fun changeTitle(title: String) {
        _currentEntry.value = _currentEntry.value.copy(title = title)
    }

    fun changeDescription(desc: String) {
        _currentEntry.value = _currentEntry.value.copy(desc = desc)
    }

    fun updateWithTags(tags: List<Tag>) {
        _currentEntry.value = _currentEntry.value.copy(tags = tags)
    }

    fun changeEntry(entry: Entry) {
        _currentEntry.value = entry
    }
}