package com.example.storyapp.view.camera

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.preferencesDataStore
import com.example.storyapp.api.ApiConfig
import com.example.storyapp.data.UserPreference
import com.example.storyapp.data.UserRepository
import com.example.storyapp.loginwithanimation.databinding.ActivityCameraMainBinding
import com.example.storyapp.view.main.ResultState
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

private val Context.dataStore by preferencesDataStore(name = "settings")

class CameraMain : AppCompatActivity() {

    private lateinit var binding: ActivityCameraMainBinding
    private var currentImageUri: Uri? = null

    // Launcher untuk mengambil foto dari kamera
    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        } else {
            showToast("Failed to capture photo")
        }
    }

    // Launcher untuk memilih gambar dari galeri
    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Menerima Intent
        val message = intent.getStringExtra("EXTRA_MESSAGE")
        binding.textViewMessage.text = Editable.Factory.getInstance().newEditable(
            message ?: "No message received"
        )

        binding.galleryButton.setOnClickListener { startGallery() }
        binding.cameraButton.setOnClickListener { startCamera() }
        binding.uploadButton.setOnClickListener { uploadStory() }

        // Meminta izin saat aplikasi dibuka pertama kali
        if (!allPermissionsGranted()) {
            requestPermissions()
        }
    }

    private fun startGallery() {
        if (allPermissionsGranted()) {
            launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            requestPermissions()
        }
    }

    private fun startCamera() {
        if (allPermissionsGranted()) {
            currentImageUri = getImageUri()
            currentImageUri?.let { launcherIntentCamera.launch(it) }
        } else {
            requestPermissions()
        }
    }

    private fun showImage() {
        binding.progressIndicator.visibility = View.VISIBLE
        currentImageUri?.let {
            binding.previewImageView.setImageURI(it)
            binding.progressIndicator.visibility = View.GONE
        }
    }

    private fun uploadStory() {
        val description = binding.textViewMessage.text.toString().trim()
        if (currentImageUri == null || description.isEmpty()) {
            showToast("Please provide a valid image and description.")
            return
        }

        // Ambil token dari sesi pengguna
        val userRepository = UserRepository.getInstance(
            ApiConfig.getApiService(),
            UserPreference.getInstance(dataStore)
        )
        val token = runBlocking {
            userRepository.getSession().firstOrNull()?.token
        } ?: run {
            showToast("Token is not available.")
            return
        }

        // Ambil file dari URI
        val imageFile = currentImageUri?.let { uri ->
            contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                val file = File(cacheDir, "temp_image.jpg")
                FileOutputStream(file).use { output ->
                    FileInputStream(descriptor.fileDescriptor).use { input ->
                        input.copyTo(output)
                    }
                }
                file
            }
        }

        if (imageFile == null) {
            showToast("Failed to process the selected image.")
            return
        }

        // Validasi ukuran file (maksimal 1 MB)
        if (imageFile.length() > 1_048_576) { // 1 MB = 1,048,576 bytes
            showToast("File size must be less than 1 MB.")
            return
        }

        // Upload melalui UserRepository
        userRepository.uploadStory(token, imageFile, description).observe(this) { resultState ->
            when (resultState) {
                is ResultState.Loading -> {
                    showToast("Uploading...")
                }
                is ResultState.Success -> {
                    showToast("Upload successful: ${resultState.data.message}")
                    // Simpan cerita ke SharedPreferences
                    currentImageUri?.let { uri ->
                        saveUploadedStory(description, uri)
                    }
                    finish() // Kembali ke halaman sebelumnya setelah berhasil upload
                }
                is ResultState.Error -> {
                    showToast("Failed to upload story: ${resultState.error}")
                }
            }
        }
    }

    private fun saveUploadedStory(description: String, imageUri: Uri) {
        val sharedPreferences = getSharedPreferences("uploaded_stories", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val timestamp = System.currentTimeMillis()
        val storyData = "$description|$imageUri|$timestamp"
        editor.putString("last_uploaded_story", storyData)
        editor.apply()
    }

    private fun getImageUri(): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "photo_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/StoryApp")
            }
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    private fun allPermissionsGranted(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                showToast("Permissions not granted")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }
}
