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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seasa.diary.data.NotesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel to retrieve, update and delete an note from the [NotesRepository]'s data source.
 */
class NoteDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val notesRepository: NotesRepository
) : ViewModel() {

    private val noteId: Int = checkNotNull(savedStateHandle[NoteDetailsDestination.noteIdArg])

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    val uiState: StateFlow<NoteUiState> =
        notesRepository.getNoteStream(noteId)
            .filterNotNull()
            .map {
                NoteUiState(isEntryValid = true, noteDetail = it.toNoteDetail(), isLoading = false)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = NoteUiState()
            )

    suspend fun deleteNote() {
        notesRepository.deleteNote(uiState.value.noteDetail.toNote())
    }
}