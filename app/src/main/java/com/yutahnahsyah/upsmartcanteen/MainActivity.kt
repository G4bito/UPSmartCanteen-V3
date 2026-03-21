package com.yutahnahsyah.upsmartcanteen

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : BaseActivity() {

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
}