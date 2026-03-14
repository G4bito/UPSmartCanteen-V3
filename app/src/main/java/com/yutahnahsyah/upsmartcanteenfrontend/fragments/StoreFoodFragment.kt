package com.yutahnahsyah.upsmartcanteenfrontend.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yutahnahsyah.upsmartcanteenfrontend.R
import com.yutahnahsyah.upsmartcanteenfrontend.adapter.FoodAdapter
import com.yutahnahsyah.upsmartcanteenfrontend.data.model.Food

class StoreFoodFragment : Fragment() {

    private lateinit var adapter: FoodAdapter
    private var storeName: String? = null

    private val foodList = listOf(
        Food("Food Number One", 101, "Store Number One", R.drawable.food_image, "Meals"),
        Food("Food Number Two", 202, "Store Number One", R.drawable.food_image, "Snacks"),
        Food("Food Number Three", 303, "Store Number Two", R.drawable.food_image, "Meals"),
        Food("Food Number Four", 404, "Store Number Two", R.drawable.food_image, "Beverages"),
        Food("Food Number Five", 505, "Store Number Three", R.drawable.food_image, "Snacks"),
        Food("Food Number Six", 606, "Store Number Three", R.drawable.food_image, "Meals")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            storeName = it.getString("storeName")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_store_food, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleTextView = view.findViewById<TextView>(R.id.storeTitle)
        val backButton = view.findViewById<ImageView>(R.id.btnBack)
        val rv = view.findViewById<RecyclerView>(R.id.foodRecyclerView)

        titleTextView.text = storeName ?: "Store Menu"

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        val filteredFoods = if (storeName != null) {
            foodList.filter { it.store == storeName }
        } else {
            foodList
        }

        adapter = FoodAdapter(filteredFoods) { selectedFood ->
            // Handle food click if needed
        }

        rv.layoutManager = GridLayoutManager(requireContext(), 2)
        rv.adapter = adapter
    }
}
