package com.apollo.thefragments.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.apollo.thefragments.data.model.User

// @Dao — Data Access Object
// This is the interface Room uses to generate the actual SQL for you.
// You just declare WHAT you want, Room figures out HOW to do it.
@Dao
interface UserDao {

    // @Insert — Room generates the INSERT SQL automatically
    // OnConflictStrategy.ABORT — if you try to insert a duplicate, it throws an error
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User)
    // suspend = this runs on a background thread (coroutine)
    // Room requires database operations off the main thread

    // @Query — for anything that needs custom SQL
    // This finds a user by matching BOTH username AND password
    // Returns null if no match found — used for login check
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun login(username: String, password: String): User?

    // Check if a username already exists — used before registering
    // to prevent duplicate usernames
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun findByUsername(username: String): User?
}
