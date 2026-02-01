package com.dicemonger.perchancescifi.io

import com.dicemonger.perchancescifi.model.Entry
import com.dicemonger.perchancescifi.model.Tag
import okio.FileSystem
import okio.Path

class EntriesLoader(private val fileSystem: FileSystem = FileSystemProvider.system) {
    fun writeFile(path: Path, entries: List<Entry>) {
        fileSystem.write(path) {
            for (entry in entries) {
                if (entry.title.isBlank()) continue
                writeUtf8("  ${entry.title}\n")
                writeUtf8("    orig=${entry.desc.replace("\n", "<br>")}\n")
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
                    currentDesc = line.trimStart().removePrefix("orig=").replace("<br>", "\n")
                }

                line.startsWith("    tags=") -> {
                    // Tags line - complete the entry
                    val tagsLine = line.trimStart().removePrefix("tags=")
                    val tagIds = tagsLine.split(",").map { it.trim() }
                    val reviewedTags = handleTags(allTags, tagIds)

                    if (currentTitle != null && currentDesc != null) {
                        entries.add(Entry(currentId, currentTitle, currentDesc, reviewedTags))
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

    private fun handleTags(allTags: List<Tag>, tagIds: List<String>): List<Tag> {
        val knownTags = allTags.filter { tag -> tagIds.contains(tag.id) }
        val combinedTags = knownTags + tagIds.filter { !knownTags.map { it.id }.contains(it) }
            .map { id -> Tag(id.capitalize(), id, "unknown") }

        val updatedTags = combinedTags.map { tag ->
            when (tag.id) {
                "religion" -> allTags.find { it.id == "mythic" } ?: Tag(title = "Mythic", id = "mythic", parentId = "null")
                "rperson" -> allTags.find { it.id == "mperson" } ?: Tag(title = "Mythic Person", id = "mperson", parentId = "misc")
                "rplace" -> allTags.find { it.id == "mplace" } ?: Tag(title = "Mythic Place", id = "mplace", parentId = "misc")
                else -> tag
            }
        }

        return updatedTags
    }
}

