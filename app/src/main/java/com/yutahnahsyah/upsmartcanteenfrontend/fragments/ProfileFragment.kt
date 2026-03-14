package com.yutahnahsyah.upsmartcanteenfrontend.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.yutahnahsyah.upsmartcanteenfrontend.OnboardingActivity
import com.yutahnahsyah.upsmartcanteenfrontend.R
import com.yutahnahsyah.upsmartcanteenfrontend.auth.Login
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.yutahnahsyah.upsmartcanteenfrontend.RetrofitClient
import com.bumptech.glide.Glide

class ProfileFragment : Fragment() {

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_profile, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val userNameTv = view.findViewById<TextView>(R.id.userName)
    val userEmailTv = view.findViewById<TextView>(R.id.userEmail)
    val profileIv = view.findViewById<ImageView>(R.id.profileImage)

    loadUserProfile(userNameTv, userEmailTv, profileIv)

    val onboardingInfoIcon = view.findViewById<ImageView>(R.id.onboardingInfoIcon)
    onboardingInfoIcon.setOnClickListener {
      val intent = Intent(requireContext(), OnboardingActivity::class.java)
      intent.putExtra("forceShow", true)
      startActivity(intent)
    }

    val editProfileCard = view.findViewById<MaterialCardView>(R.id.editProfileCard)
    editProfileCard.setOnClickListener {
      findNavController().navigate(R.id.action_nav_profile_to_nav_edit_profile)
    }

    val orderHistoryCard = view.findViewById<MaterialCardView>(R.id.orderHistoryCard)
    orderHistoryCard.setOnClickListener {
      findNavController().navigate(R.id.action_nav_profile_to_nav_history)
    }

    val paymentCard = view.findViewById<MaterialCardView>(R.id.paymentCard)
    paymentCard.setOnClickListener {
      findNavController().navigate(R.id.action_nav_profile_to_nav_payment)
    }

    val notificationsCard = view.findViewById<MaterialCardView>(R.id.notificationsCard)
    notificationsCard.setOnClickListener {
      findNavController().navigate(R.id.action_nav_profile_to_nav_notifications)
    }

    val aboutCard = view.findViewById<MaterialCardView>(R.id.aboutCard)
    aboutCard.setOnClickListener {
      findNavController().navigate(R.id.action_nav_profile_to_nav_about)
    }

    val helpCard = view.findViewById<MaterialCardView>(R.id.helpCard)
    helpCard.setOnClickListener {
      findNavController().navigate(R.id.action_nav_profile_to_nav_support)
    }

    val logoutButton = view.findViewById<MaterialButton>(R.id.logoutButton)
    logoutButton.setOnClickListener {
      // Log out logic: Navigate back to Login activity and clear the task stack
      val intent = Intent(requireActivity(), Login::class.java)
      intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
      startActivity(intent)
      requireActivity().finish()
    }
  }

  override fun onResume() {
    super.onResume()
    val userNameTv = view?.findViewById<TextView>(R.id.userName)
    val userEmailTv = view?.findViewById<TextView>(R.id.userEmail)
    val profileIv = view?.findViewById<ImageView>(R.id.profileImage)

    if (userNameTv != null && profileIv != null) {
      android.util.Log.d("PROFILE_DEBUG", "onResume triggered, loading profile...")
      loadUserProfile(userNameTv, userEmailTv!!, profileIv)
    }
  }

  private fun loadUserProfile(nameTv: TextView, emailTv: TextView, profileIv: ImageView) {
    val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val token = sharedPref.getString("auth_token", null)

    if (token == null) {
      android.util.Log.e("PROFILE_DEBUG", "Abort: Token is NULL")
      return
    }

    viewLifecycleOwner.lifecycleScope.launch {
      android.util.Log.d("PROFILE_DEBUG", "Starting API call with token: Bearer $token")
      try {
        val response = RetrofitClient.instance.getUserProfile("Bearer $token")

        if (response.isSuccessful) {
          val user = response.body()
          android.util.Log.d("PROFILE_DEBUG", "API Success! User data: $user")

          if (user != null) {
            nameTv.text = user.full_name
            emailTv.text = user.email

            if (!user.profile_picture_url.isNullOrEmpty()) {
              val cleanPath = user.profile_picture_url!!.trim().removePrefix("/")
              val fullImageUrl = "http://192.168.68.113:3000/$cleanPath"
              android.util.Log.d("GLIDE_DEBUG", "Loading into Glide: $fullImageUrl")

              Glide.with(this@ProfileFragment)
                .load(fullImageUrl)
                .signature(
                  com.bumptech.glide.signature.ObjectKey(
                    System.currentTimeMillis().toString()
                  )
                )
                .circleCrop()
                .into(profileIv)
            } else {
              android.util.Log.w("PROFILE_DEBUG", "profile_picture_url is empty in DB")
            }
          }
        } else {
          android.util.Log.e(
            "PROFILE_DEBUG",
            "API Error Code: ${response.code()} Body: ${response.errorBody()?.string()}"
          )
        }
      } catch (e: Exception) {
        android.util.Log.e("PROFILE_DEBUG", "FATAL EXCEPTION during API call", e)
      }
    }
  }
}