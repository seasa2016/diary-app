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

package com.seasa.dairy.ui.note

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.seasa.dairy.data.Note
import com.seasa.dairy.data.NotesRepository

/**
 * ViewModel to validate and insert notes in the Room database.
 */
class NoteEntryViewModel(private val notesRepository: NotesRepository) : ViewModel() {

    /**
     * Holds current note ui state
     */
    var noteUiState by mutableStateOf(NoteUiState())
        private set

    /**
     * Updates the [noteUiState] with the value provided in the argument. This method also triggers
     * a validation for input values.
     */
    fun updateUiState(noteDetail: NoteDetail) {
        noteUiState =
            NoteUiState(noteDetail = noteDetail, isEntryValid = validateInput(noteDetail))
    }

    private fun validateInput(uiState: NoteDetail = noteUiState.noteDetail): Boolean {
        if (uiState.title.isEmpty()){
            return false
        }
        return uiState.date.isValidDate()
    }

    suspend fun saveNote() {
        if (validateInput()) {
            notesRepository.insertNote(noteUiState.noteDetail.toNote())
        }
    }
}

/**
 * Represents Ui State for an Note.
 */
data class NoteUiState(
    val noteDetail: NoteDetail = NoteDetail(),
    val isEntryValid: Boolean = false,
    val isLoading: Boolean = true
)

data class NoteDetail(
    val id: Int = 0,
    val date: Int = 0,
    val title: String = "",
    val content: String = "",
)

/**
 * Extension function to convert [NoteDetails] to [Note].
 */
fun NoteDetail.toNote(): Note = Note(
    id = id,
    date = date,
    title = title,
    content = content
)

fun Int.isValidDate():Boolean  {
    val year: Int = this / 10000
    val month: Int = (this / 100) % 100
    val day: Int = this % 100

    android.util.Log.d("Info", String.format("year: %d, month: %d, day: %d", year, month, day))
    if (year == 0 || month == 0 || day == 0) {
        return false
    } else {
        return when (month) {
            1 -> day <= 31
            2 -> {
                if (year % 400 == 0) {
                    return day <= 29
                } else if (year % 100 == 0) {
                    return day <= 28
                } else if (year % 4 == 0) {
                    return day <= 29
                } else {
                    day <= 28
                }
            }

            3 -> day <= 31
            4 -> day <= 30
            5 -> day <= 31
            6 -> day <= 30
            7 -> day <= 31
            8 -> day <= 31
            9 -> day <= 30
            10 -> day <= 31
            11 -> day <= 30
            12 -> day <= 31
            else -> false
        }
    }
}

/**
 * Extension function to convert [Note] to [NoteUiState]
 */
fun Note.toNoteUiState(isEntryValid: Boolean = false): NoteUiState = NoteUiState(
    noteDetail = this.toNoteDetail(),
    isEntryValid = isEntryValid
)

/**
 * Extension function to convert [Note] to [NoteDetails]
 */
fun Note.toNoteDetail(): NoteDetail = NoteDetail(
    id = id,
    date = date,
    title = title,
    content = content,
)
