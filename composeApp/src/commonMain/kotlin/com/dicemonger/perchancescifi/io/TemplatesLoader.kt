package com.dicemonger.perchancescifi.io

import com.dicemonger.perchancescifi.model.Entry
import com.dicemonger.perchancescifi.model.Tag
import com.dicemonger.perchancescifi.model.Template
import okio.FileSystem
import okio.Path

class TemplatesLoader(private val fileSystem: FileSystem = FileSystemProvider.system) {
    fun writeFile(path: Path, templates: List<Template>) {
        fileSystem.write(path) {
            for (template in templates) {
                writeUtf8("  ${template.title}\n")
                writeUtf8("    orig=${template.entry.desc}\n")
                val tagIds = template.entry.tags.joinToString(",") { it.id }
                writeUtf8("    tags=$tagIds\n")
            }
        }
    }

    fun readFile(path: Path, allTags: List<Tag>): List<Template> {
        val fileString = fileSystem.read(path) { readUtf8() }
        val lines = fileString.lines().filter { it.isNotBlank() }
        val templates = mutableListOf<Template>()

        var currentTitle: String? = null
        var currentDesc: String? = null

        for (line in lines) {
            when {
                line.startsWith("  ") && !line.startsWith("    ") -> {
                    // Template title line (exactly two spaces)
                    currentTitle = line.trimStart()
                }

                line.startsWith("    orig=") -> {
                    // Description line
                    currentDesc = line.trimStart().removePrefix("orig=")
                }

                line.startsWith("    tags=") -> {
                    // Tags line - complete the template
                    val tagsLine = line.trimStart().removePrefix("tags=")
                    val tagIds = tagsLine.split(",").map { it.trim() }
                    val knownTags = allTags.filter { tag -> tagIds.contains(tag.id) }
                    val allTags = knownTags + tagIds.filter { !knownTags.map { it.id }.contains(it) }
                        .map { id -> Tag(id.capitalize(), id, "unknown") }

                    if (currentTitle != null && currentDesc != null) {
                        val entry = Entry(0L, "", currentDesc, allTags)
                        templates.add(Template(currentTitle, entry))
                    }

                    // Reset for next template
                    currentTitle = null
                    currentDesc = null
                }
            }
        }

        return templates
    }
}