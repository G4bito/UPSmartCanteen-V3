package com.yutahnahsyah.upsmartcanteen

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
import androidx.core.content.edit
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.cardview.widget.CardView
import com.google.android.material.button.MaterialButton
import com.yutahnahsyah.upsmartcanteen.auth.Login

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnNext: MaterialButton
    private lateinit var tvSkipTop: CardView
    private lateinit var layoutIndicators: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if onboarding has already been completed
        val sharedPref = getSharedPreferences("onboarding", Context.MODE_PRIVATE)
        val isFirstTime = sharedPref.getBoolean("isFirstTime", true)

        val forceShow = intent.getBooleanExtra("forceShow", false)
        if (!isFirstTime && !forceShow) {
            proceedToNext()
            return
        }

        setContentView(R.layout.activity_onboarding)

        viewPager = findViewById(R.id.viewPager)
        btnNext = findViewById(R.id.btnNext)
        tvSkipTop = findViewById(R.id.tvSkipTop)
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
                proceedToNext()
            }
        }

        tvSkipTop.setOnClickListener {
            markOnboardingComplete()
            proceedToNext()
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
                btnNext.text = if (position == onboardingItems.size - 1) "Get Started" else "Next"
            }
        })
    }

    private fun markOnboardingComplete() {
        getSharedPreferences("onboarding", Context.MODE_PRIVATE).edit {
            putBoolean("isFirstTime", false)
        }
    }

    private fun setupIndicators(count: Int) {
        layoutIndicators.removeAllViews()
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(6, 0, 6, 0) }

        repeat(count) { i ->
            val dot = ImageView(applicationContext).apply {
                setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        if (i == 0) R.drawable.indicator_active else R.drawable.indicator_inactive
                    )
                )
                layoutParams = params
            }
            layoutIndicators.addView(dot)
        }
    }

    private fun setCurrentIndicator(index: Int) {
        for (i in 0 until layoutIndicators.childCount) {
            val dot = layoutIndicators.getChildAt(i) as? ImageView ?: continue
            dot.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    if (i == index) R.drawable.indicator_active else R.drawable.indicator_inactive
                )
            )
        }
    }

    /**
     * Decision logic for where to go after onboarding.
     * If user is already logged in (e.g. they came from Profile), just finish.
     * Otherwise, go to Login.
     */
    private fun proceedToNext() {
        val userPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = userPrefs.getString("auth_token", null)

        if (token != null) {
            // User is already logged in, so we just go back to the app (MainActivity) 
            // or close this activity if it was "Force shown" from profile.
            if (intent.getBooleanExtra("forceShow", false)) {
                finish()
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        } else {
            // Not logged in, go to Login screen
            startActivity(Intent(this, Login::class.java))
            finish()
        }
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
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_onboarding, parent, false)
            )
        }

        override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
            val item = items[position]
            holder.ivOnboarding.setImageResource(item.image)
            holder.tvTitle.text = item.title
            holder.tvDescription.text = item.description
        }

        override fun getItemCount() = items.size
    }
}
