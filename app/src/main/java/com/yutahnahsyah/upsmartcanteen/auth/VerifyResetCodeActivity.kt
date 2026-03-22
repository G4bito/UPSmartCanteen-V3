package com.yutahnahsyah.upsmartcanteen.auth

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.yutahnahsyah.upsmartcanteen.BaseActivity
import com.yutahnahsyah.upsmartcanteen.ForgotPasswordRequest
import com.yutahnahsyah.upsmartcanteen.VerifyOtpRequest
import com.yutahnahsyah.upsmartcanteen.R
import com.yutahnahsyah.upsmartcanteen.RetrofitClient
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Locale

class VerifyResetCodeActivity : BaseActivity() {

  private lateinit var otpFields: List<EditText>
  private lateinit var tvCountdown: TextView
  private lateinit var tvResend: TextView
  private lateinit var btnVerify: MaterialButton
  private lateinit var progressBar: ProgressBar
  private var countDownTimer: CountDownTimer? = null
  private var email = ""

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_verify_reset_code)

    email = intent.getStringExtra("email") ?: ""
    findViewById<TextView>(R.id.tvEmailSentTo).text = "Sent to $email"

    otpFields = listOf(
      findViewById(R.id.otp1),
      findViewById(R.id.otp2),
      findViewById(R.id.otp3),
      findViewById(R.id.otp4),
      findViewById(R.id.otp5),
      findViewById(R.id.otp6)
    )

    tvCountdown = findViewById(R.id.tvCountdown)
    tvResend = findViewById(R.id.tvResend)
    btnVerify = findViewById(R.id.btnVerify)
    progressBar = findViewById(R.id.progressBar)

    setupOtpInputs()
    startTimer()

    btnVerify.setOnClickListener {
      val code = otpFields.joinToString("") { it.text.toString() }
      if (code.length == 6) {
        verifyCode(code)
      } else {
        Toast.makeText(this, "Please enter the 6-digit code", Toast.LENGTH_SHORT).show()
      }
    }

    tvResend.setOnClickListener {
      if (tvResend.isEnabled) {
        resendCode()
      }
    }

    findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
    findViewById<View>(R.id.btnBackToLogin).setOnClickListener { finish() }
  }

  private fun setupOtpInputs() {
    for (i in otpFields.indices) {
      otpFields[i].addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
          if (s?.length == 1 && i < otpFields.size - 1) {
            otpFields[i + 1].requestFocus()
          }
        }
        override fun afterTextChanged(s: Editable?) {
          if (s?.isEmpty() == true && i > 0) {
            otpFields[i - 1].requestFocus()
          }
        }
      })
    }
  }

  private fun startTimer() {
    tvResend.isEnabled = false
    tvResend.alpha = 0.5f
    countDownTimer?.cancel()
    countDownTimer = object : CountDownTimer(300000, 1000) { // 5 minutes matches backend expiry
      override fun onTick(millisUntilFinished: Long) {
        val minutes = (millisUntilFinished / 1000) / 60
        val seconds = (millisUntilFinished / 1000) % 60
        tvCountdown.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
      }
      override fun onFinish() {
        tvCountdown.text = "00:00"
        tvResend.isEnabled = true
        tvResend.alpha = 1.0f
      }
    }.start()
  }

  private fun verifyCode(code: String) {
    progressBar.visibility = View.VISIBLE
    btnVerify.isEnabled = false

    lifecycleScope.launch {
      try {
        val res = RetrofitClient.instance.verifyOtp(VerifyOtpRequest(email, code))

        progressBar.visibility = View.GONE
        btnVerify.isEnabled = true

        if (res.isSuccessful) {
          val resetToken = res.body()?.resetToken ?: ""
          Toast.makeText(
            this@VerifyResetCodeActivity,
            "OTP verified!",
            Toast.LENGTH_SHORT
          ).show()
          // Pass email + resetToken to ResetPasswordActivity
          val intent = Intent(this@VerifyResetCodeActivity, ResetPasswordActivity::class.java)
          intent.putExtra("email", email)
          intent.putExtra("resetToken", resetToken)
          startActivity(intent)
          finish()
        } else {
          val error = JSONObject(res.errorBody()?.string() ?: "{}").optString("message", "Invalid OTP")
          Toast.makeText(this@VerifyResetCodeActivity, error, Toast.LENGTH_SHORT).show()
          clearOtpFields()
        }

      } catch (e: Exception) {
        progressBar.visibility = View.GONE
        btnVerify.isEnabled = true
        Toast.makeText(this@VerifyResetCodeActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
      }
    }
  }

  private fun resendCode() {
    progressBar.visibility = View.VISIBLE
    tvResend.isEnabled = false

    lifecycleScope.launch {
      try {
        val res = RetrofitClient.instance.forgotPassword(ForgotPasswordRequest(email))

        progressBar.visibility = View.GONE

        if (res.isSuccessful) {
          Toast.makeText(this@VerifyResetCodeActivity, "OTP resent to $email", Toast.LENGTH_SHORT).show()
          clearOtpFields()
          startTimer()
        } else {
          val error = JSONObject(res.errorBody()?.string() ?: "{}").optString("message", "Failed to resend OTP")
          Toast.makeText(this@VerifyResetCodeActivity, error, Toast.LENGTH_SHORT).show()
          tvResend.isEnabled = true
          tvResend.alpha = 1.0f
        }

      } catch (e: Exception) {
        progressBar.visibility = View.GONE
        Toast.makeText(this@VerifyResetCodeActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
        tvResend.isEnabled = true
        tvResend.alpha = 1.0f
      }
    }
  }

  private fun clearOtpFields() {
    otpFields.forEach { it.setText("") }
    otpFields[0].requestFocus()
  }

  override fun onDestroy() {
    super.onDestroy()
    countDownTimer?.cancel()
  }
}