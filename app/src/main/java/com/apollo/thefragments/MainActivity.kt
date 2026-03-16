package com.apollo.thefragments

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.apollo.thefragments.fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val homeFragment   = HomeFragment()
    private val profileFragment = ProfileFragment()
    private val cameraFragment  = CameraFragment()
    private val settingsFragment = SettingsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, settingsFragment).hide(settingsFragment)
                .add(R.id.fragment_container, cameraFragment).hide(cameraFragment)
                .add(R.id.fragment_container, profileFragment).hide(profileFragment)
                .add(R.id.fragment_container, homeFragment)
                .commit()
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home     -> showFragment(homeFragment)
                R.id.nav_profile  -> showFragment(profileFragment)
                R.id.nav_camera   -> showFragment(cameraFragment)
                R.id.nav_settings -> showFragment(settingsFragment)
            }
            true
        }
    }

    private fun showFragment(target: Fragment) {
        supportFragmentManager.beginTransaction()
            .hide(homeFragment)
            .hide(profileFragment)
            .hide(cameraFragment)
            .hide(settingsFragment)
            .show(target)
            .commit()
    }

    override fun onBackPressed() {
        if (!homeFragment.isHidden && homeFragment.handleBackPress()) return
        if (supportFragmentManager.backStackEntryCount > 0) {
            super.onBackPressed()
            return
        }
        showExitDialog()
    }

    private fun showExitDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Exit App")
            .setMessage("Do you want to exit?")
            .setPositiveButton("Yes") { _, _ -> finish() }
            .setNegativeButton("No")  { dialog, _ -> dialog.dismiss() }
            .show()
    }
}