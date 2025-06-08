package com.seasa.diary.ui.note

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.seasa.diary.DiaryTopAppBar
import com.seasa.diary.R
import com.seasa.diary.ui.AppViewModelProvider
import com.seasa.diary.ui.navigation.NavigationDestination
import com.seasa.diary.ui.theme.DiaryTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NoteEntryDestination : NavigationDestination {
    override val route = "note_entry"
    override val titleRes = R.string.note_entry_title
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEntryScreen(
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    canNavigateBack: Boolean = true,
    dateSelectEnabled: Boolean = true,
    viewModel: NoteEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->uri?.let {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        viewModel.insertImageLabel(viewModel.noteUiState.noteDetail.content, uri)
    }}

    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            DiaryTopAppBar(
                title = stringResource(NoteEntryDestination.titleRes),
                canNavigateBack = canNavigateBack,
                navigateUp = onNavigateUp
            )
        },
        floatingActionButton = {
            var isMenuExpanded by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd
            ) {
                // child button
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    AnimatedVisibility(visible = isMenuExpanded) {
                        FloatingActionButton(
                            onClick = {pickMediaLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )},
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Upload image")
                        }
                    }
                    // Main button
                    FloatingActionButton(
                        onClick = { isMenuExpanded = !isMenuExpanded },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = if (isMenuExpanded) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = "Main Action"
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NoteEntryBody(
            noteUiState = viewModel.noteUiState,
            onNoteValueChange = viewModel::updateUiState,
            onSaveClick = {
                coroutineScope.launch {
                    viewModel.saveNote()
                    navigateBack()
                }
            },
            dateSelectEnabled = dateSelectEnabled,
            modifier = Modifier
                .padding(
                    start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                    end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                    top = innerPadding.calculateTopPadding()
                )
                .fillMaxWidth()
        )
    }
}

@Composable
fun NoteEntryBody(
    noteUiState: NoteUiState,
    onNoteValueChange: (NoteDetail) -> Unit,
    onSaveClick: () -> Unit,
    dateSelectEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large)),
        modifier = modifier
            .padding(dimensionResource(id = R.dimen.padding_medium))
            .fillMaxHeight()
    ) {
        DatePicker(
            noteDetail = noteUiState.noteDetail,
            onValueChange = onNoteValueChange,
            enabled = dateSelectEnabled
        )
        OutlinedTextField(
            value = noteUiState.noteDetail.title,
            onValueChange = { onNoteValueChange(noteUiState.noteDetail.copy(title = it)) },
            label = { Text(stringResource(R.string.note_title)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = noteUiState.noteDetail.content,
            onValueChange = { onNoteValueChange(noteUiState.noteDetail.copy(content = it)) },
            label = { Text(stringResource(R.string.note_content)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        )
        Button(
            onClick = onSaveClick,
            enabled = noteUiState.isEntryValid,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Text(text = stringResource(R.string.save_action))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePicker(
    noteDetail: NoteDetail,
    onValueChange: (NoteDetail) -> Unit = {},
    enabled: Boolean = true
) {
    val showDialog = remember { mutableStateOf(false) }
    val date = remember {
        mutableStateOf(
            String.format(
                Locale.US,
                "%04d-%02d-%02d",
                noteDetail.date / 10000,
                (noteDetail.date / 100) % 100,
                noteDetail.date % 100
            )
        )
    }

    Column {
        OutlinedTextField(
            value = date.value,
            onValueChange = { newValue ->
                date.value = newValue

                try {
                    val parsedDate = (newValue.filterNot { it == '-' }).toInt()
                    if (parsedDate.isValidDate()) {
                        onValueChange(noteDetail.copy(date = parsedDate))
                    }
                } catch (_: java.lang.NumberFormatException) {
                    Log.d("warning", String.format("date parse error: %s", newValue))
                }
            },
            label = { Text(stringResource(R.string.date_with_format)) },
            trailingIcon = {
                IconButton(onClick = { showDialog.value = true }) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = stringResource(R.string.choose_date)
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth(),
            enabled = enabled
        )
        Log.d("Info", String.format("showDialog: %b", showDialog.value))
        if (showDialog.value && enabled) {
            DatePickerDialog(
                onDismissRequest = { showDialog.value = false },
                confirmButton = {
                    TextButton(
                        onClick = { showDialog.value = false }
                    ) { Text(stringResource(R.string.confirm)) }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDialog.value = false }
                    ) { Text(stringResource(R.string.cancel)) }
                }
            ) {
                val datePickerState: DatePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = System.currentTimeMillis()
                )
                DatePicker(state = datePickerState)
                onValueChange(noteDetail.copy(date = datePickerState.selectedDateMillis?.let { millis ->
                    SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(millis)).toInt()
                } ?: 0))
                date.value = String.format(
                    Locale.US,
                    "%04d-%02d-%02d",
                    noteDetail.date / 10000,
                    (noteDetail.date / 100) % 100,
                    noteDetail.date % 100
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NoteEntryScreenPreview() {
    DiaryTheme {
        NoteEntryBody(
            noteUiState = NoteUiState(
                NoteDetail(
                    date = 20250401, content = TextFieldValue("hello world")
                )
            ), onNoteValueChange = {}, onSaveClick = {}, dateSelectEnabled = true
        )
    }
}
