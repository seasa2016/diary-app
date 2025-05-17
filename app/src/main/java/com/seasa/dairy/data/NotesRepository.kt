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

package com.seasa.dairy.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository that provides insert, update, delete, and retrieve of [Note] from a given data source.
 */
interface NotesRepository {
    /**
     * Retrieve all the notes from the the given data source.
     */
    fun getAllNoteBriefs(): Flow<List<NoteBrief>>

    /**
     * Retrieve an note from the given data source that matches with the [id].
     */
    fun getNoteStream(id: Int): Flow<Note?>

    /**
     * Insert note in the data source
     */
    suspend fun insertNote(note: Note)

    /**
     * Delete note from the data source
     */
    suspend fun deleteNote(note: Note)

    /**
     * Update note in the data source
     */
    suspend fun updateNote(note: Note)
}