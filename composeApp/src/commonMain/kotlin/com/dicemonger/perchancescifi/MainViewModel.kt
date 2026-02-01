package com.dicemonger.perchancescifi

import androidx.lifecycle.ViewModel
import com.dicemonger.perchancescifi.io.EntriesLoader
import com.dicemonger.perchancescifi.io.FileSystemProvider
import com.dicemonger.perchancescifi.model.Entry
import com.dicemonger.perchancescifi.model.Tag
import com.dicemonger.perchancescifi.model.TagsCreator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okio.Path
import okio.Path.Companion.toPath
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class MainViewModel() : ViewModel() {
    private val _templateName: MutableStateFlow<String> = MutableStateFlow("")
    val templateName = _templateName.asStateFlow()

    private val _allTags: MutableStateFlow<List<Tag>> = MutableStateFlow(emptyList())
    val allTags = _allTags.asStateFlow()

    private val _allEntries: MutableStateFlow<List<Entry>> = MutableStateFlow(emptyList())
    val allEntries = _allEntries.asStateFlow()

    private val _currentEntry: MutableStateFlow<Entry> = MutableStateFlow(Entry("", "", "", emptyList()))
    val currentEntry = _currentEntry.asStateFlow()

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        _isLoading.value = true

        _allTags.value = TagsCreator().CreateTags()

        val fileSystem = FileSystemProvider.system

        val baseDir: Path = "D:/PerchanceSciFiEntryApp".toPath()
        val entriesPath: Path = baseDir / "entries.txt"

        fileSystem.createDirectories(baseDir)

        check(fileSystem.exists(entriesPath)) {
            "Missing entries file: $entriesPath (place your default entries.txt there)"
        }

        _allEntries.value = EntriesLoader().readFile(
            path = entriesPath,
            allTags = _allTags.value
        )

        _currentEntry.value = _allEntries.value.first()
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

    fun saveAllEntries() {
        _isLoading.value = true
        
        val baseDir: Path = "D:/PerchanceSciFiEntryApp".toPath()
        val entriesPath: Path = baseDir / "entries.txt"

        EntriesLoader().writeFile(
            path = entriesPath,
            entries = _allEntries.value
        )

        _isLoading.value = false
    }
}