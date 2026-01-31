package com.dicemonger.perchancescifi

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
            actionChangeEntry = viewModel::changeEntry
        )
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun CreateInstance(
        templateName: State<String>,
        currentEntry: State<Entry>,
        allEntries: State<List<Entry>>,
        allTags: State<List<Tag>>,
        isLoading: State<Boolean>,
        actionChangeTitle: (String) -> Unit,
        actionChangeDescription: (String) -> Unit,
        actionUpdateWithTags: (List<Tag>) -> Unit,
        actionChangeEntry: (Entry) -> Unit
    ) {
        var showTemplatesPicker by remember { mutableStateOf(false) }
        var showTagsPicker by remember { mutableStateOf(false) }
        var showAllEntries by remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading.value.not() && showTemplatesPicker.not() && showTagsPicker.not() && showAllEntries.not()) {
                EditView(
                    templateName = templateName,
                    currentEntry = currentEntry,
                    actionChangeTitle = actionChangeTitle,
                    actionChangeDescription = actionChangeDescription,
                    actionShowChangeTemplate = { showTemplatesPicker = true },
                    actionShowTagsPicker = { showTagsPicker = true },
                    actionShowAllEntries = { showAllEntries = true }
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
            if(isLoading.value) {
                LoadingIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }

    @Composable
    private fun EditView(
        templateName: State<String>,
        currentEntry: State<Entry>,
        actionChangeTitle: (String) -> Unit,
        actionChangeDescription: (String) -> Unit,
        actionShowChangeTemplate: () -> Unit,
        actionShowTagsPicker: () -> Unit,
        actionShowAllEntries: () -> Unit
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TemplatePicker(templateName, actionShowChangeTemplate)
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = currentEntry.value.title,
                onValueChange = actionChangeTitle,
                singleLine = true
            )
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = currentEntry.value.desc,
                onValueChange = actionChangeDescription,
                minLines = 3
            )
            FlowRow(modifier = Modifier.clickable { actionShowTagsPicker() }) {
                currentEntry.value.tags.forEachIndexed { index, tag ->
                    if (index > 0) Text(", ")
                    Text(tag.title)
                }
            }
            Button(onClick = actionShowAllEntries) {
                Text("All Entries")
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
        entry: State<Entry>,
        tags: State<List<Tag>>,
        actionUpdateWithTags: (List<Tag>) -> Unit,
        actionHide: () -> Unit
    ) {
        val entryTags = entry.value.tags
        val mainTags = tags.value.filter { it.parentId == null }
        var activeTab = remember { mutableStateOf(mainTags.firstOrNull()?.id) }
        var tabTags = tags.value.filter { it.parentId == activeTab.value }
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
                tabTags.forEach { tag ->
                    val isActive = entryTags.contains(tag)
                    Text(
                        modifier = Modifier.clickable {
                            if (isActive)
                                actionUpdateWithTags(entryTags - tag)
                            else
                                actionUpdateWithTags(entryTags + tag)
                        },
                        color = if (isActive) Color.Red else Color.Black,
                        style = if (isActive) TextStyle(fontWeight = FontWeight.SemiBold) else TextStyle(fontWeight = FontWeight.Normal),
                        text = tag.title
                    )
                }
            }
        }
    }

    @Composable
    private fun TemplatesPicker(actionHide: () -> Unit) {

    }

    @Composable
    private fun AllEntriesPicker(entries: State<List<Entry>>, pickEntry: (Entry) -> Unit, actionHide: () -> Unit) {
        LazyColumn(
            horizontalAlignment = Alignment.End
        ) {
            item {
                Button(onClick = actionHide) {
                    Text("X")
                }
            }
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