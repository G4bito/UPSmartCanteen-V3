package com.yutahnahsyah.upsmartcanteen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yutahnahsyah.upsmartcanteen.auth.Login
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

  private var sessionCheckJob: Job? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val navHostFragment = supportFragmentManager
      .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    val navController = navHostFragment.navController

    val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
    bottomNav.setupWithNavController(navController)

    navController.addOnDestinationChangedListener { _, destination, _ ->
      when (destination.id) {
        R.id.nav_cart_details -> bottomNav.visibility = View.GONE
        else -> bottomNav.visibility = View.VISIBLE
      }
    }
  }

  override fun onResume() {
    super.onResume()

    startSessionCheck()
  }

  override fun onPause() {
    super.onPause()
    sessionCheckJob?.cancel()
  }

  private fun startSessionCheck() {
    sessionCheckJob?.cancel()
    sessionCheckJob = lifecycleScope.launch {
      while (true) {
        delay(3_000)
        checkSession()
      }
    }
  }

  private suspend fun checkSession() {
    try {
      val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
      val token = sharedPref.getString("auth_token", null) ?: run {
        forceLogout()
        return
      }

      val response = RetrofitClient.instance.getUserProfile("Bearer $token")

      if (response.code() == 401 || response.code() == 403) {
        forceLogout()
      }
    } catch (e: Exception) {
    }
  }

  private fun forceLogout() {
    val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    sharedPref.edit().remove("auth_token").apply()

    runOnUiThread {
      val intent = Intent(this, Login::class.java)
      intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
      startActivity(intent)
      finish()
    }
  }
}