package com.seasa.diary.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Transaction
    suspend fun runInTransaction(fn:suspend ()->Unit) {
        fn()
    }

    @Query("DELETE FROM notes")
    fun deleteAll()

    @Query("SELECT * from notes WHERE id = :id")
    fun getNote(id: Int): Flow<Note>

    @Query("SELECT id, date, title from notes ORDER BY date ASC")
    fun getAllNoteBriefs(): Flow<List<NoteBrief>>

    @Query("SELECT * from notes ORDER BY date ASC")
    fun getAllNotes(): Flow<List<Note>>
}