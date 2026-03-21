package com.yutahnahsyah.upsmartcanteen.auth

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.yutahnahsyah.upsmartcanteen.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class VerifyResetCodeActivity : AppCompatActivity() {

    private lateinit var otpFields: List<EditText>
    private lateinit var tvCountdown: TextView
    private lateinit var tvResend: TextView
    private lateinit var btnVerify: MaterialButton
    private lateinit var progressBar: ProgressBar
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_reset_code)

        val email = intent.getStringExtra("email") ?: "your email"
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
                resendCode(email)
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
        countDownTimer = object : CountDownTimer(600000, 1000) { // 10 minutes
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
        lifecycleScope.launch {
            try {
                // Mocking verification
                delay(1500)
                progressBar.visibility = View.GONE
                Toast.makeText(this@VerifyResetCodeActivity, "Code verified successfully!", Toast.LENGTH_SHORT).show()
                // Redirect to New Password Activity here
                finish()
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@VerifyResetCodeActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resendCode(email: String) {
        Toast.makeText(this, "Resending code to $email...", Toast.LENGTH_SHORT).show()
        startTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
