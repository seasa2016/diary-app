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

package com.seasa.diary.data

import android.content.Context

/**
 * App container for Dependency injection.
 */
interface AppContainer {
    val notesRepository: NotesRepository
    val loginRepository: LoginRepository
    val googleDriveRepository: GoogleDriveRepository
}

/**
 * [AppContainer] implementation that provides instance of [OfflineNotesRepository]
 */
class AppDataContainer(private val context: Context) : AppContainer {
    /**
     * Implementation for [NotesRepository]
     */
    override val notesRepository: NotesRepository by lazy {
        OfflineNotesRepository(DiaryDatabase.getDatabase(context).noteDao())
    }
    override val loginRepository: LoginRepository by lazy {
        LoginRepository(context)
    }
    override val googleDriveRepository: GoogleDriveRepository by lazy {
        GoogleDriveRepository(context)
    }
}
