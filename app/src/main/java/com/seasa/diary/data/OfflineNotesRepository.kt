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

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class OfflineNotesRepository(private val noteDao: NoteDao) : NotesRepository {
    override fun getAllNoteBriefs(): Flow<List<NoteBrief>> = noteDao.getAllNoteBriefs()

    override fun getNoteStream(id: Int): Flow<Note?> = noteDao.getNote(id)

    override suspend fun insertNote(note: Note) = noteDao.insert(note)

    override suspend fun deleteNote(note: Note) = noteDao.delete(note)

    override suspend fun updateNote(note: Note) = noteDao.update(note)

    override suspend fun exportDatabaseToJson(): String = withContext(Dispatchers.IO) {
        val gson = Gson()
        val exportData = mutableMapOf<String, Any>()

        // Export each table
        noteDao.getAllNotes().let { data ->
            exportData["notes"] = data
        }

        gson.toJson(exportData)
    }

    override suspend fun importDatabaseFromJson(jsonData: String) = withContext(Dispatchers.IO) {
        val gson = Gson()
        val importData = gson.fromJson(jsonData, Map::class.java)

        noteDao.runInTransaction {
            // Clear existing data
            noteDao.deleteAll()

            // Import new data
            val tableData = importData["notes"] as? List<*>
            tableData?.forEach{
                noteDao.insert(it as Note)
            }
        }
    }
}