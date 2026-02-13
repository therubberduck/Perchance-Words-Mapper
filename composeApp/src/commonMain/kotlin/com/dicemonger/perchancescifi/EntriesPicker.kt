package com.dicemonger.perchancescifi

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dicemonger.perchancescifi.model.Entry
import com.dicemonger.perchancescifi.model.Tag

object EntriesPicker {

    private enum class SearchMode(val title: String) {
        Title("by title"), Any("by title or desc"), Tags("by tags"), BrokenTags(
            "Broken Tags"
        )
    }

    @Composable
    fun Create(
        entries: State<List<Entry>>,
        pickEntry: (Entry) -> Unit,
        testTag: (Tag) -> Boolean,
        actionHide: () -> Unit
    ) {
        Column {
            var searchText by remember { mutableStateOf("") }
            var searchMode by remember { mutableStateOf(SearchMode.Title) }
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.Center
            ) {
                val focusRequester = remember { FocusRequester() }
                LaunchedEffect(Unit) { focusRequester.requestFocus() }
                TextField(
                    modifier = Modifier.focusRequester(focusRequester),
                    value = searchText,
                    enabled = searchMode != SearchMode.BrokenTags,
                    onValueChange = { searchText = it },
                    label = { Text("Search") })
                SearchMode.entries.forEach { mode ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.RadioButton(
                            selected = searchMode == mode,
                            onClick = { searchMode = mode }
                        )
                        Text(mode.title)
                    }

                }
                Button(onClick = actionHide) {
                    Text("X")
                }
            }

            val filteredEntries = entries.value.filter {
                when (searchMode) {
                    SearchMode.Title -> it.title.contains(searchText, ignoreCase = true)
                    SearchMode.Any -> it.title.contains(searchText, ignoreCase = true) || it.desc.contains(
                        searchText,
                        ignoreCase = true
                    )

                    SearchMode.Tags -> it.tags.any { tag ->
                        tag.title.contains(
                            searchText,
                            ignoreCase = true
                        ) || tag.id.contains(searchText, ignoreCase = true)
                    }

                    SearchMode.BrokenTags -> it.tags.any { tag -> testTag(tag).not() }
                }
            }
            LazyColumn {
                items(items = filteredEntries) { item ->
                    Column(
                        modifier = Modifier
                            .padding(4.dp)
                            .fillMaxWidth()
                            .clickable {
                                pickEntry(item)
                                actionHide()
                            }
                            .border(1.dp, Color.Black, RoundedCornerShape(4f))
                            .padding(4.dp)
                    ) {
                        Text(item.title)
                        Text(item.desc)
                        FlowRow {
                            item.tags.forEachIndexed { index, tag ->
                                if (index > 0) Text(", ")
                                Text(tag.title)
                            }
                        }
                    }
                }
            }

        }
    }
}