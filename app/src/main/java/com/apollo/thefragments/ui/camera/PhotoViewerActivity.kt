package com.apollo.thefragments.ui.camera

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.apollo.thefragments.R
import com.apollo.thefragments.data.db.AppDatabase
import com.apollo.thefragments.data.model.Photo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PhotoViewerActivity : AppCompatActivity() {

    companion object {
        // Pass the clicked photo index so we open on the right page
        const val EXTRA_START_INDEX = "start_index"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.w("CameraViewer", "=== PhotoViewerActivity onCreate ===")
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_photo_viewer)
            Log.w("CameraViewer", "setContentView OK")
        } catch (e: Exception) {
            Log.w("CameraViewer", "CRASH at setContentView: ${e.message}", e)
            return
        }

        val startIndex = intent.getIntExtra(EXTRA_START_INDEX, 0)
        Log.w("CameraViewer", "startIndex: $startIndex")

        val viewPager    = findViewById<ViewPager2>(R.id.viewPagerPhotos)
        val btnClose     = findViewById<ImageButton>(R.id.btnClose)
        val tvPhotoIndex = findViewById<TextView>(R.id.tvPhotoIndex)

        btnClose.setOnClickListener { finish() }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.w("CameraViewer", "Querying Room for photos...")
                val photos = AppDatabase.getDatabase(applicationContext)
                    .photoDao()
                    .getAllPhotosOnce()
                Log.w("CameraViewer", "Room returned ${photos.size} photos")

                photos.forEachIndexed { i, p ->
                    Log.w("CameraViewer", "  Photo[$i] path=${p.localPath} exists=${File(p.localPath).exists()}")
                }

                withContext(Dispatchers.Main) {
                    if (photos.isEmpty()) {
                        Log.w("CameraViewer", "No photos — finishing")
                        finish()
                        return@withContext
                    }

                    Log.w("CameraViewer", "Setting adapter with ${photos.size} photos")
                    viewPager.adapter = PhotoViewerAdapter(photos)
                    viewPager.setCurrentItem(startIndex, false)
                    tvPhotoIndex.text = "${startIndex + 1} / ${photos.size}"
                    Log.w("CameraViewer", "ViewPager setup complete")

                    viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                        override fun onPageSelected(position: Int) {
                            Log.w("CameraViewer", "Swiped to page: $position")
                            tvPhotoIndex.text = "${position + 1} / ${photos.size}"
                        }
                    })
                }
            } catch (e: Exception) {
                Log.w("CameraViewer", "CRASH inside coroutine: ${e.message}", e)
            }
        }
    }
}