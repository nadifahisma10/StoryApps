package com.example.storyapp.view.main

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storyapp.loginwithanimation.databinding.ActivityMainBinding
import com.example.storyapp.view.camera.CameraMain
import com.example.storyapp.view.welcome.WelcomeActivity

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityMainBinding
    private var storyAdapter: StoryAdapter = StoryAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi storyAdapter terlebih dahulu
        storyAdapter = StoryAdapter()

        setupView()
        setupAction()
        playAnimation()

        // Mengamati loading state
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Mengamati pesan error
        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupView() {

        binding.recyclerViewStories.apply {
            adapter = storyAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                // Jika user belum login, arahkan ke WelcomeActivity
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            } else {
                // Panggil getStories dengan token
                viewModel.getStories(token = "Bearer ${user.token}").observe(this) { result ->
                    when (result) {
                        is ResultActivity.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is ResultActivity.Success -> {
                            binding.progressBar.visibility = View.GONE
                            if (result.data.error == true) {
                                Toast.makeText(this, "Error: ${result.data.message}", Toast.LENGTH_SHORT).show()
                            } else {
                                storyAdapter.submitList(result.data.listStory)
                            }
                        }
                        is ResultActivity.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(this, "Failed to load stories: ${result.error}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupAction() {
        binding.fabAddStory.setOnClickListener {
            val intent = Intent(this, CameraMain::class.java)
            startActivity(intent)
        }

        binding.logoutButton.setOnClickListener {
            viewModel.logout()
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val name = ObjectAnimator.ofFloat(binding.nameTextView, View.ALPHA, 1f).setDuration(100)
        val message = ObjectAnimator.ofFloat(binding.messageTextView, View.ALPHA, 1f).setDuration(100)
        val logout = ObjectAnimator.ofFloat(binding.logoutButton, View.ALPHA, 1f).setDuration(100)

        AnimatorSet().apply {
            playSequentially(name, message, logout)
            startDelay = 100
        }.start()
    }
}
