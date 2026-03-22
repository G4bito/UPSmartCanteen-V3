package com.yutahnahsyah.upsmartcanteen.auth

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import android.view.View
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.yutahnahsyah.upsmartcanteen.BaseActivity
import com.yutahnahsyah.upsmartcanteen.R
import com.yutahnahsyah.upsmartcanteen.ResetPasswordRequest
import com.yutahnahsyah.upsmartcanteen.RetrofitClient
import kotlinx.coroutines.launch
import org.json.JSONObject

class ResetPasswordActivity : BaseActivity() {

  private var isNewPasswordVisible = false
  private var isConfirmPasswordVisible = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_reset_password)

    val resetToken = intent.getStringExtra("resetToken") ?: ""

    val etNewPassword = findViewById<EditText>(R.id.etNewPassword)
    val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
    val btnToggleNew = findViewById<ImageButton>(R.id.btnToggleNewPassword)
    val btnToggleConfirm = findViewById<ImageButton>(R.id.btnToggleConfirmPassword)
    val btnResetPassword = findViewById<MaterialButton>(R.id.btnResetPassword)
    val btnBackToLogin = findViewById<MaterialButton>(R.id.btnBackToLogin)
    val btnBack = findViewById<CardView>(R.id.btnBack)
    val progressBar = findViewById<ProgressBar>(R.id.progressBar)

    btnToggleNew.setOnClickListener {
      isNewPasswordVisible = !isNewPasswordVisible
      etNewPassword.transformationMethod = if (isNewPasswordVisible)
        HideReturnsTransformationMethod.getInstance()
      else
        PasswordTransformationMethod.getInstance()
      btnToggleNew.setImageResource(
        if (isNewPasswordVisible) R.drawable.ic_eye_on else R.drawable.ic_eye_off
      )
      etNewPassword.setSelection(etNewPassword.text.length)
    }

    btnToggleConfirm.setOnClickListener {
      isConfirmPasswordVisible = !isConfirmPasswordVisible
      etConfirmPassword.transformationMethod = if (isConfirmPasswordVisible)
        HideReturnsTransformationMethod.getInstance()
      else
        PasswordTransformationMethod.getInstance()
      btnToggleConfirm.setImageResource(
        if (isConfirmPasswordVisible) R.drawable.ic_eye_on else R.drawable.ic_eye_off
      )
      etConfirmPassword.setSelection(etConfirmPassword.text.length)
    }

    btnBack.setOnClickListener { finish() }

    btnBackToLogin.setOnClickListener {
      val intent = Intent(this, Login::class.java)
      intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
      startActivity(intent)
    }

    btnResetPassword.setOnClickListener {
      val newPassword = etNewPassword.text.toString().trim()
      val confirmPassword = etConfirmPassword.text.toString().trim()

      if (newPassword.isEmpty()) {
        etNewPassword.error = "Please enter a new password"
        return@setOnClickListener
      }
      if (newPassword.length < 8) {
        etNewPassword.error = "Minimum 8 characters"
        return@setOnClickListener
      }
      if (confirmPassword.isEmpty()) {
        etConfirmPassword.error = "Please confirm your password"
        return@setOnClickListener
      }
      if (newPassword != confirmPassword) {
        etConfirmPassword.error = "Passwords do not match"
        return@setOnClickListener
      }

      resetPassword(resetToken, newPassword, progressBar, btnResetPassword)
    }
  }

  private fun resetPassword(
    resetToken: String,
    newPassword: String,
    progressBar: ProgressBar,
    btn: MaterialButton
  ) {
    progressBar.visibility = View.VISIBLE
    btn.isEnabled = false

    lifecycleScope.launch {
      try {
        val res = RetrofitClient.instance.resetPassword(
          ResetPasswordRequest(resetToken, newPassword)
        )

        progressBar.visibility = View.GONE
        btn.isEnabled = true

        if (res.isSuccessful) {
          Toast.makeText(
            this@ResetPasswordActivity,
            "Password reset successful! Please log in.",
            Toast.LENGTH_LONG
          ).show()
          val intent = Intent(this@ResetPasswordActivity, Login::class.java)
          intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
          startActivity(intent)
        } else {
          val error = JSONObject(
            res.errorBody()?.string() ?: "{}"
          ).optString("message", "Something went wrong")
          Toast.makeText(this@ResetPasswordActivity, error, Toast.LENGTH_SHORT).show()
        }

      } catch (e: Exception) {
        progressBar.visibility = View.GONE
        btn.isEnabled = true
        Toast.makeText(
          this@ResetPasswordActivity,
          "Network error: ${e.message}",
          Toast.LENGTH_SHORT
        ).show()
      }
    }
  }
}