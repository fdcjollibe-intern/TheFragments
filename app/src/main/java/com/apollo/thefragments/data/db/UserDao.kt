package com.apollo.thefragments.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.apollo.thefragments.data.model.User

@Dao
interface UserDao {
    // REPLACE = if user already exists (same uid), overwrite with fresh data
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    suspend fun getUserByUid(uid: String): User?

    @Query("DELETE FROM users")
    suspend fun clearAll()
}
