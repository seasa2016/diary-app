package com.seasa.diary.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database class with a singleton Instance object.
 */
@Database(entities = [Note::class], version = 2, exportSchema = false)
abstract class DiaryDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var Instance: DiaryDatabase? = null

        fun getDatabase(context: Context): DiaryDatabase {
            val migration1to2 = object : Migration(1, 2) { // From version 1 to 2
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE notes ADD COLUMN imageUrisJson TEXT")
                }
            }

            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, DiaryDatabase::class.java, "note_database")
                    .addMigrations(migration1to2)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}