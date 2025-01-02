package com.example.storyapp.view.signup

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.storyapp.loginwithanimation.R
import com.example.storyapp.loginwithanimation.databinding.ActivitySignupBinding
import com.example.storyapp.view.login.CustomButton
import com.example.storyapp.view.login.CustomEditTextEmail
import com.example.storyapp.view.login.CustomEditTextName
import com.example.storyapp.view.login.CustomEditTextPassword
import com.example.storyapp.di.ResultState
import com.example.storyapp.view.main.ViewModelFactory
import com.example.storyapp.view.welcome.WelcomeActivity

class SignupActivity : AppCompatActivity() {
    private lateinit var nameText: CustomEditTextName
    private lateinit var emailText: CustomEditTextEmail
    private lateinit var passwordText: CustomEditTextPassword
    private lateinit var myButton: CustomButton
    private lateinit var binding: ActivitySignupBinding
    private val viewModel: SignupViewModel by viewModels { ViewModelFactory.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        nameText = binding.nameEditText
        emailText = binding.emailEditText
        passwordText = binding.passwordEditText
        myButton = binding.signupButton

        emailText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setButton()
            }
            override fun afterTextChanged(s: Editable) {}
        })

        passwordText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setButton()
            }
            override fun afterTextChanged(s: Editable) {}
        })

        setupView()
        playAnimation()

        myButton.setOnClickListener { register() }
    }

    private fun register() {
        val name = nameText.text.toString()
        val email = emailText.text.toString()
        val password = passwordText.text.toString()

        viewModel.signup(name, email, password)

        viewModel.registerResult.observe(this) { resultState ->
            when (resultState) {
                is ResultState.Loading -> {
                    showLoading(true)
                }
                is ResultState.Success -> {
                    setTitle(getString(R.string.login_success_title))
                    showToast("Akun dengan $email sudah jadi nih. Selamat bersenang-senang!")
                    showLoading(false)
                    val intent = Intent(this@SignupActivity, WelcomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
                is ResultState.Error -> {
                    showToast(resultState.error)
                    showLoading(false)
                }
            }
        }
    }

    private fun setupView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
        supportActionBar?.hide()
    }

    private fun setButton() {
        val nameResult = nameText.text.toString().isNotEmpty() && nameText.error == null
        val emailResult = emailText.text.toString().isNotEmpty() && emailText.error == null
        val passwordResult = passwordText.text.toString().isNotEmpty() && passwordText.error == null
        myButton.isEnabled = nameResult && emailResult && passwordResult
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val title = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1f).setDuration(100)
        val nameTextView = ObjectAnimator.ofFloat(binding.nameTextView, View.ALPHA, 1f).setDuration(100)
        val nameEditTextLayout = ObjectAnimator.ofFloat(binding.nameEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val emailTextView = ObjectAnimator.ofFloat(binding.emailTextView, View.ALPHA, 1f).setDuration(100)
        val emailEditTextLayout = ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val passwordTextView = ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 1f).setDuration(100)
        val passwordEditTextLayout = ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val signup = ObjectAnimator.ofFloat(binding.signupButton, View.ALPHA, 1f).setDuration(100)

        AnimatorSet().apply {
            playSequentially(
                title,
                nameTextView,
                nameEditTextLayout,
                emailTextView,
                emailEditTextLayout,
                passwordTextView,
                passwordEditTextLayout,
                signup
            )
            startDelay = 100
        }.start()
    }
}
