package com.dicemonger.perchancescifi

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dicemonger.perchancescifi.model.Entry
import com.dicemonger.perchancescifi.model.Tag

object TagsPicker {

    enum class Category {
        World, Region, Country, Culture, Religion, Word, Fiction, Misc
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun Create(
        entry: State<Entry?>,
        tags: State<List<Tag>>,
        actionUpdateWithTags: (List<Tag>) -> Unit
    ) {
        val entryTags = entry.value?.tags ?: emptyList()
        val mainTags = tags.value.filter { it.parentId == null }

        val isMiscTag: (Tag) -> Boolean = { tag ->
            tag.parentId != null && tag.parentId != "world" && tag.parentId != "region" && tag.parentId != "place" && tag.parentId != "culture" && tag.parentId != "amindian" && tag.parentId != "religion" && tag.parentId != "word" && tag.parentId != "fictional"
        }
        val miscTagsFromAll = tags.value.filter(isMiscTag)
        val miscTagsFromEntry = entryTags.filter(isMiscTag).filterNot(miscTagsFromAll::contains)

        var activeTab by remember { mutableStateOf(Category.World) }
        Column {
            FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                mainTags.forEach { tag ->
                    val isChecked = entryTags.contains(tag)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            modifier = Modifier.clickable {
                                if (isChecked) {
                                    actionUpdateWithTags(entryTags - tag)
                                } else {
                                    actionUpdateWithTags(entryTags + tag)
                                }
                            },
                            color = if (isChecked) Color.Red else Color.Black,
                            style = if (isChecked) {
                                TextStyle(fontWeight = FontWeight.Bold)
                            } else {
                                TextStyle(fontWeight = FontWeight.SemiBold)
                            },
                            text = tag.title
                        )
                    }
                }
            }
            FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Category.entries.forEach { category ->
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = if (activeTab == category) Color.Black else Color.LightGray),
                        onClick = { activeTab = category }
                    ) {
                        val text = if (category == Category.Misc && miscTagsFromEntry.isNotEmpty()) {
                            category.name + " (!)"
                        } else {
                            category.name
                        }
                        Text(
                            color = if (activeTab == category) Color.White else Color.Black,
                            text = text
                        )
                    }
                }
            }
            when (activeTab) {
                Category.World -> TagsSection(
                    tags.value.filter { it.parentId == "world" },
                    entryTags,
                    actionUpdateWithTags
                )

                Category.Region -> TagsSection(
                    tags.value.filter { it.parentId == "region" },
                    entryTags,
                    actionUpdateWithTags
                )

                Category.Country -> TagsSection(
                    tags.value.filter { it.parentId == "place" },
                    entryTags,
                    actionUpdateWithTags
                )

                Category.Culture -> TagsSection(
                    tags.value.filter { it.parentId == "culture" || it.parentId == "amindian" },
                    entryTags,
                    actionUpdateWithTags
                )

                Category.Religion -> TagsSection(
                    tags.value.filter { it.parentId == "religion" },
                    entryTags,
                    actionUpdateWithTags
                )

                Category.Word -> TagsSection(
                    tags.value.filter { it.parentId == "word" },
                    entryTags,
                    actionUpdateWithTags
                )

                Category.Fiction -> TagsSection(
                    tags.value.filter { it.parentId == "fictional" },
                    entryTags,
                    actionUpdateWithTags
                )

                Category.Misc -> {
                    if (miscTagsFromEntry.isNotEmpty()) {
                        TagsSection(miscTagsFromEntry, entryTags, actionUpdateWithTags)
                        Spacer(
                            modifier = Modifier.padding(4.dp).height(2.dp).fillMaxWidth()
                                .background(Color.LightGray)
                        )
                    }
                    TagsSection(
                        miscTagsFromAll,
                        entryTags,
                        actionUpdateWithTags
                    )
                }
            }
        }
    }

    @Composable
    private fun TagsSection(sectionTags: List<Tag>, entryTags: List<Tag>, actionUpdateWithTags: (List<Tag>) -> Unit) {
        val tagsByPriority = sectionTags.groupBy { it.priority }.toSortedMap()

        Column(modifier = Modifier.fillMaxWidth().wrapContentHeight().verticalScroll(rememberScrollState())) {
            var notFirst = false
            tagsByPriority.forEach { (_, tagsInPriority) ->
                if(notFirst) {
                    Spacer(modifier = Modifier.padding(4.dp).height(2.dp).fillMaxWidth().background(Color.LightGray))
                }
                notFirst = true
                FlowRow {
                    tagsInPriority.sortedBy { it.title }.forEach { tag ->
                        val isActive = entryTags.contains(tag)
                        Text(
                            modifier = Modifier
                                .padding(2.dp)
                                .clickable {
                                    if (isActive)
                                        actionUpdateWithTags(entryTags - tag)
                                    else
                                        actionUpdateWithTags(entryTags + tag)
                                }
                                .border(1.dp, Color.Black, RoundedCornerShape(9000.dp))
                                .padding(vertical = 2.dp, horizontal = 6.dp),
                            color = if (isActive) Color.Red else Color.Black,
                            style = if (isActive) TextStyle(fontWeight = FontWeight.SemiBold) else TextStyle(fontWeight = FontWeight.Normal),
                            text = tag.title
                        )
                    }
                }
            }
        }       
    }
}