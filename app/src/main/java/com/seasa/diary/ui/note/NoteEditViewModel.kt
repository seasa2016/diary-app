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

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.seasa.diary.data.NotesRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel to retrieve and update an note from the [NotesRepository]'s data source.
 */
class NoteEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val notesRepository: NotesRepository
) : NoteEntryViewModel(notesRepository) {
    private val noteId: Int = checkNotNull(savedStateHandle[NoteEditDestination.noteIdArg])


    init {
        viewModelScope.launch {
            noteUiState = notesRepository.getNoteStream(noteId)
                .filterNotNull()
                .first()
                .toNoteUiState(true).copy(isLoading = false)

            Log.d("Info", String.format("edit date: %d", noteUiState.noteDetail.date))
        }
    }

    override suspend fun saveNote()  {
        notesRepository.updateNote(noteUiState.noteDetail.toNote())
    }
}
