package com.yutahnahsyah.upsmartcanteen.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.yutahnahsyah.upsmartcanteen.Constants
import com.yutahnahsyah.upsmartcanteen.R
import com.yutahnahsyah.upsmartcanteen.RegisterRequest
import com.yutahnahsyah.upsmartcanteen.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class EditProfileFragment : Fragment() {

    private val departments = arrayOf("CAHS", "CAS", "CCJE", "CEA", "CELA", "CHTM", "CITE", "CMA")
    private var profileImageView: ShapeableImageView? = null
    private var isPasswordVisible = false
    private var currentEmployeeId: String = ""

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            profileImageView?.setImageURI(it)
            uploadImageToServer(it)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<View>(R.id.btnBack)
        val btnChangePhoto = view.findViewById<View>(R.id.btnChangePhoto)
        profileImageView = view.findViewById(R.id.editProfileImage)
        val etFullName = view.findViewById<EditText>(R.id.etFullName)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val spinnerDept = view.findViewById<Spinner>(R.id.spinnerDepartment)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val togglePassword = view.findViewById<ImageButton>(R.id.togglePassword)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSaveProfile)

        btnBack?.setOnClickListener { findNavController().navigateUp() }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, departments)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDept?.adapter = adapter

        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("auth_token", null)

        if (token != null) {
            loadCurrentProfile(token, etFullName, etEmail, spinnerDept)
        }

        btnChangePhoto?.setOnClickListener { pickImageLauncher.launch("image/*") }

        togglePassword?.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword?.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                togglePassword.setImageResource(R.drawable.ic_eye_on)
            } else {
                etPassword?.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePassword.setImageResource(R.drawable.ic_eye_off)
            }
            etPassword?.let { it.setSelection(it.text.length) }
        }

      btnSave?.setOnClickListener {
        val name = etFullName?.text?.toString()?.trim() ?: ""
        val email = etEmail?.text?.toString()?.trim() ?: ""
        val selectedDept = spinnerDept?.selectedItem?.toString() ?: ""
        val password = etPassword?.text?.toString()?.trim() ?: ""

        val validEmailRegex = Regex("^[^@]+@(phinmaed\\.com|gmail\\.com)$")

        if (token != null && name.isNotEmpty() && email.isNotEmpty()) {
          if (!validEmailRegex.matches(email)) {
            etEmail?.error = "Email must end with @phinmaed.com or @gmail.com"
            etEmail?.requestFocus()
          } else {
            updateProfile(token, name, email, selectedDept, password)
          }
        } else {
          Toast.makeText(requireContext(), "Name and Email are required", Toast.LENGTH_SHORT).show()
        }
      }    }

    private fun loadCurrentProfile(token: String, nameEt: EditText?, emailEt: EditText?, deptSpinner: Spinner?) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getUserProfile("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    currentEmployeeId = user.employee_id
                    nameEt?.setText(user.full_name)
                    emailEt?.setText(user.email)

                    val imageUrl = Constants.getFullImageUrl(user.profile_picture_url)
                    profileImageView?.let {
                        Glide.with(this@EditProfileFragment)
                            .load(imageUrl)
                            .circleCrop()
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .into(it)
                    }

                    val deptIndex = departments.indexOf(user.department)
                    if (deptIndex >= 0) deptSpinner?.setSelection(deptIndex)
                }
            } catch (e: Exception) {
                android.util.Log.e("EDIT_PROFILE", "Error loading profile", e)
            }
        }
    }

  private fun updateProfile(token: String, name: String, email: String, dept: String, pass: String) {
    val updateRequest = RegisterRequest(currentEmployeeId, name, email, pass, dept)
    viewLifecycleOwner.lifecycleScope.launch {
      try {
        val response = RetrofitClient.instance.editUserProfile("Bearer $token", updateRequest)
        if (response.isSuccessful) {
          // ✅ Sync updated values back to SharedPreferences
          val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
          sharedPref.edit().apply {
            putString("user_full_name", name)
            putString("user_department", dept)
            apply()
          }

          Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
          findNavController().navigateUp()
        } else {
          Toast.makeText(requireContext(), "Update failed: ${response.code()}", Toast.LENGTH_SHORT).show()
        }
      } catch (e: Exception) {
        Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
      }
    }
  }

    private fun uploadImageToServer(uri: Uri) {
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("auth_token", null) ?: return
        val file = uriToFile(uri, "profile_pic.jpg") ?: return
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.uploadProfilePicture("Bearer $token", body)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Photo uploaded!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uriToFile(uri: Uri, fileName: String): File? {
        return try {
            val context = context ?: return null
            val file = File(context.cacheDir, fileName)
            val inputStream = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            null
        }
    }
}
