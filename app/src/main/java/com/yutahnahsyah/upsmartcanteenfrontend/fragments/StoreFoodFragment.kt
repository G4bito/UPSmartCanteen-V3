package com.yutahnahsyah.upsmartcanteenfrontend.fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.yutahnahsyah.upsmartcanteenfrontend.AddToCartRequest
import com.yutahnahsyah.upsmartcanteenfrontend.R
import com.yutahnahsyah.upsmartcanteenfrontend.RetrofitClient
import com.yutahnahsyah.upsmartcanteenfrontend.adapter.FoodAdapter
import com.yutahnahsyah.upsmartcanteenfrontend.data.model.FoodItem
import kotlinx.coroutines.launch
import java.util.Locale

class StoreFoodFragment : Fragment() {

    private lateinit var adapter: FoodAdapter
    private var storeName: String? = null
    private var stallId: Int = -1

    private lateinit var btnAll: TextView
    private lateinit var btnMeals: TextView
    private lateinit var btnSnacks: TextView
    private lateinit var btnDrinks: TextView
    private lateinit var btnDessert: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            storeName = it.getString("storeName")
            stallId = it.getInt("stallId", -1)
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
        val searchBar = view.findViewById<EditText>(R.id.searchBar)

        titleTextView.text = storeName ?: "Store Menu"

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        adapter = FoodAdapter(emptyList()) { selectedFood ->
            showFoodDetailsDialog(selectedFood)
        }

        rv.layoutManager = GridLayoutManager(requireContext(), 2)
        rv.adapter = adapter

        // Search Implementation
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filterBySearch(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Category Chips Implementation
        btnAll = view.findViewById(R.id.chipAll)
        btnMeals = view.findViewById(R.id.chipMeals)
        btnSnacks = view.findViewById(R.id.chipSnacks)
        btnDrinks = view.findViewById(R.id.chipDrinks)
        btnDessert = view.findViewById(R.id.chipDessert)

        btnAll.setOnClickListener { updateChipColors(btnAll); adapter.filterByCategory("All") }
        btnMeals.setOnClickListener { updateChipColors(btnMeals); adapter.filterByCategory("Meals") }
        btnSnacks.setOnClickListener { updateChipColors(btnSnacks); adapter.filterByCategory("Snacks") }
        btnDrinks.setOnClickListener { updateChipColors(btnDrinks); adapter.filterByCategory("Drinks") }
        btnDessert.setOnClickListener { updateChipColors(btnDessert); adapter.filterByCategory("Dessert") }

        if (stallId != -1) {
            fetchFoods(stallId)
        }
    }

    private fun showFoodDetailsDialog(food: FoodItem) {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.dialog_food_details, null)
        dialog.setContentView(view)

        val foodImage = view.findViewById<ImageView>(R.id.dialogFoodImage)
        val foodName = view.findViewById<TextView>(R.id.dialogFoodName)
        val foodStall = view.findViewById<TextView>(R.id.dialogFoodStall)
        val foodDesc = view.findViewById<TextView>(R.id.dialogFoodDescription)
        val foodPrice = view.findViewById<TextView>(R.id.dialogFoodPrice)
        val btnAddToCart = view.findViewById<MaterialButton>(R.id.btnAddToCart)
        val btnPlus = view.findViewById<ImageView>(R.id.btnPlus)
        val btnMinus = view.findViewById<ImageView>(R.id.btnMinus)
        val tvQuantity = view.findViewById<TextView>(R.id.tvQuantity)
        val btnClose = view.findViewById<ImageView>(R.id.btnCloseDialog)

        var quantity = 1

        foodName.text = food.name
        foodStall.text = "By ${food.stall_name}"
        foodDesc.text = food.description ?: "No description provided."
        foodPrice.text = String.format(Locale.getDefault(), "₱%.2f", food.price)

        val serverUrl = "http://192.168.18.41:3000"
        val imageUrl = if (!food.image_url.isNullOrEmpty()) {
            val cleanPath = food.image_url!!.trim().removePrefix("/")
            "$serverUrl/$cleanPath"
        } else {
            null
        }

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.food_image)
            .into(foodImage)

        btnPlus.setOnClickListener {
            if (quantity < food.stock_qty) {
                quantity++
                tvQuantity.text = quantity.toString()
                foodPrice.text = String.format(Locale.getDefault(), "₱%.2f", food.price * quantity)
            } else {
                Toast.makeText(requireContext(), "Max stock reached", Toast.LENGTH_SHORT).show()
            }
        }

        btnMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                tvQuantity.text = quantity.toString()
                foodPrice.text = String.format(Locale.getDefault(), "₱%.2f", food.price * quantity)
            }
        }

        btnAddToCart.setOnClickListener {
            addToCart(food.item_id, quantity, dialog)
        }

        btnClose.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun addToCart(itemId: Int, qty: Int, dialog: BottomSheetDialog) {
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("auth_token", null)

        if (token == null) {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.addToCart("Bearer $token", AddToCartRequest(itemId, qty))
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Added to cart!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "Failed to add to cart", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Connection error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchFoods(id: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getFoodsByStall(id)
                if (response.isSuccessful) {
                    val foods = response.body() ?: emptyList()
                    adapter.updateData(foods)
                }
            } catch (e: Exception) {
                Log.e("STORE_FOOD", "Exception", e)
            }
        }
    }

    private fun updateChipColors(selectedChip: TextView) {
        val chips = listOf(btnAll, btnMeals, btnSnacks, btnDrinks, btnDessert)
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
