package com.apollo.thefragments

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.apollo.thefragments.fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    // ─────────────────────────────────────────────────────────────
    // WHY WE STORE INSTANCES HERE
    // ─────────────────────────────────────────────────────────────
    // Your original code used replace() which destroys the old fragment
    // and creates a brand new one every tab switch.
    //
    // The problem: HomeFragment uses childFragmentManager to hold the inner
    // stack (Step1 → Step2 → Step3). That childFragmentManager is OWNED by
    // the HomeFragment INSTANCE. If replace() destroys the instance, the
    // entire inner stack dies with it.
    //
    // The fix: we create each fragment ONCE and store the instance here.
    // On tab switch we use hide()/show() instead of replace() — the fragment
    // stays alive in memory, its childFragmentManager and inner stack intact.
    // ─────────────────────────────────────────────────────────────
    private val homeFragment     = HomeFragment()
    private val profileFragment  = ProfileFragment()
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
            // Add ALL three fragments to the container at startup.
            // Only homeFragment is visible — the other two are hidden.
            // This way all three instances exist in memory from the start.
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, settingsFragment).hide(settingsFragment)
                .add(R.id.fragment_container, profileFragment).hide(profileFragment)
                .add(R.id.fragment_container, homeFragment)   // visible by default
                .commit()
        }

        bottomNav.setOnItemSelectedListener { item ->
            // Instead of replace() (destroys fragment), we show() the selected one
            // and hide() all others. The fragments stay alive — no lifecycle destroy.
            //
            // What fires when you switch tabs with hide/show:
            //   Leaving fragment:  onPause() → onStop()         (NOT onDestroy)
            //   Arriving fragment: onStart() → onResume()       (NOT onCreate)
            //
            // This means the inner back stack in HomeFragment survives tab switches. ✅
            when (item.itemId) {
                R.id.nav_home     -> showFragment(homeFragment)
                R.id.nav_profile  -> showFragment(profileFragment)
                R.id.nav_settings -> showFragment(settingsFragment)
            }
            true
        }
    }

    private fun showFragment(target: Fragment) {
        supportFragmentManager.beginTransaction()
            .hide(homeFragment)
            .hide(profileFragment)
            .hide(settingsFragment)
            .show(target)
            .commit()
    }

    // ─────────────────────────────────────────────────────────────
    // BACK PRESS — checks in order:
    //   1. HomeFragment's inner stack (Step3 → Step2 → Step1 → welcome)
    //   2. Activity's own back stack
    //   3. Nothing left → show exit dialog
    // ─────────────────────────────────────────────────────────────
    override fun onBackPressed() {
        // Only delegate to HomeFragment if it's currently visible
        if (!homeFragment.isHidden && homeFragment.handleBackPress()) {
            // HomeFragment consumed the back press (popped Step3/2/1)
            return
        }

        if (supportFragmentManager.backStackEntryCount > 0) {
            super.onBackPressed()
            return
        }

        // Nothing left to pop — ask the user if they want to exit
        showExitDialog()
    }

    private fun showExitDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Exit App")
            .setMessage("Do you want to exit?")
            .setPositiveButton("Yes") { _, _ ->
                // finish() destroys the Activity → triggers onDestroy on all fragments → app closes
                finish()
            }
            .setNegativeButton("No") { dialog, _ ->
                // dismiss() just removes the dialog — nothing else changes
                dialog.dismiss()
            }
            .show()
    }
}
