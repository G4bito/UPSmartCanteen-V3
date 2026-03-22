package com.yutahnahsyah.upsmartcanteen.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.yutahnahsyah.upsmartcanteen.BaseActivity
import com.yutahnahsyah.upsmartcanteen.ForgotPasswordRequest
import com.yutahnahsyah.upsmartcanteen.R
import com.yutahnahsyah.upsmartcanteen.RetrofitClient
import kotlinx.coroutines.launch
import org.json.JSONObject

class ForgotPasswordActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_forgot_password)

    val etEmail = findViewById<EditText>(R.id.etEmail)
    val btnSendReset = findViewById<MaterialButton>(R.id.btnSendReset)
    val btnBackToLogin = findViewById<MaterialButton>(R.id.btnBackToLogin)
    val btnBack = findViewById<View>(R.id.btnBack)
    val progressBar = findViewById<ProgressBar>(R.id.progressBar)

    btnSendReset.setOnClickListener {
      val email = etEmail.text.toString().trim()
      if (email.isEmpty()) {
        etEmail.error = "Please enter your email"
        return@setOnClickListener
      }
      sendResetLink(email, progressBar, btnSendReset)
    }

    btnBackToLogin.setOnClickListener { finish() }
    btnBack.setOnClickListener { finish() }
  }

  private fun sendResetLink(email: String, loader: ProgressBar, btn: MaterialButton) {
    loader.visibility = View.VISIBLE
    btn.isEnabled = false

    lifecycleScope.launch {
      try {
        val res = RetrofitClient.instance.forgotPassword(ForgotPasswordRequest(email))

        loader.visibility = View.GONE
        btn.isEnabled = true

        if (res.isSuccessful) {
          Toast.makeText(
            this@ForgotPasswordActivity,
            "OTP sent to $email",
            Toast.LENGTH_SHORT
          ).show()
          val intent = Intent(this@ForgotPasswordActivity, VerifyResetCodeActivity::class.java)
          intent.putExtra("email", email)
          startActivity(intent)
          finish()
        } else {
          val error = JSONObject(res.errorBody()?.string() ?: "{}").optString("message", "Something went wrong")
          Toast.makeText(this@ForgotPasswordActivity, error, Toast.LENGTH_SHORT).show()
        }

      } catch (e: Exception) {
        loader.visibility = View.GONE
        btn.isEnabled = true
        Toast.makeText(this@ForgotPasswordActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
      }
    }
  }
}