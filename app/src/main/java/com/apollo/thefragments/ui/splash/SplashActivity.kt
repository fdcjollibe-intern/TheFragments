package com.apollo.thefragments.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.apollo.thefragments.MainActivity
import com.apollo.thefragments.R
import com.apollo.thefragments.data.db.AppDatabase
import com.apollo.thefragments.repository.AuthRepository
import com.apollo.thefragments.ui.auth.AuthActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// @SuppressLint("CustomSplashScreen") — Android 12+ has its own splash screen API.
// We're using a custom one for simplicity and compatibility with minSdk 29.
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val db         = AppDatabase.getDatabase(this)
        val repository = AuthRepository(db.userDao(), db.sessionDao())

        lifecycleScope.launch {
            // Show splash for 1.5 seconds then decide where to go
            delay(1500)

            val destination = if (repository.isLoggedIn()) {
                // Room says isLoggedIn = true → skip auth, go straight to app
                Intent(this@SplashActivity, MainActivity::class.java)
            } else {
                // Not logged in → show Login/Register screen
                Intent(this@SplashActivity, AuthActivity::class.java)
            }

            // Clear the back stack so pressing back from the next screen
            // does not return to SplashActivity
            destination.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(destination)
            finish()
        }
    }
}
