package com.apollo.thefragments.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.apollo.thefragments.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CameraFragment : Fragment() {

    // ─── Views ────────────────────────────────────────────────────────────────
    private lateinit var cardOpenCamera: CardView
    private lateinit var tvPhotoCount: TextView
    private lateinit var gridPhotos: GridView

    // ─── State ────────────────────────────────────────────────────────────────

    // The URI we prepared for the camera app to write the photo into.
    // We store it as a property because the camera result callback needs it.
    // It survives orientation changes via onSaveInstanceState.
    private var pendingPhotoUri: Uri? = null

    // How many times the user has denied the permission in this session.
    // Used to decide which "attempt" dialog to show.
    private var permissionDenyCount = 0

    // ─── Modern Permission API ────────────────────────────────────────────────
    //
    // registerForActivityResult() is the modern replacement for
    // onRequestPermissionsResult() + requestPermissions().
    //
    // RequestPermission → asks for ONE permission and gives back a Boolean:
    //   true  = user granted
    //   false = user denied (or permanently denied)
    //
    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                launchCamera()
            } else {
                permissionDenyCount++
                handlePermissionDenied()
            }
        }

    // ─── Modern Camera Launch API ─────────────────────────────────────────────
    //
    // TakePicture → launches the system camera and returns true if the
    // photo was saved successfully to the URI we provided.
    //
    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { photoSaved ->
            if (photoSaved) {
                // The camera wrote the photo to pendingPhotoUri — refresh gallery
                refreshGallery()
            }
            // If false, user cancelled — do nothing
        }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Restore the pending URI if the fragment was recreated
        // (e.g. after coming back from the camera app)
        savedInstanceState?.let {
            pendingPhotoUri = it.getParcelable("pendingPhotoUri")
        }

        cardOpenCamera = view.findViewById(R.id.cardOpenCamera)
        tvPhotoCount   = view.findViewById(R.id.tvPhotoCount)
        gridPhotos     = view.findViewById(R.id.gridPhotos)

        cardOpenCamera.setOnClickListener { checkCameraPermission() }

        refreshGallery()
    }

    // Save the pending URI across config changes (even though portrait is locked,
    // this is good practice so it's safe if the user changes it later).
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("pendingPhotoUri", pendingPhotoUri)
    }

    // Lock to portrait when this fragment is visible.
    // This prevents the mid-camera orientation restart issue.
    override fun onResume() {
        super.onResume()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    // Restore free rotation when leaving this fragment.
    override fun onPause() {
        super.onPause()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    // ─── Permission Logic ─────────────────────────────────────────────────────

    private fun checkCameraPermission() {
        when {
            // Case 1: Already granted → go straight to camera
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
            }

            // Case 2: Should show rationale.
            // Android sets this to true after the user denies once,
            // and sets it back to false after permanent denial.
            // We use this + our deny count to craft the right message.
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                showRationaleDialog()
            }

            // Case 3: Either first-time ask, or permanently denied.
            // We use permissionDenyCount to tell them apart.
            else -> {
                if (permissionDenyCount >= 2) {
                    // User has denied enough times → likely permanently denied
                    showPermanentlyDeniedDialog()
                } else {
                    // First time asking
                    requestCameraPermission.launch(Manifest.permission.CAMERA)
                }
            }
        }
    }

    // ── Attempt 1: First ask — no dialog, just the system prompt ─────────────
    // (handled directly in checkCameraPermission's else-branch above)

    // ── Attempt 2: Rationale — explain why, then ask again ───────────────────
    private fun showRationaleDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Camera Access Needed")
            .setMessage(
                "This app needs camera access to take photos.\n\n" +
                "Please grant the permission so you can capture images."
            )
            .setPositiveButton("Ask Again") { _, _ ->
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
            .setNegativeButton("Not Now") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // ── Attempt 3: Permanently denied — send to App Settings ─────────────────
    private fun showPermanentlyDeniedDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Permission Permanently Denied")
            .setMessage(
                "Camera access was permanently denied.\n\n" +
                "To enable it, go to App Settings → Permissions → Camera and turn it on."
            )
            .setPositiveButton("Open Settings") { _, _ ->
                // Deep link to this app's permission settings page
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", requireContext().packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // Called in the requestCameraPermission callback when denied
    private fun handlePermissionDenied() {
        when {
            // shouldShowRequestPermissionRationale is true = denied once, not permanently
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                showRationaleDialog()
            }
            permissionDenyCount >= 2 -> {
                // No rationale shown + denied multiple times = permanently denied
                showPermanentlyDeniedDialog()
            }
        }
    }

    // ─── Camera Launch ────────────────────────────────────────────────────────

    private fun launchCamera() {
        // Create a private file for the photo with a timestamp name
        val photoFile = createPhotoFile()

        // Wrap the private file path in a content:// URI using FileProvider.
        // The camera app MUST receive a content URI (not a file path) for security.
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )

        // Save the URI so the result callback can reference it
        pendingPhotoUri = uri

        // Launch the system camera — it will write the photo to `uri`
        takePicture.launch(uri)
    }

    private fun createPhotoFile(): File {
        // Use the app's private files directory → no gallery access needed
        val dir = File(requireContext().filesDir, "camera_photos").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File(dir, "PHOTO_$timestamp.jpg")
    }

    // ─── Gallery ──────────────────────────────────────────────────────────────

    private fun refreshGallery() {
        val dir = File(requireContext().filesDir, "camera_photos")
        // Get all .jpg files, newest first
        val photos = dir.listFiles { f -> f.extension == "jpg" }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()

        // Update count label
        tvPhotoCount.text = when (photos.size) {
            0    -> "No photos yet"
            1    -> "1 photo"
            else -> "${photos.size} photos"
        }

        // Hook up the grid adapter
        gridPhotos.adapter = PhotoGridAdapter(requireContext(), photos)
    }

    // ─── Grid Adapter ─────────────────────────────────────────────────────────

    private class PhotoGridAdapter(
        private val context: Context,
        private val photos: List<File>
    ) : BaseAdapter() {

        override fun getCount()                  = photos.size
        override fun getItem(pos: Int)            = photos[pos]
        override fun getItemId(pos: Int)          = pos.toLong()

        override fun getView(pos: Int, convertView: View?, parent: ViewGroup): View {
            val imageView = (convertView as? ImageView) ?: ImageView(context).apply {
                // Each cell is square: match column width, fixed height
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    300   // px — GridView stretches columns, height stays fixed
                )
                scaleType   = ImageView.ScaleType.CENTER_CROP
            }

            // Decode the photo file into the ImageView.
            // BitmapFactory is fine for small grids.
            // For large collections, swap this out for Glide/Coil later.
            val bitmap = android.graphics.BitmapFactory.decodeFile(photos[pos].absolutePath)
            imageView.setImageBitmap(bitmap)

            return imageView
        }
    }
}