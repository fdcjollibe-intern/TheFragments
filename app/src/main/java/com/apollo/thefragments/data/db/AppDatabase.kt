package com.apollo.thefragments.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.apollo.thefragments.data.model.Photo
import com.apollo.thefragments.data.model.Session
import com.apollo.thefragments.data.model.User

@Database(
    entities  = [User::class, Session::class, Photo::class],  // <-- Photo added
    version   = 2,                                             // <-- bump version
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun sessionDao(): SessionDao
    abstract fun photoDao(): PhotoDao      // NEW

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "thefragments_db"
                )
                    .fallbackToDestructiveMigration() // dev only — wipes DB on version bump
                    .build().also { INSTANCE = it }
            }
        }
    }
}