package com.seasa.dairy

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.seasa.dairy.data.DairyDatabase
import com.seasa.dairy.data.Item
import com.seasa.dairy.data.NoteDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

private var item1 = Item(1, "Apples", 10.0, 20)
private var item2 = Item(2, "Bananas", 15.0, 97)

@RunWith(AndroidJUnit4::class)
class NoteDaoTest {
    private lateinit var noteDao: NoteDao
    private lateinit var dairyDatabase: DairyDatabase

    @Before
    fun createDb() {
        val context: Context = ApplicationProvider.getApplicationContext()
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        dairyDatabase = Room.inMemoryDatabaseBuilder(context, DairyDatabase::class.java)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()
        noteDao = dairyDatabase.noteDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        dairyDatabase.close()
    }

    private suspend fun addOneItemToDb() {
        noteDao.insert(item1)
    }

    private suspend fun addTwoItemsToDb() {
        noteDao.insert(item1)
        noteDao.insert(item2)
    }

    @Test
    @Throws(Exception::class)
    fun daoInsert_insertsItemIntoDB() = runBlocking {
        addOneItemToDb()
        val allItems = noteDao.getAllItems().first()
        assertEquals(allItems[0], item1)
    }

    @Test
    @Throws(Exception::class)
    fun daoGetAllItems_returnsAllItemsFromDB() = runBlocking {
        addTwoItemsToDb()
        val allItems = noteDao.getAllItems().first()
        assertEquals(allItems[0], item1)
        assertEquals(allItems[1], item2)
    }

    @Test
    @Throws(Exception::class)
    fun daoUpdateItems_updatesItemsInDB() = runBlocking {
        addTwoItemsToDb()
        noteDao.update(Item(1, "Apples", 15.0, 25))
        noteDao.update(Item(2, "Bananas", 5.0, 50))

        val allItems = noteDao.getAllItems().first()
        assertEquals(allItems[0], Item(1, "Apples", 15.0, 25))
        assertEquals(allItems[1], Item(2, "Bananas", 5.0, 50))
    }

    @Test
    @Throws(Exception::class)
    fun daoDeleteItems_deletesAllItemsFromDB() = runBlocking {
        addTwoItemsToDb()
        noteDao.delete(item1)
        noteDao.delete(item2)
        val allItems = noteDao.getAllItems().first()
        assertTrue(allItems.isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun daoGetItem_returnsItemFromDB() = runBlocking {
        addOneItemToDb()
        val item = noteDao.getItem(1)
        assertEquals(item.first(), item1)
    }
}