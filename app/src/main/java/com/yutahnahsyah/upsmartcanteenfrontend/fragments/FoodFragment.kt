package com.yutahnahsyah.upsmartcanteenfrontend.fragments

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yutahnahsyah.upsmartcanteenfrontend.R
import com.yutahnahsyah.upsmartcanteenfrontend.adapter.FoodAdapter
import com.yutahnahsyah.upsmartcanteenfrontend.data.model.Food

class FoodFragment : Fragment() {

    private lateinit var adapter: FoodAdapter

    // UI Variables for the category chips
    private lateinit var btnAll: TextView
    private lateinit var btnMeals: TextView
    private lateinit var btnSnacks: TextView

    // 1. UPDATED DATA: Added "category" to each item (Meals, Snacks, etc.)
    // Make sure your Food.kt file has the 'category' field!
    private val foodList = listOf(
        Food("Food Number One", 101, "Store Number One", R.drawable.food_image, "Meals"),
        Food("Food Number Two", 202, "Store Number One", R.drawable.food_image, "Snacks"),
        Food("Food Number Three", 303, "Store Number Two", R.drawable.food_image, "Meals"),
        Food("Food Number Four", 404, "Store Number Two", R.drawable.food_image, "Beverages"),
        Food("Food Number Five", 505, "Store Number Three", R.drawable.food_image, "Snacks"),
        Food("Food Number Six", 606, "Store Number Three", R.drawable.food_image, "Meals")
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // This must match the XML file name where you put the search bar and chips
        return inflater.inflate(R.layout.fragment_food, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 2. SETUP RECYCLERVIEW
        // Note: I changed ID to 'foodRecyclerView' to match the updated XML.
        // If your XML still uses 'recyclerView', change this line back.
        val rv = view.findViewById<RecyclerView>(R.id.foodRecyclerView)

        adapter = FoodAdapter(foodList) { selectedFood ->
            Toast.makeText(requireContext(), "Clicking ${selectedFood.name}", Toast.LENGTH_SHORT).show()
        }

        rv.layoutManager = GridLayoutManager(requireContext(), 2)
        rv.adapter = adapter

        // 3. SEARCH BAR LOGIC
        val searchBar = view.findViewById<EditText>(R.id.searchBar)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // This calls the filter function we added to your Adapter
                adapter.filterBySearch(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 4. CATEGORY CHIP LOGIC
        btnAll = view.findViewById(R.id.chipAll)
        btnMeals = view.findViewById(R.id.chipMeals)
        btnSnacks = view.findViewById(R.id.chipSnacks)

        btnAll.setOnClickListener {
            adapter.filterByCategory("All")
            updateChipColors(btnAll)
        }

        btnMeals.setOnClickListener {
            adapter.filterByCategory("Meals")
            updateChipColors(btnMeals)
        }

        btnSnacks.setOnClickListener {
            adapter.filterByCategory("Snacks")
            updateChipColors(btnSnacks)
        }
    }

    // Helper function to toggle Green/White colors
    private fun updateChipColors(selectedChip: TextView) {
        val chips = listOf(btnAll, btnMeals, btnSnacks)

        chips.forEach { chip ->
            if (chip == selectedChip) {
                // Active: Dark Green
                chip.setBackgroundResource(R.drawable.chip_selected)
                chip.setTextColor(Color.WHITE)
            } else {
                // Inactive: White with Border
                chip.setBackgroundResource(R.drawable.chip_unselected)
                chip.setTextColor(Color.parseColor("#2D5016"))
            }
        }
    }
}