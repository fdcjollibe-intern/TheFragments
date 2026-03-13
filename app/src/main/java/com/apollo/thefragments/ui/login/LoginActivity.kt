package com.apollo.thefragments.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.apollo.thefragments.MainActivity
import com.apollo.thefragments.R
import com.apollo.thefragments.data.db.AppDatabase
import com.apollo.thefragments.repository.AuthRepository

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // ─────────────────────────────────────────────────────────────
        // WIRING UP THE MVVM CHAIN
        // ─────────────────────────────────────────────────────────────
        // Activity → ViewModel → Repository → DAO → Room
        //
        // 1. Get the DAO from the database singleton
        // 2. Pass it into the Repository
        // 3. Pass the Repository into the ViewModelFactory
        // 4. Use the Factory to create the ViewModel
        // ─────────────────────────────────────────────────────────────
        val dao        = AppDatabase.getDatabase(this).userDao()
        val repository = AuthRepository(dao)
        val factory    = LoginViewModelFactory(repository)
        viewModel      = ViewModelProvider(this, factory)[LoginViewModel::class.java]

        val etUsername = findViewById<EditText>(R.id.et_username)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnLogin   = findViewById<Button>(R.id.btn_login)
        val btnRegister = findViewById<Button>(R.id.btn_register)

        // ─────────────────────────────────────────────────────────────
        // OBSERVING LIVEDATA
        // ─────────────────────────────────────────────────────────────
        // We don't call the ViewModel and wait for a return value.
        // Instead we OBSERVE — the Activity just watches and reacts
        // whenever the ViewModel posts a new value.
        // ─────────────────────────────────────────────────────────────

        // Observe login result
        viewModel.loginResult.observe(this) { result ->
            if (result == "SUCCESS") {
                // Navigate to MainActivity and clear the back stack
                // so pressing back from MainActivity doesn't return to login
                Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                // Show the error message from the ViewModel
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
            }
        }

        // Observe register result
        viewModel.registerResult.observe(this) { result ->
            if (result == "SUCCESS") {
                Toast.makeText(this, "Registered! You can now log in.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
            }
        }

        // Login button — pass input to ViewModel, ViewModel handles the rest
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()
            viewModel.login(username, password)
        }

        // Register button — same pattern
        btnRegister.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()
            viewModel.register(username, password)
        }
    }
}