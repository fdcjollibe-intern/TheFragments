package com.apollo.thefragments.ui.camera

import android.animation.AnimatorInflater
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ProgressBar
import com.apollo.thefragments.R
import com.apollo.thefragments.data.model.Photo
import java.io.File

class PhotoGridAdapter(
    private val context: Context,
    private var photos: List<Photo>,
    private var uploadStates: Map<String, UploadState>
) : BaseAdapter() {

    override fun getCount()          = photos.size
    override fun getItem(pos: Int)   = photos[pos]
    override fun getItemId(pos: Int) = pos.toLong()

    fun update(newPhotos: List<Photo>, newStates: Map<String, UploadState>) {
        photos       = newPhotos
        uploadStates = newStates
        notifyDataSetChanged()
    }

    override fun getView(pos: Int, convertView: View?, parent: ViewGroup): View {
        // CrashTrace log for easy searching
        val photo = photos[pos]
        Log.w("CrashTrace", "PhotoGridAdapter.getView - pos=$pos, photoId=${photo.id}, localPath=${photo.localPath}, fileExists=${File(photo.localPath).exists()}")

        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_photo, parent, false)

        val ivPhoto     = view.findViewById<ImageView>(R.id.ivPhoto)
        val syncDot     = view.findViewById<View>(R.id.viewSyncDot)
        val dotProgress = view.findViewById<ProgressBar>(R.id.dotProgress)

        // Load thumbnail safely
        val bitmap = try {
            BitmapFactory.decodeFile(photo.localPath)
        } catch (e: Exception) {
            Log.w("CrashTrace", "Bitmap decode exception for path=${photo.localPath}: ${e.message}", e)
            null
        }

        if (bitmap == null) {
            Log.w("CrashTrace", "Bitmap is null or failed to decode for path=${photo.localPath}")
            // Clear image or set placeholder if you have one
            ivPhoto.setImageDrawable(null)
        } else {
            Log.w("CrashTrace", "Bitmap decoded for path=${photo.localPath} size=${bitmap.width}x${bitmap.height}")
            ivPhoto.setImageBitmap(bitmap)
        }

        // Determine state — Room isSynced is source of truth
        val state = when {
            photo.isSynced -> UploadState.SYNCED
            else -> uploadStates[photo.id] ?: UploadState.IDLE
        }

        // Clear any running animation before applying new state
        syncDot.animate().cancel()
        syncDot.alpha = 1f

        when (state) {
            UploadState.UPLOADING -> {
                // Hide dot, show spinner
                syncDot.visibility    = View.INVISIBLE
                dotProgress.visibility = View.VISIBLE
            }
            UploadState.SYNCED -> {
                // Green dot, no spinner
                dotProgress.visibility = View.GONE
                syncDot.visibility     = View.VISIBLE
                syncDot.setBackgroundResource(R.drawable.bg_dot_synced)
            }
            UploadState.ERROR -> {
                // Red dot, pulsing to draw attention
                dotProgress.visibility = View.GONE
                syncDot.visibility     = View.VISIBLE
                syncDot.setBackgroundResource(R.drawable.bg_dot_unsynced)
                AnimatorInflater.loadAnimator(context, R.animator.anim_pulse)
                    .apply { setTarget(syncDot); start() }
            }
            UploadState.IDLE -> {
                // Red dot, static
                dotProgress.visibility = View.GONE
                syncDot.visibility     = View.VISIBLE
                syncDot.setBackgroundResource(R.drawable.bg_dot_unsynced)
            }
        }

        // Tap photo → open full screen viewer at this position
        ivPhoto.setOnClickListener {
            Log.w("CameraViewer", "=== PHOTO TAPPED ===")
            Log.w("CameraViewer", "Position: $pos")
            Log.w("CameraViewer", "Photo id: ${photo.id}")
            Log.w("CameraViewer", "Local path: ${photo.localPath}")
            Log.w("CameraViewer", "File exists: ${File(photo.localPath).exists()}")
            Log.w("CameraViewer", "Context type: ${context.javaClass.simpleName}")

            // Additional CrashTrace entry to help grep a large logcat
            Log.w("CrashTrace", "PhotoGridAdapter.onClick - pos=$pos, id=${photo.id}, path=${photo.localPath}")

            try {
                Log.w("CameraViewer", "Building intent...")
                val intent = Intent(context, PhotoViewerActivity::class.java).apply {
                    putExtra(PhotoViewerActivity.EXTRA_START_INDEX, pos)
                }
                Log.w("CameraViewer", "Intent built successfully, launching...")
                Log.w("CrashTrace", "Launching PhotoViewerActivity intent for pos=$pos")
                context.startActivity(intent)
                Log.w("CameraViewer", "startActivity called OK")
            } catch (e: ActivityNotFoundException) {
                Log.w("CrashTrace", "ActivityNotFoundException launching PhotoViewerActivity: ${e.message}", e)
                Log.w("CameraViewer", "ActivityNotFoundException: ${e.message}", e)
            } catch (e: Exception) {
                Log.w("CrashTrace", "Exception launching PhotoViewerActivity: ${e.message}", e)
                Log.w("CameraViewer", "CRASH launching viewer: ${e.message}", e)
            }
        }

        return view
    }
}