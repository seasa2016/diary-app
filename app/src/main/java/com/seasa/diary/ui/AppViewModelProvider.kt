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

package com.seasa.diary.ui

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.seasa.diary.DiaryApplication
import com.seasa.diary.ui.home.HomeViewModel
import com.seasa.diary.ui.note.NoteEditViewModel
import com.seasa.diary.ui.note.NoteEntryViewModel
import com.seasa.diary.ui.note.NoteDetailsViewModel
import com.seasa.diary.ui.setting.BackupViewModel

/**
 * Provides Factory to create instance of ViewModel for the entire Diary app
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for ItemEntryViewModel
        initializer {
            NoteEntryViewModel(diaryApplication().container.notesRepository)
        }
        // Initializer for ItemEditViewModel
        initializer {
            NoteEditViewModel(
                this.createSavedStateHandle(),
                diaryApplication().container.notesRepository
            )
        }
        // Initializer for ItemDetailsViewModel
        initializer {
            NoteDetailsViewModel(
                this.createSavedStateHandle(),
                diaryApplication().container.notesRepository
            )
        }
        // Initializer for HomeViewModel
        initializer {
            HomeViewModel(diaryApplication().container.notesRepository)
        }
        // Initializer for SignInViewModel
        initializer {
            BackupViewModel(
                diaryApplication().container.loginRepository,
                diaryApplication().container.googleDriveRepository,
                diaryApplication().container.notesRepository
            )
        }
    }
}

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [DiaryApplication].
 */
fun CreationExtras.diaryApplication(): DiaryApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as DiaryApplication)
