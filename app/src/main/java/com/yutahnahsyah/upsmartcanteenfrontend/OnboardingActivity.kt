package com.yutahnahsyah.upsmartcanteenfrontend

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.yutahnahsyah.upsmartcanteenfrontend.auth.Login

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnNext: MaterialButton
    private lateinit var tvSkip: TextView
    private lateinit var layoutIndicators: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if onboarding has already been completed
        val sharedPref = getSharedPreferences("onboarding", Context.MODE_PRIVATE)
        val isFirstTime = sharedPref.getBoolean("isFirstTime", true)
        
        // If not first time and not explicitly requested from Profile, skip to Login
        val forceShow = intent.getBooleanExtra("forceShow", false)
        if (!isFirstTime && !forceShow) {
            navigateToLogin()
            return
        }

        setContentView(R.layout.activity_onboarding)

        viewPager = findViewById(R.id.viewPager)
        btnNext = findViewById(R.id.btnNext)
        tvSkip = findViewById(R.id.tvSkip)
        layoutIndicators = findViewById(R.id.layoutIndicators)

        val onboardingItems = listOf(
            OnboardingItem(
                R.drawable.ic_food,
                "Satisfy Your Cravings",
                "From quick snacks to heavy meals, find exactly what you're hungry for in your favorite stalls."
            ),
            OnboardingItem(
                R.drawable.ic_cart,
                "Easy Ordering",
                "Order your favorite meals ahead of time and skip the long queues."
            ),
            OnboardingItem(
                R.drawable.ic_payment,
                "Easy Pay",
                "Pay conveniently using cash or stubs."
            )
        )

        val adapter = OnboardingAdapter(onboardingItems)
        viewPager.adapter = adapter

        setupIndicators(onboardingItems.size)
        setCurrentIndicator(0)

        btnNext.setOnClickListener {
            if (viewPager.currentItem + 1 < onboardingItems.size) {
                viewPager.currentItem += 1
            } else {
                markOnboardingComplete()
                navigateToLogin()
            }
        }

        tvSkip.setOnClickListener {
            markOnboardingComplete()
            navigateToLogin()
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
                if (position == onboardingItems.size - 1) {
                    btnNext.text = "Get Started"
                } else {
                    btnNext.text = "Next"
                }
            }
        })
    }

    private fun markOnboardingComplete() {
        val sharedPref = getSharedPreferences("onboarding", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("isFirstTime", false)
            apply()
        }
    }

    private fun setupIndicators(count: Int) {
        val indicators = arrayOfNulls<ImageView>(count)
        val layoutParams: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        layoutParams.setMargins(6, 0, 6, 0)

        for (i in indicators.indices) {
            indicators[i] = ImageView(applicationContext)
            indicators[i]?.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.dot_unselected
                )
            )
            indicators[i]?.layoutParams = layoutParams
            layoutIndicators.addView(indicators[i])
        }
    }

    private fun setCurrentIndicator(index: Int) {
        val childCount = layoutIndicators.childCount
        for (i in 0 until childCount) {
            val imageView = layoutIndicators.getChildAt(i) as ImageView
            if (i == index) {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.dot_selected
                    )
                )
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.dot_unselected
                    )
                )
            }
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, Login::class.java))
        finish()
    }

    data class OnboardingItem(val image: Int, val title: String, val description: String)

    inner class OnboardingAdapter(private val items: List<OnboardingItem>) :
        RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

        inner class OnboardingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivOnboarding: ImageView = view.findViewById(R.id.ivOnboarding)
            val tvTitle: TextView = view.findViewById(R.id.tvTitle)
            val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
            return OnboardingViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_onboarding, parent, false)
            )
        }

        override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
            val item = items[position]
            holder.ivOnboarding.setImageResource(item.image)
            holder.tvTitle.text = item.title
            holder.tvDescription.text = item.description
        }

        override fun getItemCount(): Int = items.size
    }
}
