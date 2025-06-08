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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.seasa.diary.data.Note
import com.seasa.diary.data.NotesRepository
import java.util.UUID

/**
 * ViewModel to validate and insert notes in the Room database.
 */
open class NoteEntryViewModel(private val notesRepository: NotesRepository) : ViewModel() {

    /**
     * Holds current note ui state
     */
    open var noteUiState by mutableStateOf(NoteUiState())
        protected set

    /**
     * Updates the [noteUiState] with the value provided in the argument. This method also triggers
     * a validation for input values.
     */
    fun updateUiState(noteDetail: NoteDetail) {
        noteUiState =
            NoteUiState(noteDetail = noteDetail, isEntryValid = validateInput(noteDetail), isLoading = false)
    }


    fun insertImageLabel(
        currentContent: TextFieldValue,
        imageUri: Uri
    ) {
        val imageLabel = "[IMAGE_${UUID.randomUUID()}]"

        val cursorPosition = currentContent.selection.start
        val text = currentContent.text
        val newText = text.substring(0, cursorPosition) + imageLabel + text.substring(cursorPosition)
        val newCursorPosition = cursorPosition + imageLabel.length
        val newTextFieldValue = TextFieldValue(text = newText, selection = TextRange(newCursorPosition))

        // Update the UI state with the new content (as string) and the image map
        val updatedImageMap = noteUiState.noteDetail.imageUriMap.toMutableMap()
        updatedImageMap[imageLabel] = imageUri.toUriWrapper()

        updateUiState(
            noteUiState.noteDetail.copy(
                content = newTextFieldValue,
                imageUriMap = updatedImageMap
            )
        )
    }

    private fun validateInput(uiState: NoteDetail = noteUiState.noteDetail): Boolean {
        if (uiState.title.isEmpty()){
            return false
        }
        return uiState.date.isValidDate()
    }

    open suspend fun saveNote() {
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
    val content: TextFieldValue = TextFieldValue(""), // After
    val imageUriMap: Map<String, UriWrapper> = mapOf()
)

data class UriWrapper(val uriString: String) {
   fun toUri(): Uri = Uri.parse(uriString)
}
fun Uri.toUriWrapper() = UriWrapper(this.toString())

/**
 * Extension function to convert [NoteDetails] to [Note].
 */
fun NoteDetail.toNote(): Note {
    val gson = Gson()
    // Convert Map<String, UriWrapper> to JSON String
    val imageUrisJsonString = if (imageUriMap.isNotEmpty()) {
        gson.toJson(imageUriMap)
    } else {
        null // Store null if the map is empty
    }

    return Note(
        id = id,
        date = date,
        title = title,
        content = content.text, // Ensure this matches the type in your Note entity
        imageUrisJson = imageUrisJsonString
    )
}

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
fun Note.toNoteDetail(): NoteDetail {
    val gson = Gson()
    val imageMap: Map<String, UriWrapper> = if (!imageUrisJson.isNullOrBlank()) {
        try {
            // Define the type for deserialization
            val type = object : TypeToken<Map<String, UriWrapper>>() {}.type
            gson.fromJson(imageUrisJson, type)
        } catch (e: Exception) {
            // Handle potential JSON parsing errors, e.g., log and return empty map
            android.util.Log.e("NoteMapping", "Error parsing imageUrisJson: $imageUrisJson", e)
            emptyMap()
        }
    } else {
        emptyMap()
    }

    return NoteDetail(
        id = id,
        date = date,
        title = title,
        content = TextFieldValue(content), // Ensure this matches the type in your NoteDetail
        imageUriMap = imageMap
    )
}
