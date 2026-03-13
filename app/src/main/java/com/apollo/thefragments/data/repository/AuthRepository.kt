package com.apollo.thefragments.repository

import com.apollo.thefragments.data.db.UserDao
import com.apollo.thefragments.data.model.User

// Repository sits between the ViewModel and the DAO.
// The ViewModel never talks to the DAO directly — it always goes through here.
//
// Why? Because in a real app you might have multiple data sources
// (Room database, a remote API, cache). The repository decides
// which source to use. The ViewModel doesn't need to know or care.
class AuthRepository(private val userDao: UserDao) {

    // Called when user taps Register
    // Returns a Result — either success or a failure with a message
    suspend fun register(username: String, password: String): Result<String> {
        // First check if username is already taken
        val existing = userDao.findByUsername(username)
        return if (existing != null) {
            // Username already in the database — tell the user
            Result.failure(Exception("Username already exists"))
        } else {
            // Username is free — insert the new user
            // id = 0 because autoGenerate = true handles it
            userDao.insertUser(User(username = username, password = password))
            Result.success("Registered successfully")
        }
    }

    // Called when user taps Login
    // Returns the User if found, null if username/password don't match
    suspend fun login(username: String, password: String): User? {
        return userDao.login(username, password)
    }
}
