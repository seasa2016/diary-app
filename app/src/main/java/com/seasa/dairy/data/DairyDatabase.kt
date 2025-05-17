package com.seasa.dairy.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Database class with a singleton Instance object.
 */
@Database(entities = [Note::class], version = 1, exportSchema = false)
abstract class DairyDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var Instance: DairyDatabase? = null

        fun getDatabase(context: Context): DairyDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, DairyDatabase::class.java, "note_database")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}