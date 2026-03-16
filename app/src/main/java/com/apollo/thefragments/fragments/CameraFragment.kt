package com.apollo.thefragments.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.apollo.thefragments.R
import com.apollo.thefragments.data.db.AppDatabase
import com.apollo.thefragments.data.model.Photo
import com.apollo.thefragments.repository.PhotoRepository
import com.apollo.thefragments.ui.camera.CameraViewModel
import com.apollo.thefragments.ui.camera.CameraViewModelFactory
import com.apollo.thefragments.ui.camera.PhotoGridAdapter
import com.apollo.thefragments.ui.camera.UploadState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CameraFragment : Fragment() {

    // ── Views ──────────────────────────────────────────────────────────────────
    private lateinit var cardOpenCamera: CardView
    private lateinit var tvPhotoCount: TextView
    private lateinit var tvConnectivity: TextView
    private lateinit var gridPhotos: GridView

    // ── ViewModel ──────────────────────────────────────────────────────────────
    private lateinit var viewModel: CameraViewModel

    // ── State ──────────────────────────────────────────────────────────────────
    private var pendingPhotoUri: Uri? = null

    private var pendingPhotoFile: File? = null
    private var permissionDenyCount  = 0
    private var isOnline             = false

    // ── Connectivity callback ──────────────────────────────────────────────────
    private lateinit var connectivityManager: ConnectivityManager
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            // Use activity? and only update UI if fragment is added
            activity?.runOnUiThread {
                if (isAdded) {
                    updateConnectivityUI(true)
                } else {
                    Log.w("CrashTrace", "networkCallback.onAvailable - fragment not added, skipping updateConnectivityUI(true)")
                }
            }
        }
        override fun onLost(network: Network) {
            activity?.runOnUiThread {
                if (isAdded) {
                    updateConnectivityUI(false)
                } else {
                    Log.w("CrashTrace", "networkCallback.onLost - fragment not added, skipping updateConnectivityUI(false)")
                }
            }
        }
    }

    // ── Permission launcher ────────────────────────────────────────────────────
    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                launchCamera()
            } else {
                permissionDenyCount++
                handlePermissionDenied()
            }
        }

    // ── Camera launcher ────────────────────────────────────────────────────────
    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { photoSaved ->
            if (photoSaved) {
                // ✅ Use pendingPhotoFile.absolutePath — this is the FULL correct path
                val file = pendingPhotoFile ?: return@registerForActivityResult
                val id   = file.nameWithoutExtension
                val photo = Photo(
                    id        = id,
                    localPath = file.absolutePath,  // e.g. /data/user/0/com.apollo.thefragments/files/camera_photos/PHOTO_xxx.jpg
                    isSynced  = false
                )
                viewModel.insertPhoto(photo)
            }
        }

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_camera, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.let {
            pendingPhotoUri = it.getParcelable("pendingPhotoUri")
            pendingPhotoFile = it.getString("pendingPhotoPath")?.let { path -> File(path) }
        }

        // ── ViewModel setup
        val db      = AppDatabase.getDatabase(requireContext())
        val repo    = PhotoRepository(db.photoDao())
        val factory = CameraViewModelFactory(repo)
        viewModel   = ViewModelProvider(this, factory)[CameraViewModel::class.java]

        // ── Bind views
        cardOpenCamera  = view.findViewById(R.id.cardOpenCamera)
        tvPhotoCount    = view.findViewById(R.id.tvPhotoCount)
        tvConnectivity  = view.findViewById(R.id.tvConnectivity)
        gridPhotos      = view.findViewById(R.id.gridPhotos)
        val fabSync  = view.findViewById<View>(R.id.fabSync)

        // FAB click — sync all unsynced photos
        fabSync.setOnClickListener {
            if (!isOnline) {
                Toast.makeText(requireContext(),
                    "You're offline. Connect to sync.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val unsynced = viewModel.photos.value.filter { !it.isSynced }
            if (unsynced.isEmpty()) {
                Toast.makeText(requireContext(),
                    "All photos are already synced!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            unsynced.forEach { viewModel.syncPhoto(it) }
        }


        cardOpenCamera.setOnClickListener { checkCameraPermission() }

        // ── Connectivity setup
        connectivityManager =
            requireContext().getSystemService(ConnectivityManager::class.java)
        val initialOnline = connectivityManager
            .getNetworkCapabilities(connectivityManager.activeNetwork)
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        updateConnectivityUI(initialOnline)

        // ── Observe local photos (Room Flow → StateFlow)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.photos.collect { photos ->
                val count = photos.size
                tvPhotoCount.text = when (count) {
                    0    -> "Local Photos — none yet"
                    1    -> "Local Photos — 1 photo"
                    else -> "Local Photos — $count photos"
                }
                refreshGrid(photos, viewModel.uploadStates.value ?: emptyMap())
            }
        }

        // ── Observe upload states (to refresh button labels)
        viewModel.uploadStates.observe(viewLifecycleOwner) { states ->
            refreshGrid(viewModel.photos.value, states)
        }

        // ── Observe toast messages
        viewModel.message.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                viewModel.clearMessage()
            }
        }

        // ── Fetch cloud photos if online (for cross-device display later)
        if (isOnline) viewModel.fetchCloudPhotos()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("pendingPhotoUri", pendingPhotoUri)
        outState.putString("pendingPhotoPath", pendingPhotoFile?.absolutePath)  // ADD
    }

    override fun onResume() {
        super.onResume()
        // Lock portrait to prevent mid-camera orientation restart
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Start watching connectivity
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    override fun onPause() {
        super.onPause()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        // Always unregister to avoid leaks
        try { connectivityManager.unregisterNetworkCallback(networkCallback) } catch (_: Exception) {}
    }

    // ── Connectivity UI ────────────────────────────────────────────────────────

    private fun updateConnectivityUI(online: Boolean) {
        // Defensive guard: ensure fragment is added and views are initialized before touching UI
        if (!isAdded || !this::tvConnectivity.isInitialized) {
            Log.w("CrashTrace", "updateConnectivityUI skipped - fragment not added or tvConnectivity not initialized")
            isOnline = online
            return
        }

        isOnline = online
        tvConnectivity.text = if (online) "● Online" else "● Offline"
        tvConnectivity.setBackgroundResource(
            if (online) R.drawable.bg_badge_online else R.drawable.bg_badge_offline
        )
        // Refresh grid so sync buttons enable/disable correctly
        refreshGrid(viewModel.photos.value, viewModel.uploadStates.value ?: emptyMap())
    }

    // ── Grid refresh ───────────────────────────────────────────────────────────

    private fun refreshGrid(photos: List<Photo>, states: Map<String, UploadState>) {
        // Defensive: avoid touching views or calling requireContext when fragment isn't attached
        if (!isAdded || !this::gridPhotos.isInitialized || context == null) {
            Log.w("CrashTrace", "refreshGrid skipped - fragment not added or gridPhotos not initialized")
            return
        }

        gridPhotos.adapter = PhotoGridAdapter(
            context      = requireContext(),
            photos       = photos,
            uploadStates = states,
        )
    }

    // ── Permission Logic ───────────────────────────────────────────────────────

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> launchCamera()

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) ->
                showRationaleDialog()

            else -> {
                if (permissionDenyCount >= 2) showPermanentlyDeniedDialog()
                else requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun handlePermissionDenied() {
        when {
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) ->
                showRationaleDialog()
            permissionDenyCount >= 2 ->
                showPermanentlyDeniedDialog()
        }
    }

    // Attempt 2 — explain why, then ask again
    private fun showRationaleDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Camera Access Needed")
            .setMessage("This app needs camera access to take photos.\nPlease grant the permission so you can capture images.")
            .setPositiveButton("Ask Again") { _, _ ->
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
            .setNegativeButton("Not Now") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    // Attempt 3 — permanently denied, send to Settings
    private fun showPermanentlyDeniedDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Permission Permanently Denied")
            .setMessage("Camera access was permanently denied.\n\nGo to App Settings → Permissions → Camera and turn it on.")
            .setPositiveButton("Open Settings") { _, _ ->
                startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", requireContext().packageName, null)
                    }
                )
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    // ── Camera Launch ──────────────────────────────────────────────────────────

    private fun launchCamera() {
        val photoFile = createPhotoFile()
        pendingPhotoFile = photoFile  // ADD THIS — save the File before launching

        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )
        pendingPhotoUri = uri
        takePicture.launch(uri)
    }

    private fun createPhotoFile(): File {
        val dir = File(requireContext().filesDir, "camera_photos").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File(dir, "PHOTO_$timestamp.jpg")
    }
}