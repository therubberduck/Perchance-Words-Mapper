package com.dicemonger.perchancescifi.io

import com.dicemonger.perchancescifi.model.Entry
import com.dicemonger.perchancescifi.model.Tag
import okio.FileSystem
import okio.Path

class EntriesLoader(private val fileSystem: FileSystem = FileSystemProvider.system) {
    fun writeFile(path: Path, entries: List<Entry>) {
        fileSystem.write(path) {
            for (entry in entries) {
                writeUtf8("  ${entry.title}\n")
                writeUtf8("    orig=${entry.desc}\n")
                val tagIds = entry.tags.joinToString(",") { it.id }
                writeUtf8("    tags=$tagIds\n")
            }
        }
    }

    fun readFile(path: Path, allTags: List<Tag>): List<Entry> {
        val fileString = fileSystem.read(path) { readUtf8() }
        val lines = fileString.lines().filter { it.isNotBlank() }
        val entries = mutableListOf<Entry>()

        var currentId = 0L
        var currentTitle: String? = null
        var currentDesc: String? = null


        for (line in lines) {
            when {
                line.startsWith("  ") && !line.startsWith("    ") -> {
                    // Title line (exactly two spaces)
                    currentTitle = line.trimStart()
                }

                line.startsWith("    orig=") -> {
                    // Description line
                    currentDesc = line.trimStart().removePrefix("orig=")
                }

                line.startsWith("    tags=") -> {
                    // Tags line - complete the entry
                    val tagsLine = line.trimStart().removePrefix("tags=")
                    val tagIds = tagsLine.split(",").map { it.trim() }
                    val tags = allTags.filter { tag -> tagIds.contains(tag.id) }

                    if (currentTitle != null && currentDesc != null) {
                        entries.add(Entry(currentId, currentTitle, currentDesc, tags))
                    }

                    // Reset for next entry
                    currentTitle = null
                    currentDesc = null
                    currentId++
                }
            }
        }

        return entries
    }
}

