package com.yutahnahsyah.upsmartcanteen.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.yutahnahsyah.upsmartcanteen.Constants
import com.yutahnahsyah.upsmartcanteen.OnboardingActivity
import com.yutahnahsyah.upsmartcanteen.R
import com.yutahnahsyah.upsmartcanteen.RetrofitClient
import com.yutahnahsyah.upsmartcanteen.auth.Login
import kotlinx.coroutines.launch
import java.util.Locale

class ProfileFragment : Fragment() {

    private var userNameTv: TextView? = null
    private var userEmailTv: TextView? = null
    private var profileIv: ImageView? = null
    private var statOrdersTv: TextView? = null
    private var statSpentTv: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userNameTv = view.findViewById(R.id.userName)
        userEmailTv = view.findViewById(R.id.userEmail)
        profileIv = view.findViewById(R.id.profileImage)
        statOrdersTv = view.findViewById(R.id.statOrders)
        statSpentTv = view.findViewById(R.id.statSpent)

        loadUserProfile()
        loadOrderStats()

        // FIXED: Found as View instead of ImageView to avoid ClassCastException
        view.findViewById<View>(R.id.onboardingInfoIcon)?.setOnClickListener {
            val intent = Intent(requireContext(), OnboardingActivity::class.java)
            intent.putExtra("forceShow", true)
            startActivity(intent)
        }

        view.findViewById<MaterialCardView>(R.id.editProfileCard)?.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_nav_edit_profile)
        }

        view.findViewById<MaterialCardView>(R.id.orderHistoryCard)?.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_nav_history)
        }

        view.findViewById<MaterialCardView>(R.id.paymentCard)?.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_nav_payment)
        }

        view.findViewById<MaterialCardView>(R.id.notificationsCard)?.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_nav_notifications)
        }

        view.findViewById<MaterialCardView>(R.id.aboutCard)?.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_nav_about)
        }

        view.findViewById<MaterialCardView>(R.id.helpCard)?.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_nav_support)
        }

        view.findViewById<MaterialButton>(R.id.logoutButton)?.setOnClickListener {
            val intent = Intent(requireActivity(), Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserProfile()
        loadOrderStats()
    }

    private fun loadUserProfile() {
        val context = context ?: return
        val sharedPref = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("auth_token", null)

        if (token == null) {
            Log.e("PROFILE_DEBUG", "Abort: Token is NULL")
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getUserProfile("Bearer $token")

                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        userNameTv?.text = user.full_name
                        userEmailTv?.text = user.email

                        if (!user.profile_picture_url.isNullOrEmpty()) {
                            val fullImageUrl = Constants.getFullImageUrl(user.profile_picture_url)

                            profileIv?.let {
                                Glide.with(this@ProfileFragment)
                                    .load(fullImageUrl)
                                    .signature(com.bumptech.glide.signature.ObjectKey(System.currentTimeMillis().toString()))
                                    .circleCrop()
                                    .into(it)
                            }
                        }
                    }
                } else {
                    Log.e("PROFILE_DEBUG", "API Error Code: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("PROFILE_DEBUG", "Exception during API call", e)
            }
        }
    }

    private fun loadOrderStats() {
        val context = context ?: return
        val sharedPref = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("auth_token", null)

        if (token == null) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getMyOrders("Bearer $token")
                if (response.isSuccessful) {
                    val orders = response.body() ?: emptyList()
                    
                    // Count only successfully completed orders (Status: picked_up or completed)
                    val successfulOrders = orders.filter { 
                        it.status.equals("completed", ignoreCase = true) || 
                        it.status.equals("picked_up", ignoreCase = true)
                    }
                    
                    val totalOrders = successfulOrders.size
                    val totalSpent = successfulOrders.sumOf { it.total_price }

                    statOrdersTv?.text = totalOrders.toString()
                    statSpentTv?.text = String.format(Locale.getDefault(), "₱%.2f", totalSpent)
                    
                    Log.d("PROFILE_STATS", "Loaded stats: $totalOrders successful orders, $totalSpent spent")
                } else {
                    Log.e("PROFILE_STATS", "Failed to load orders: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("PROFILE_STATS", "Exception during stats load", e)
            }
        }
    }
}
