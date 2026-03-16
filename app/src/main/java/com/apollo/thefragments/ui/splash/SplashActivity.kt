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

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val db         = AppDatabase.getDatabase(this)
        val repository = AuthRepository(db.userDao(), db.sessionDao())

        lifecycleScope.launch {
            delay(1500)

            val destination = if (repository.isLoggedIn()) {
                Intent(this@SplashActivity, MainActivity::class.java)
            } else {
                Intent(this@SplashActivity, AuthActivity::class.java)
            }

            // Clear the back stack so pressing back from the next screen does not return to SplashActivity
            destination.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(destination)
            finish()
        }
    }
}
