/*
* Copyright (C) 2023 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     https://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.seasa.diary.ui.note

import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.seasa.diary.DiaryTopAppBar
import com.seasa.diary.LoadingScreen
import com.seasa.diary.R
import com.seasa.diary.ui.AppViewModelProvider
import com.seasa.diary.ui.navigation.NavigationDestination
import com.seasa.diary.ui.theme.DiaryTheme
import kotlinx.coroutines.launch
import java.util.regex.Pattern

object NoteDetailsDestination : NavigationDestination {
    override val route = "note_details"
    override val titleRes = R.string.note_detail_title
    const val noteIdArg = "noteId"
    val routeWithArgs = "$route/{$noteIdArg}"
}

sealed interface DisplayContentPart {
    data class TextPart(val text: String) : DisplayContentPart
    data class ImagePart(val label: String, val uri: Uri) : DisplayContentPart
}

fun parseContentForDisplay(
    textToParse: String,
    imageUriMap: Map<String, UriWrapper>
): List<DisplayContentPart> {
    val parts = mutableListOf<DisplayContentPart>()
    val imageLabelRegex = Pattern.compile("\\[IMAGE_[a-z0-9\\-]+]")

    var lastProcessedEnd = 0
    val matcher = imageLabelRegex.matcher(textToParse)

    while (matcher.find()) {
        val labelStart = matcher.start()
        val labelEnd = matcher.end()
        val imageLabel = matcher.group() // Get the full captured label e.g. [IMAGE_...]

        if (labelStart > lastProcessedEnd) {
            parts.add(
                DisplayContentPart.TextPart(
                    textToParse.substring(
                        lastProcessedEnd,
                        labelStart
                    )
                )
            )
        }
        Log.d("NoteDetailsScreen", imageLabel)
        imageUriMap[imageLabel.toString()]?.let { uriWrapper ->
            parts.add(
                DisplayContentPart.ImagePart(
                    label = imageLabel,
                    uri = uriWrapper.toUri()
                )
            )
        } ?: run {
            parts.add(DisplayContentPart.TextPart("(Image not found: $imageLabel)"))
        }
        lastProcessedEnd = labelEnd
    }

    if (lastProcessedEnd < textToParse.length) {
        parts.add(DisplayContentPart.TextPart(textToParse.substring(lastProcessedEnd)))
    }

    return parts
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailsScreen(
    navigateToEditNote: (Int) -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NoteDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    if (uiState.value.isLoading) {
        LoadingScreen()
    } else {
        Scaffold(
            topBar = {
                DiaryTopAppBar(
                    title = uiState.value.noteDetail.title,
                    canNavigateBack = true,
                    navigateUp = navigateBack
                )
            }, floatingActionButton = {
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
                                onClick = { navigateToEditNote(uiState.value.noteDetail.id) },
                                containerColor = MaterialTheme.colorScheme.primary
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                        }

                        AnimatedVisibility(visible = isMenuExpanded) {
                            NoteDeleteButton(onDelete = {
                                coroutineScope.launch {
                                    viewModel.deleteNote()
                                    navigateBack()
                                }
                            })
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
            }, modifier = modifier
        ) { innerPadding ->
            NoteDetails(
                noteDetail = uiState.value.noteDetail, modifier = Modifier
                    .padding(
                        start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                        end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                        top = innerPadding.calculateTopPadding()
                    )
            )
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    onDeleteConfirm: () -> Unit, onDeleteCancel: () -> Unit, modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = { /* Do nothing */ },
        title = { Text(stringResource(R.string.attention)) },
        text = { Text(stringResource(R.string.delete_question)) },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDeleteCancel) {
                Text(stringResource(R.string.no))
            }
        },
        confirmButton = {
            TextButton(onClick = onDeleteConfirm) {
                Text(stringResource(R.string.yes))
            }
        })
}

@Composable
private fun NoteDeleteButton(
    onDelete: () -> Unit
) {
    var deleteConfirmationRequired by rememberSaveable { mutableStateOf(false) }
    FloatingActionButton(
        onClick = { deleteConfirmationRequired = true },
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Icon(Icons.Default.Delete, contentDescription = "Delete")
    }

    if (deleteConfirmationRequired) {
        DeleteConfirmationDialog(
            onDeleteConfirm = {
                deleteConfirmationRequired = false
                onDelete()
            },
            onDeleteCancel = { deleteConfirmationRequired = false },
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium))
        )
    }
}

@Composable
fun NoteDetails(
    noteDetail: NoteDetail, modifier: Modifier = Modifier
) {
// Parse the content when noteDetail changes
    val displayParts = remember(noteDetail.content.text, noteDetail.imageUriMap) {
        parseContentForDisplay(noteDetail.content.text, noteDetail.imageUriMap)
    }

    LazyColumn(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(displayParts) { part ->
            when (part) {
                is DisplayContentPart.TextPart -> {
                    if (part.text.isNotBlank()) {
                        Text(text = part.text, style = MaterialTheme.typography.bodyLarge)
                    }
                }

                is DisplayContentPart.ImagePart -> {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(part.uri) // The Uri of the image
                            .crossfade(true)
                            .build(),
                        contentDescription = "Image: ${part.label}", // Accessibility
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp) // Constrain image height
                            .wrapContentHeight(),
                        contentScale = ContentScale.Fit, // Or ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NoteDetailsScreenPreview() {
    val sampleImageUri1 =
        "android.resource://com.seasa.diary/${R.drawable.ic_launcher_background}".toUri() // Replace with a real drawable
    val sampleImageUri2 =
        "android.resource://com.seasa.diary/${R.drawable.database_svgrepo_com}".toUri() // Replace with a real drawable
    val sampleNoteDetail = NoteDetail(
        id = 1,
        date = 20240115,
        title = "My Awesome Note with Images",
        content = TextFieldValue( // Or String if your NoteDetail uses String
            "This is the first part of the text.\n" +
                    "[IMAGE_123e4567-e89b-12d3-a456-426614174000]\n" +
                    "Here is some more text after the first image.\n" +
                    "And another image coming up!\n" +
                    "[IMAGE_another-uuid-for-image2-here00]\n" +
                    "Finally, some concluding text."
        ),
        imageUriMap = mapOf(
            "[IMAGE_123e4567-e89b-12d3-a456-426614174000]" to sampleImageUri1.toUriWrapper(),
            "[IMAGE_another-uuid-for-image2-here00]" to sampleImageUri2.toUriWrapper()
        )
// isLoading = false // If you add this to NoteDetail for UI state
    )
    DiaryTheme(darkTheme = true) {
        NoteDetails(
            noteDetail = sampleNoteDetail
        )
    }
}

@Composable
fun NoteDetailsPreviewVersion() {
    val displayParts = listOf(
        DisplayContentPart.TextPart("This is the first part of the text."),
        DisplayContentPart.ImagePart("Sample Image", Uri.EMPTY), // Placeholder
        DisplayContentPart.TextPart("Here is some more text after the first image.\nAnd another image coming up!"),
        DisplayContentPart.ImagePart("Sample Image 2", Uri.EMPTY),
        DisplayContentPart.TextPart("Finally, some concluding text.")
    )

    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(displayParts) { part ->
            when (part) {
                is DisplayContentPart.TextPart -> {
                    Text(text = part.text, style = MaterialTheme.typography.bodyLarge)
                }

                is DisplayContentPart.ImagePart -> {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_background),
                        contentDescription = part.label,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .wrapContentHeight(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NoteDetailsScreenPreview2() {
    DiaryTheme(darkTheme = true) {
        NoteDetailsPreviewVersion()
    }
}
