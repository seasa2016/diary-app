package com.seasa.diary

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.seasa.diary.data.DiaryDatabase
import com.seasa.diary.data.Note
import com.seasa.diary.data.NoteBrief
import com.seasa.diary.data.NoteDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

private var note1 = Note(1, 20250101, "title1", "content1")
private var note2 = Note(2, 20250102, "title2", "content2")
private var noteBrief1 = NoteBrief(1, 20250101, "title1")
private var noteBrief2 = NoteBrief(2, 20250102, "title2")

@RunWith(AndroidJUnit4::class)
class NoteDaoTest {
    private lateinit var noteDao: NoteDao
    private lateinit var diaryDatabase: DiaryDatabase

    @Before
    fun createDb() {
        val context: Context = ApplicationProvider.getApplicationContext()
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        diaryDatabase = Room.inMemoryDatabaseBuilder(context, DiaryDatabase::class.java)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()
        noteDao = diaryDatabase.noteDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        diaryDatabase.close()
    }

    private suspend fun addOneNoteToDb() {
        noteDao.insert(note1)
    }

    private suspend fun addTwoNotesToDb() {
        noteDao.insert(note1)
        noteDao.insert(note2)
    }

    @Test
    @Throws(Exception::class)
    fun daoInsert_insertsNoteIntoDB() = runBlocking {
        addOneNoteToDb()
        val allNotes = noteDao.getAllNoteBriefs().first()
        assertEquals(allNotes[0], noteBrief1)
    }

    @Test
    @Throws(Exception::class)
    fun daoGetAllNotes_returnsAllNotesFromDB() = runBlocking {
        addTwoNotesToDb()
        val allNotes = noteDao.getAllNoteBriefs().first()
        assertEquals(allNotes[0], noteBrief1)
        assertEquals(allNotes[1], noteBrief2)
    }

    @Test
    @Throws(Exception::class)
    fun daoUpdateNotes_updatesNotesInDB() = runBlocking {
        addTwoNotesToDb()
        noteDao.update(Note(1, 20250201, title = "titleNew1", content = "contentNew1"))
        noteDao.update(Note(2, 20250202, title = "titleNew2", content = "contentNew2"))

        val allNotes = noteDao.getAllNoteBriefs().first()
        assertEquals(allNotes[0], NoteBrief(1, 20250201, title = "titleNew1"))
        assertEquals(allNotes[1], NoteBrief(2, 20250202, title = "titleNew2"))
    }

    @Test
    @Throws(Exception::class)
    fun daoDeleteNotes_deletesAllNotesFromDB() = runBlocking {
        addTwoNotesToDb()
        noteDao.delete(note1)
        noteDao.delete(note2)
        val allNotes = noteDao.getAllNoteBriefs().first()
        assertTrue(allNotes.isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun daoGetNote_returnsNoteFromDB() = runBlocking {
        addOneNoteToDb()
        val note = noteDao.getNote(1)
        assertEquals(note.first(), note1)
    }
}