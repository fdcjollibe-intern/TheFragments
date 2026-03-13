package com.apollo.thefragments.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.apollo.thefragments.data.model.Session

@Dao
interface SessionDao {
    // REPLACE so we always overwrite the single row (id = 1)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(session: Session)

    // Returns null if no session row exists yet (first ever launch)
    @Query("SELECT * FROM session WHERE id = 1 LIMIT 1")
    suspend fun getSession(): Session?

    @Query("DELETE FROM session")
    suspend fun clearSession()
}
