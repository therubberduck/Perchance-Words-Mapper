package com.dicemonger.perchancescifi

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dicemonger.perchancescifi.model.Entry
import com.dicemonger.perchancescifi.model.Tag
import org.koin.compose.viewmodel.koinViewModel

object MainScreen {

    @Composable
    fun Create() {
        val viewModel: MainViewModel = koinViewModel()

        CreateInstance(
            templateName = viewModel.templateName.collectAsState(),
            currentEntry = viewModel.currentEntry.collectAsState(),
            allEntries = viewModel.allEntries.collectAsState(),
            allTags = viewModel.allTags.collectAsState(),
            isLoading = viewModel.isLoading.collectAsState(),
            actionChangeTitle = viewModel::changeTitle,
            actionChangeDescription = viewModel::changeDescription,
            actionUpdateWithTags = viewModel::updateWithTags,
            actionChangeEntry = viewModel::changeEntry,
            actionSaveAllEntries = viewModel::saveAllEntries
        )
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun CreateInstance(
        templateName: State<String>,
        currentEntry: State<Entry?>,
        allEntries: State<List<Entry>>,
        allTags: State<List<Tag>>,
        isLoading: State<Boolean>,
        actionChangeTitle: (String) -> Unit,
        actionChangeDescription: (String) -> Unit,
        actionUpdateWithTags: (List<Tag>) -> Unit,
        actionChangeEntry: (Entry) -> Unit,
        actionSaveAllEntries: () -> Unit
    ) {
        var showTemplatesPicker by remember { mutableStateOf(false) }
        var showTagsPicker by remember { mutableStateOf(false) }
        var showAllEntries by remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxSize()) {
            if (currentEntry.value != null && isLoading.value.not() && showTemplatesPicker.not() && showTagsPicker.not() && showAllEntries.not()) {
                EditView(
                    templateName = templateName,
                    currentEntry = currentEntry,
                    actionChangeTitle = actionChangeTitle,
                    actionChangeDescription = actionChangeDescription,
                    actionShowChangeTemplate = { showTemplatesPicker = true },
                    actionShowTagsPicker = { showTagsPicker = true },
                    actionShowAllEntries = { showAllEntries = true },
                    actionSaveAllEntries = actionSaveAllEntries
                )
            }
            if (showTemplatesPicker) {
                TemplatesPicker(actionHide = { showTemplatesPicker = false })
            }
            if (showTagsPicker) {
                TagsPicker(
                    entry = currentEntry,
                    tags = allTags,
                    actionUpdateWithTags = actionUpdateWithTags,
                    actionHide = { showTagsPicker = false })
            }
            if (showAllEntries) {
                AllEntriesPicker(allEntries, pickEntry = actionChangeEntry, actionHide = { showAllEntries = false })
            }
            if (currentEntry.value == null || isLoading.value) {
                LoadingIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }

    @Composable
    private fun EditView(
        templateName: State<String>,
        currentEntry: State<Entry?>,
        actionChangeTitle: (String) -> Unit,
        actionChangeDescription: (String) -> Unit,
        actionShowChangeTemplate: () -> Unit,
        actionShowTagsPicker: () -> Unit,
        actionShowAllEntries: () -> Unit,
        actionSaveAllEntries: () -> Unit
    ) {
        val currentEntry = currentEntry.value ?: return
        Column(modifier = Modifier.fillMaxSize()) {
            TemplatePicker(templateName, actionShowChangeTemplate)
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = currentEntry.title,
                onValueChange = actionChangeTitle,
                singleLine = true
            )
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = currentEntry.desc,
                onValueChange = actionChangeDescription,
                minLines = 3
            )
            FlowRow(modifier = Modifier.clickable { actionShowTagsPicker() }) {
                currentEntry.tags.forEachIndexed { index, tag ->
                    if (index > 0) Text(", ")
                    Text(tag.title)
                }
            }
            Row {
                Button(onClick = actionShowAllEntries) {
                    Text("All Entries")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = actionSaveAllEntries) {
                    Text("Save All")
                }
            }
        }
    }

    @Composable
    private fun TemplatePicker(templateName: State<String>, actionChangeTemplate: () -> Unit) {
        Button(onClick = actionChangeTemplate) {
            Text(templateName.value)
        }
    }

    @Composable
    private fun TagsPicker(
        entry: State<Entry?>,
        tags: State<List<Tag>>,
        actionUpdateWithTags: (List<Tag>) -> Unit,
        actionHide: () -> Unit
    ) {
        val entryTags = entry.value?.tags ?: emptyList()
        val mainTags = tags.value.filter { it.parentId == null }
        var activeTab = remember { mutableStateOf(mainTags.firstOrNull()?.id) }
        var tabTags =
            tags.value.filter { it.parentId == activeTab.value }.sortedBy { it.title }.sortedBy { it.priority }
        Column {
            Button(modifier = Modifier.align(Alignment.End), onClick = actionHide) {
                Text("X")
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                mainTags.forEach { tag ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            modifier = Modifier.clickable { activeTab.value = tag.id },
                            color = if (activeTab.value == tag.id) Color.Red else Color.Black,
                            style = if (activeTab.value == tag.id) TextStyle(fontWeight = FontWeight.Bold) else TextStyle(
                                fontWeight = FontWeight.SemiBold
                            ),
                            text = tag.title
                        )
                        Checkbox(
                            checked = entryTags.contains(tag),
                            enabled = tag.id != "world",
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    actionUpdateWithTags(entryTags + tag)
                                } else {
                                    actionUpdateWithTags(entryTags - tag)
                                }
                            }
                        )
                    }
                }
            }
            FlowRow(modifier = Modifier.fillMaxWidth()) {
                var lastPrio: Int = tabTags.firstOrNull()?.priority ?: 0
                tabTags.forEach { tag ->
                    if (lastPrio != tag.priority) {
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            style = TextStyle(fontWeight = FontWeight.Black),
                            text = "|"
                        )
                    }

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
                    lastPrio = tag.priority
                }
            }
        }
    }

    @Composable
    private fun TemplatesPicker(actionHide: () -> Unit) {

    }

    @Composable
    private fun AllEntriesPicker(entries: State<List<Entry>>, pickEntry: (Entry) -> Unit, actionHide: () -> Unit) {
        Column {
            Button(modifier = Modifier.align(Alignment.End), onClick = actionHide) {
                Text("X")
            }
            LazyColumn {
                items(items = entries.value) { item ->
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