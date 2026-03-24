package com.yutahnahsyah.upsmartcanteen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yutahnahsyah.upsmartcanteen.auth.Login
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

  private var sessionCheckJob: Job? = null
  private lateinit var navController: NavController
  private var pendingNavigateTo: String? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    pendingNavigateTo = intent?.getStringExtra("navigate_to")

    val navHostFragment = supportFragmentManager
      .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    navController = navHostFragment.navController

    val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
    bottomNav.setupWithNavController(navController)

    navController.addOnDestinationChangedListener { _, destination, _ ->
      when (destination.id) {
        R.id.nav_cart_details -> bottomNav.visibility = View.GONE
        else -> bottomNav.visibility = View.VISIBLE
      }

      when (destination.id) {
        R.id.nav_stall,
        R.id.nav_store_food ->
          bottomNav.menu.findItem(R.id.nav_stall)?.isChecked = true

        R.id.nav_food ->
          bottomNav.menu.findItem(R.id.nav_food)?.isChecked = true

        R.id.nav_cart,
        R.id.nav_cart_details ->
          bottomNav.menu.findItem(R.id.nav_cart)?.isChecked = true

        R.id.nav_profile,
        R.id.nav_edit_profile,
        R.id.nav_history,
        R.id.nav_payment,
        R.id.nav_notifications,
        R.id.nav_about,
        R.id.nav_support,
        R.id.nav_terms,
        R.id.nav_privacy ->
          bottomNav.menu.findItem(R.id.nav_profile)?.isChecked = true
      }
    }

    requestNotificationPermission()

    if (savedInstanceState == null && intent?.getStringExtra("navigate_to") == "history") {
      navController.addOnDestinationChangedListener(
        object : NavController.OnDestinationChangedListener {
          override fun onDestinationChanged(
            controller: NavController,
            destination: NavDestination,
            arguments: Bundle?
          ) {
            navController.removeOnDestinationChangedListener(this)
            window.decorView.post {
              navController.navigate(R.id.nav_history)
            }
          }
        }
      )
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    if (intent.getStringExtra("navigate_to") == "history") {
      intent.removeExtra("navigate_to")
      window.decorView.postDelayed({
        if (!isFinishing && !isDestroyed) {
          navController.navigate(R.id.action_global_to_nav_history)
        }
      }, 200)
    }
  }

  private fun requestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(
          this, Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
      ) {
        ActivityCompat.requestPermissions(
          this,
          arrayOf(Manifest.permission.POST_NOTIFICATIONS),
          1001
        )
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