package com.apollo.thefragments.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity tells Room "this class is a database table"
// tableName = the actual table name inside the SQLite database
@Entity(tableName = "users")
data class User(

    // @PrimaryKey — every table needs a unique identifier per row
    // autoGenerate = true — Room auto-increments this number (1, 2, 3...)
    // you never set this yourself when inserting
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val username: String,
    val password: String
)
