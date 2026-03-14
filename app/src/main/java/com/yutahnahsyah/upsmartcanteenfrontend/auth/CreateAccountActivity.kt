package com.yutahnahsyah.upsmartcanteenfrontend.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yutahnahsyah.upsmartcanteenfrontend.*
import kotlinx.coroutines.launch

class CreateAccountActivity : BaseActivity() {

  private lateinit var etEmployeeId: EditText
  private lateinit var etFullName: EditText
  private lateinit var etEmail: EditText
  private lateinit var spinnerDepartment: Spinner
  private lateinit var etPassword: EditText
  private lateinit var etConfirmPassword: EditText
  private lateinit var cbTerms: CheckBox
  private lateinit var btnRegister: MaterialButton
  private lateinit var progressBar: ProgressBar

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_create_account)

    // Initialize Views
    etEmployeeId = findViewById(R.id.etEmployeeId)
    etFullName = findViewById(R.id.etFullName)
    etEmail = findViewById(R.id.etEmail)
    spinnerDepartment = findViewById(R.id.spinnerDepartment)
    etPassword = findViewById(R.id.etPassword)
    etConfirmPassword = findViewById(R.id.etConfirmPassword)
    cbTerms = findViewById(R.id.cbTerms)
    btnRegister = findViewById(R.id.btnRegister)
    progressBar = findViewById(R.id.progressBar)
    val tvTermsAndPrivacy = findViewById<TextView>(R.id.tvTermsAndPrivacy)
    val tvLogin = findViewById<TextView>(R.id.tvLogin)

    setupDepartmentSpinner()

    val noSpaceFilter = InputFilter { source, start, end, _, _, _ ->
      for (i in start until end) {
        if (Character.isWhitespace(source[i])) return@InputFilter ""
      }
      null
    }
    etPassword.filters = arrayOf(noSpaceFilter)
    etConfirmPassword.filters = arrayOf(noSpaceFilter)

    val watcher = object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        checkFields()
      }

      override fun afterTextChanged(s: Editable?) {}
    }
    etEmployeeId.addTextChangedListener(watcher)
    etFullName.addTextChangedListener(watcher)
    etEmail.addTextChangedListener(watcher)
    etPassword.addTextChangedListener(watcher)
    etConfirmPassword.addTextChangedListener(watcher)
    cbTerms.setOnCheckedChangeListener { _, _ -> checkFields() }

    tvTermsAndPrivacy.setOnClickListener { showTermsDialog() }
    tvLogin.setOnClickListener { finish() }

    btnRegister.setOnClickListener {
      val id = etEmployeeId.text.toString().trim()
      val name = etFullName.text.toString().trim()
      val email = etEmail.text.toString().trim()
      val dept = spinnerDepartment.selectedItem.toString()
      val pass = etPassword.text.toString()
      val confirm = etConfirmPassword.text.toString()

      if (validateInput(name, email, pass, confirm)) {
        performRegistration(id, name, email, pass, dept)
      }
    }
  }

  private fun setupDepartmentSpinner() {
    val departments = arrayOf("CAHS", "CAS", "CCJE", "CEA", "CELA", "CHTM", "CITE", "CMA")
    val adapter = ArrayAdapter(this, R.layout.spinner_item, departments)
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    spinnerDepartment.adapter = adapter
  }

  private fun checkFields() {
    val id = etEmployeeId.text.toString().trim()
    val name = etFullName.text.toString().trim()
    val email = etEmail.text.toString().trim()
    val pass = etPassword.text.toString()
    val confirm = etConfirmPassword.text.toString()
    val isChecked = cbTerms.isChecked

    if (btnRegister.isEnabled) {
      btnRegister.alpha = 1.0f
    } else {
      btnRegister.alpha = 0.5f
    }

    btnRegister.isEnabled = id.isNotEmpty() &&
            name.isNotEmpty() &&
            email.isNotEmpty() &&
            pass.length >= 8 &&
            confirm.isNotEmpty() &&
            isChecked
  }

  private fun performRegistration(
    id: String,
    name: String,
    email: String,
    pass: String,
    dept: String
  ) {
    val request = RegisterRequest(id, name, email, pass, dept)

    btnRegister.isEnabled = false
    progressBar.visibility = View.VISIBLE

    lifecycleScope.launch {
      try {
        val response = RetrofitClient.instance.registerUser(request)
        progressBar.visibility = View.GONE

        if (response.isSuccessful) {
          Toast.makeText(this@CreateAccountActivity, "Registration Successful!", Toast.LENGTH_SHORT)
            .show()
          startActivity(Intent(this@CreateAccountActivity, Login::class.java))
          finish()
        } else {
          btnRegister.isEnabled = true
          val errorMsg = response.errorBody()?.string() ?: "Registration failed"
          Toast.makeText(this@CreateAccountActivity, errorMsg, Toast.LENGTH_LONG).show()
        }
      } catch (e: Exception) {
        progressBar.visibility = View.GONE
        btnRegister.isEnabled = true
        Toast.makeText(this@CreateAccountActivity, "Server Error: ${e.message}", Toast.LENGTH_SHORT)
          .show()
      }
    }
  }

  private fun validateInput(name: String, email: String, pass: String, confirm: String): Boolean {
    if (!email.lowercase().endsWith("@phinmaed.com") && !email.lowercase().endsWith("@gmail.com")) {
      Toast.makeText(this, "Use PhinmaEd or Gmail account", Toast.LENGTH_SHORT).show()
      return false
    }
    if (pass != confirm) {
      Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
      return false
    }
    return true
  }

  private fun showTermsDialog() {
    MaterialAlertDialogBuilder(this)
      .setTitle("Terms & Privacy Policy")
      .setMessage(
        "1. Data Collection: We collect minimal data like name and email.\n\n" +
                "2. Usage: Your data is used for ordering and identification.\n\n" +
                "3. Security: We use industry standards to protect your data.\n\n" +
                "4. Payments: Cash and Stub are the only accepted methods.\n\n" +
                "By using this app, you agree to these terms."
      )
      .setPositiveButton("I Understand") { dialog, _ ->
        dialog.dismiss()
      }
      .show()
  }
}