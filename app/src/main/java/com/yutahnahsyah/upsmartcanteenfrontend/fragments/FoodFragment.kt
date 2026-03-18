package com.yutahnahsyah.upsmartcanteenfrontend.fragments

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yutahnahsyah.upsmartcanteenfrontend.R
import com.yutahnahsyah.upsmartcanteenfrontend.RetrofitClient
import com.yutahnahsyah.upsmartcanteenfrontend.adapter.FoodAdapter
import kotlinx.coroutines.launch

class FoodFragment : Fragment() {

    private lateinit var adapter: FoodAdapter
    private lateinit var btnAll: TextView
    private lateinit var btnMeals: TextView
    private lateinit var btnDrinks: TextView
    private lateinit var btnSnacks: TextView
    private lateinit var btnDessert: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_food, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.foodRecyclerView)
        adapter = FoodAdapter(emptyList()) { selectedFood ->
            Toast.makeText(requireContext(), "Selected: ${selectedFood.name}", Toast.LENGTH_SHORT).show()
        }

        rv.layoutManager = GridLayoutManager(requireContext(), 2)
        rv.adapter = adapter

        val searchBar = view.findViewById<EditText>(R.id.searchBar)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filterBySearch(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Initialize all chips
        btnAll = view.findViewById(R.id.chipAll)
        btnMeals = view.findViewById(R.id.chipMeals)
        btnDrinks = view.findViewById(R.id.chipDrinks)
        btnSnacks = view.findViewById(R.id.chipSnacks)
        btnDessert = view.findViewById(R.id.chipDessert)

        btnAll.setOnClickListener {
            updateChipColors(btnAll)
            adapter.filterByCategory("All")
        }
        btnMeals.setOnClickListener {
            updateChipColors(btnMeals)
            adapter.filterByCategory("Meals")
        }
        btnDrinks.setOnClickListener {
            updateChipColors(btnDrinks)
            adapter.filterByCategory("Drinks")
        }
        btnSnacks.setOnClickListener {
            updateChipColors(btnSnacks)
            adapter.filterByCategory("Snacks")
        }
        btnDessert.setOnClickListener {
            updateChipColors(btnDessert)
            // Filtering for "Dessert" - ensure your database matches this spelling
            adapter.filterByCategory("Dessert")
        }

        fetchAllFoods()
    }

    private fun fetchAllFoods() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getAllMenuItems()
                if (response.isSuccessful) {
                    val foods = response.body() ?: emptyList()
                    adapter.updateData(foods)
                } else {
                    Log.e("FOOD_FETCH", "Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("FOOD_FETCH", "Exception", e)
            }
        }
    }

    private fun updateChipColors(selectedChip: TextView) {
        val chips = listOf(btnAll, btnMeals, btnDrinks, btnSnacks, btnDessert)
        chips.forEach { chip ->
            if (chip == selectedChip) {
                chip.setBackgroundResource(R.drawable.chip_selected)
                chip.setTextColor(Color.WHITE)
            } else {
                chip.setBackgroundResource(R.drawable.chip_unselected)
                chip.setTextColor(Color.parseColor("#2D5016"))
            }
        }
    }
}
