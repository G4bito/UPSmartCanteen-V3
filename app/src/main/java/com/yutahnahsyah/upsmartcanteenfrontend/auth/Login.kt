package com.yutahnahsyah.upsmartcanteenfrontend.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.yutahnahsyah.upsmartcanteenfrontend.BaseActivity
import com.yutahnahsyah.upsmartcanteenfrontend.MainActivity
import com.yutahnahsyah.upsmartcanteenfrontend.R
import com.yutahnahsyah.upsmartcanteenfrontend.RetrofitClient
import com.yutahnahsyah.upsmartcanteenfrontend.UserRequest
import kotlinx.coroutines.launch

class Login : BaseActivity() {

  private var isPasswordVisible = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)

    val emailField = findViewById<EditText>(R.id.emailEditText)
    val passwordField = findViewById<EditText>(R.id.passwordEditText)
    val loginBtn = findViewById<Button>(R.id.loginButton)
    val registerBtn = findViewById<Button>(R.id.registerButton)
    val forgotPasswordBtn = findViewById<TextView>(R.id.forgotPassword)
    val togglePasswordBtn = findViewById<ImageButton>(R.id.togglePasswordVisibility)
    val progressBar =
      findViewById<ProgressBar>(R.id.progressBar)

    togglePasswordBtn.setOnClickListener {
      isPasswordVisible = !isPasswordVisible
      if (isPasswordVisible) {
        passwordField.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        togglePasswordBtn.setImageResource(R.drawable.ic_eye_on)
      } else {
        passwordField.inputType =
          InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        togglePasswordBtn.setImageResource(R.drawable.ic_eye_off)
      }
      passwordField.setSelection(passwordField.text.length)
    }

    loginBtn.setOnClickListener {
      val email = emailField.text.toString().trim()
      val password = passwordField.text.toString().trim()

      if (email.isNotEmpty() && password.isNotEmpty()) {
        performLogin(email, password, progressBar)
      } else {
        Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
      }
    }

    registerBtn.setOnClickListener {
      val intent = Intent(this@Login, CreateAccountActivity::class.java)
      startActivity(intent)
    }

    forgotPasswordBtn.setOnClickListener {
      val intent = Intent(this@Login, ForgotPasswordActivity::class.java)
      startActivity(intent)
    }
  }

  private fun performLogin(user: String, pass: String, loader: ProgressBar) {
    val loginData = UserRequest(user, pass)
    loader.visibility = View.VISIBLE

    lifecycleScope.launch {
      try {
        val response = RetrofitClient.instance.loginUser(loginData)
        loader.visibility = View.GONE

        if (response.isSuccessful) {
          val token = response.body()?.token

          if (token != null) {
            val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
              putString("auth_token", token)
              apply()
            }

            val intent = Intent(this@Login, MainActivity::class.java)
            startActivity(intent)
            finish()
          }
        } else {
          val errorMsg = "Invalid Credentials"
          Toast.makeText(this@Login, errorMsg, Toast.LENGTH_SHORT).show()
        }
      } catch (e: Exception) {
        loader.visibility = View.GONE
        Toast.makeText(this@Login, "Connection Failed: ${e.message}", Toast.LENGTH_SHORT).show()
      }
    }
  }
}