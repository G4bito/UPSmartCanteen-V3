package com.yutahnahsyah.upsmartcanteen.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.yutahnahsyah.upsmartcanteen.R
import kotlinx.coroutines.launch

class ForgotPasswordActivity : AppCompatActivity() {

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
            if (email.isNotEmpty()) {
                sendResetLink(email, progressBar)
            } else {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            }
        }

        btnBackToLogin.setOnClickListener { finish() }
        btnBack.setOnClickListener { finish() }
    }

    private fun sendResetLink(email: String, loader: ProgressBar) {
        loader.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                // You'll need to add forgotPassword to ApiService.kt if not already there
                // val response = RetrofitClient.instance.forgotPassword(email)
                
                // Mocking for now since I can't edit your backend directly
                kotlinx.coroutines.delay(1500)
                loader.visibility = View.GONE
                
                Toast.makeText(this@ForgotPasswordActivity, "Reset code was sent to $email", Toast.LENGTH_LONG).show()
                
                // Navigate to VerifyResetCodeActivity instead of finishing
                val intent = Intent(this@ForgotPasswordActivity, VerifyResetCodeActivity::class.java)
                intent.putExtra("email", email)
                startActivity(intent)
                finish() // Optional: finish this activity so they can't come back to the email entry screen
            } catch (e: Exception) {
                loader.visibility = View.GONE
                Toast.makeText(this@ForgotPasswordActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
