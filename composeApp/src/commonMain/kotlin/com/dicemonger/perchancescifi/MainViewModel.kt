package com.dicemonger.perchancescifi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicemonger.perchancescifi.io.EntriesLoader
import com.dicemonger.perchancescifi.io.FileSystemProvider
import com.dicemonger.perchancescifi.io.TemplatesLoader
import com.dicemonger.perchancescifi.model.Entry
import com.dicemonger.perchancescifi.model.Tag
import com.dicemonger.perchancescifi.model.TagsCreator
import com.dicemonger.perchancescifi.model.Template
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import okio.Path
import okio.Path.Companion.toPath
import org.koin.android.annotation.KoinViewModel
import kotlin.random.Random

@KoinViewModel
class MainViewModel() : ViewModel() {
    private val _allTags: MutableStateFlow<List<Tag>> = MutableStateFlow(emptyList())
    val allTags = _allTags.asStateFlow()

    private val _allEntries: MutableStateFlow<List<Entry>> = MutableStateFlow(emptyList())
    val allEntries = _allEntries.asStateFlow()

    private val _templates: MutableStateFlow<List<Template>> = MutableStateFlow(emptyList())
    val templates = _templates.asStateFlow()

    private val _toast: MutableStateFlow<String?> = MutableStateFlow(null)
    val toast: StateFlow<String?> = _toast.asStateFlow()

    private val _currentEntryId: MutableStateFlow<Long> = MutableStateFlow(0L)

    val currentEntry: StateFlow<Entry?> =
        combine(_allEntries, _currentEntryId) { entries, id ->
            entries.firstOrNull { it.id == id }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    private val _currentTemplateId: MutableStateFlow<String?> = MutableStateFlow(null)

    val currentTemplate: StateFlow<Template?> =
        combine(_templates, _currentTemplateId) { templates, id ->
            templates.firstOrNull { it.title == id }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val baseDir: Path = "E:/ProjectBuilds/PerchanceSciFiEntryApp".toPath()
    private val entriesPath: Path = baseDir / "entries.txt"
    private val templatesPath: Path = baseDir / "templates.txt"

    init {
        _isLoading.value = true

        _allTags.value = TagsCreator().CreateTags()

        val fileSystem = FileSystemProvider.system

        fileSystem.createDirectories(baseDir)

        check(fileSystem.exists(entriesPath)) {
            "Missing entries file: $entriesPath (place your default entries.txt there)"
        }

        _allEntries.value = EntriesLoader().readFile(
            path = entriesPath,
            allTags = _allTags.value
        )
        _currentEntryId.value = _allEntries.value.firstOrNull()?.id ?: 0L

        _templates.value = TemplatesLoader().readFile(templatesPath, _allTags.value)
        _currentTemplateId.value = _templates.value.firstOrNull()?.title

        _isLoading.value = false
    }

    private fun nextEntryId(): Long {
        return (_allEntries.value.maxByOrNull { it.id }?.id ?: 0L) + 1
    }

    fun changeEntry(entry: Entry) {
        _currentEntryId.value = entry.id
    }

    fun changeTitle(title: String) {
        updateCurrentEntry { it.copy(title = title) }
    }

    fun changeDescription(desc: String) {
        updateCurrentEntry { it.copy(desc = desc) }
    }

    fun updateWithTags(tags: List<Tag>) {
        val sortedTags = tags.sortedBy { it.title }.sortedBy { it.parentId != null }
        updateCurrentEntry { it.copy(tags = sortedTags) }
    }

    private fun updateCurrentEntry(transform: (Entry) -> Entry) {
        val id = _currentEntryId.value ?: return
        _allEntries.update { entries ->
            val index = entries.indexOfFirst { it.id == id }
            if (index < 0) return@update entries
            entries.toMutableList().apply { this[index] = transform(this[index]) }
        }
    }

    fun addEntry() {
        val entry = Entry(
            id = nextEntryId(),
            title = "",
            desc = "",
            tags = emptyList()
        )
        _allEntries.update { it + entry }
        _currentEntryId.value = entry.id
    }

    fun removeCurrentEntry() {
        val id = _currentEntryId.value ?: return
        val old = _allEntries.value
        val idx = old.indexOfFirst { it.id == id }
        if (idx < 0) return

        val newList = old.toMutableList().apply { removeAt(idx) }
        _allEntries.value = newList

        _currentEntryId.value = when {
            newList.isEmpty() -> 0L
            idx < newList.size -> newList[idx].id
            else -> newList.last().id
        }
    }

    fun applyTemplate() {
        val currentTemplate = currentTemplate.value ?: return
        val newEntry = currentTemplate.entry.copy(id = nextEntryId())
        _allEntries.update { it + newEntry }
        _currentEntryId.value = newEntry.id
    }

    fun changeTemplate(template: Template) {
        _currentTemplateId.value = template.title
    }

    fun saveEntryAsTemplate(title: String) {
        val entry = currentEntry.value ?: return
        _templates.update { it + Template(entry = entry, title = title) }
        _currentTemplateId.value = title
        TemplatesLoader().writeFile(path = templatesPath, templates = _templates.value)
    }

    fun deleteTemplate() {
        if(_templates.value.size <= 1) return

        val currentTemplate = currentTemplate.value ?: return
        _templates.update { it - currentTemplate }
        _currentTemplateId.value = _templates.value.firstOrNull()?.title
        TemplatesLoader().writeFile(path = templatesPath, templates = _templates.value)
    }

    fun saveAllEntries() {
        _isLoading.value = true

        EntriesLoader().writeFile(
            path = entriesPath,
            entries = _allEntries.value
        )

        _toast.value = "Saved!"
        _isLoading.value = false
    }

    fun clearToast() {
        _toast.value = null
    }
}