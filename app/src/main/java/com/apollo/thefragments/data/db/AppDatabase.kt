package com.apollo.thefragments.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.apollo.thefragments.data.model.User

// @Database — tells Room this is the actual database class
// entities  — list of all tables (each @Entity class is one table)
// version   — database version number. If you change the schema later,
//             you bump this number and provide a migration
// exportSchema — saves schema to a JSON file for version tracking.
//               false for now to keep it simple
@Database(entities = [User::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // Room generates the implementation of UserDao for us
    // We just declare it here as abstract
    abstract fun userDao(): UserDao

    companion object {
        // ─────────────────────────────────────────────────────────────
        // SINGLETON PATTERN
        // ─────────────────────────────────────────────────────────────
        // We only ever want ONE instance of the database open at a time.
        // @Volatile means any write to INSTANCE is immediately visible
        // to all threads — prevents two threads from creating two databases
        // at the same time.
        // ─────────────────────────────────────────────────────────────
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // If INSTANCE is not null, return it immediately
            // If it IS null, create it inside synchronized() so only
            // one thread can create it at a time
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,  // always use applicationContext to avoid leaks
                    AppDatabase::class.java,
                    "thefragments_db"            // the actual .db file name on disk
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
