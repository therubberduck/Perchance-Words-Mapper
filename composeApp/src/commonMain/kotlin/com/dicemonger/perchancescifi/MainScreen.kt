package com.dicemonger.perchancescifi

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.dicemonger.perchancescifi.model.Entry
import com.dicemonger.perchancescifi.model.Tag
import com.dicemonger.perchancescifi.model.Template
import org.koin.compose.viewmodel.koinViewModel

object MainScreen {

    @Composable
    fun Create() {
        val viewModel: MainViewModel = koinViewModel()
        val snackbarHostState = remember { SnackbarHostState() }
        val toast by viewModel.toast.collectAsState()

        Box(modifier = Modifier.fillMaxSize()) {
            CreateInstance(
                activeTemplate = viewModel.currentTemplate.collectAsState(),
                allTemplates = viewModel.templates.collectAsState(),
                actionApplyTemplate = viewModel::applyTemplate,
                actionChangeTemplate = viewModel::changeTemplate,
                actionCreateTemplate = viewModel::saveEntryAsTemplate,
                actionDeleteTemplate = viewModel::deleteTemplate,
                currentEntry = viewModel.currentEntry.collectAsState(),
                allEntries = viewModel.allEntries.collectAsState(),
                allTags = viewModel.allTags.collectAsState(),
                isLoading = viewModel.isLoading.collectAsState(),
                actionCreateEntry = viewModel::addEntry,
                actionDeleteEntry = viewModel::removeCurrentEntry,
                actionChangeTitle = viewModel::changeTitle,
                actionChangeDescription = viewModel::changeDescription,
                actionUpdateWithTags = viewModel::updateWithTags,
                actionChangeEntry = viewModel::changeEntry,
                actionSaveAllEntries = viewModel::saveAllEntries
            )

            LaunchedEffect(toast) {
                toast?.let {
                    snackbarHostState.showSnackbar(it)
                    viewModel.clearToast()
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun CreateInstance(
        activeTemplate: State<Template?>,
        allTemplates: State<List<Template>>,
        actionApplyTemplate: () -> Unit,
        actionChangeTemplate: (Template) -> Unit,
        actionCreateTemplate: (String) -> Unit,
        actionDeleteTemplate: () -> Unit,
        currentEntry: State<Entry?>,
        allEntries: State<List<Entry>>,
        allTags: State<List<Tag>>,
        isLoading: State<Boolean>,
        actionCreateEntry: () -> Unit,
        actionDeleteEntry: () -> Unit,
        actionChangeTitle: (String) -> Unit,
        actionChangeDescription: (String) -> Unit,
        actionUpdateWithTags: (List<Tag>) -> Unit,
        actionChangeEntry: (Entry) -> Unit,
        actionSaveAllEntries: () -> Unit
    ) {
        var showTemplatesPicker by remember { mutableStateOf(false) }
        var showAllEntries by remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxSize()) {
            if (currentEntry.value != null && isLoading.value.not() && showTemplatesPicker.not() && showAllEntries.not()) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f)) {
                        EditView(
                            activeTemplate = activeTemplate,
                            actionApplyTemplate = actionApplyTemplate,
                            actionChangeTemplate = { showTemplatesPicker = true },
                            actionCreateTemplate = actionCreateTemplate,
                            actionDeleteTemplate = actionDeleteTemplate,
                            currentEntry = currentEntry,
                            actionCreateEntry = actionCreateEntry,
                            actionDeleteEntry = actionDeleteEntry,
                            actionChangeTitle = actionChangeTitle,
                            actionChangeDescription = actionChangeDescription,
                            actionShowAllEntries = { showAllEntries = true },
                            actionSaveAllEntries = actionSaveAllEntries
                        )

                    }
                    Box(modifier = Modifier.weight(1f)) {
                        TagsPicker.Create(
                            entry = currentEntry,
                            tags = allTags,
                            actionUpdateWithTags = actionUpdateWithTags
                        )
                    }
                }
            }
            if (showTemplatesPicker) {
                TemplatesPicker(allTemplates, actionChangeTemplate, actionHide = { showTemplatesPicker = false })
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
        activeTemplate: State<Template?>,
        actionApplyTemplate: () -> Unit,
        actionChangeTemplate: () -> Unit,
        actionCreateTemplate: (String) -> Unit,
        actionDeleteTemplate: () -> Unit,
        currentEntry: State<Entry?>,
        actionCreateEntry: () -> Unit,
        actionDeleteEntry: () -> Unit,
        actionChangeTitle: (String) -> Unit,
        actionChangeDescription: (String) -> Unit,
        actionShowAllEntries: () -> Unit,
        actionSaveAllEntries: () -> Unit
    ) {
        var showDeleteConfirmation by remember { mutableStateOf(false) }

        val currentEntry = currentEntry.value ?: return
        Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
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
            FlowRow {
                if (currentEntry.tags.isEmpty()) {
                    Text(
                        text = "No tags",
                        style = TextStyle(color = Color.Gray, textDecoration = TextDecoration.Underline)
                    )
                } else {
                    currentEntry.tags.forEachIndexed { index, tag ->
                        if (index > 0) Text(", ")
                        Text(tag.title)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = actionCreateEntry) {
                    Text("Create New")
                }
                Button(onClick = { showDeleteConfirmation = true }) {
                    Text("Delete")
                }
                Button(onClick = actionShowAllEntries) {
                    Text("All Entries")
                }
                Button(onClick = actionSaveAllEntries) {
                    Text("Save All")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            TemplateSection(
                activeTemplate,
                actionApplyTemplate,
                actionChangeTemplate,
                actionCreateTemplate,
                actionDeleteTemplate
            )
        }

        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text("Delete Entry") },
                text = { Text("Are you sure you want to delete this entry?") },
                confirmButton = {
                    Button(onClick = {
                        actionDeleteEntry()
                        showDeleteConfirmation = false
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDeleteConfirmation = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    @Composable
    private fun TemplateSection(
        activeTemplate: State<Template?>,
        actionApplyTemplate: () -> Unit,
        actionChangeTemplate: () -> Unit,
        actionCreateTemplate: (String) -> Unit,
        actionDeleteTemplate: () -> Unit
    ) {
        val activeTemplate = activeTemplate.value ?: return

        var showCreationDialog by remember { mutableStateOf(false) }
        var showDeleteTemplateConfirmation by remember { mutableStateOf(false) }
        FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                border = BorderStroke(2.dp, Color.Black),
                onClick = actionChangeTemplate
            ) {
                Text(activeTemplate.title)
            }
            Button(onClick = actionApplyTemplate) {
                Text("Apply Template")
            }
            Button(onClick = { showCreationDialog = true }) {
                Text("Create Template")
            }
            Button(onClick = { showDeleteTemplateConfirmation = true }) {
                Text("Delete Template")
            }
        }

        if (showCreationDialog) {
            var dialogTitle by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showCreationDialog = false },
                title = { Text("Create Template") },
                text = {
                    TextField(
                        value = dialogTitle,
                        onValueChange = { dialogTitle = it },
                        label = { Text("Template Title") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        actionCreateTemplate(dialogTitle)
                        showCreationDialog = false
                    }) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    Button(onClick = { showCreationDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showDeleteTemplateConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteTemplateConfirmation = false },
                title = { Text("Delete Template") },
                text = { Text("Are you sure you want to delete this template?") },
                confirmButton = {
                    Button(onClick = {
                        actionDeleteTemplate()
                        showDeleteTemplateConfirmation = false
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDeleteTemplateConfirmation = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    @Composable
    private fun TemplatesPicker(
        templates: State<List<Template>>,
        actionChangeTemplate: (Template) -> Unit,
        actionHide: () -> Unit
    ) {
        Column {
            Button(modifier = Modifier.align(Alignment.End), onClick = actionHide) {
                Text("X")
            }
            LazyColumn {
                items(items = templates.value) { item ->
                    Column(
                        modifier = Modifier
                            .padding(4.dp)
                            .fillMaxWidth()
                            .clickable {
                                actionChangeTemplate(item)
                                actionHide()
                            }
                            .border(1.dp, Color.Black, RoundedCornerShape(4f))
                            .padding(4.dp)
                    ) {
                        Text(item.title)
                        Text(item.entry.desc)
                        FlowRow {
                            item.entry.tags.forEachIndexed { index, tag ->
                                if (index > 0) Text(", ")
                                Text(tag.title)
                            }
                        }
                    }
                }
            }
        }
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