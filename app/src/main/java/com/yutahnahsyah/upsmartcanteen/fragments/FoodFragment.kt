package com.yutahnahsyah.upsmartcanteen.fragments

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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.yutahnahsyah.upsmartcanteen.AddToCartRequest
import com.yutahnahsyah.upsmartcanteen.Constants
import com.yutahnahsyah.upsmartcanteen.R
import com.yutahnahsyah.upsmartcanteen.RetrofitClient
import com.yutahnahsyah.upsmartcanteen.adapter.FoodAdapter
import com.yutahnahsyah.upsmartcanteen.data.model.FoodItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class FoodFragment : Fragment() {

  private lateinit var adapter: FoodAdapter
  private lateinit var btnAll: TextView
  private lateinit var btnMeals: TextView
  private lateinit var btnDrinks: TextView
  private lateinit var btnSnacks: TextView
  private lateinit var btnDessert: TextView
  private var pollingJob: Job? = null
  private var currentDialog: BottomSheetDialog? = null
  private var currentFood: FoodItem? = null
  private var currentQuantity: Int = 1
  private var currentFoodRef: FoodItem? = null

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
      showFoodDetailsDialog(selectedFood)
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

    btnAll = view.findViewById(R.id.chipAll)
    btnMeals = view.findViewById(R.id.chipMeals)
    btnDrinks = view.findViewById(R.id.chipDrinks)
    btnSnacks = view.findViewById(R.id.chipSnacks)
    btnDessert = view.findViewById(R.id.chipDessert)

    btnAll.setOnClickListener { updateChipColors(btnAll); adapter.filterByCategory("All") }
    btnMeals.setOnClickListener { updateChipColors(btnMeals); adapter.filterByCategory("Meals") }
    btnDrinks.setOnClickListener { updateChipColors(btnDrinks); adapter.filterByCategory("Drinks") }
    btnSnacks.setOnClickListener { updateChipColors(btnSnacks); adapter.filterByCategory("Snacks") }
    btnDessert.setOnClickListener { updateChipColors(btnDessert); adapter.filterByCategory("Dessert") }

    fetchAllFoods()
  }

  override fun onResume() {
    super.onResume()
    startPolling()
  }

  override fun onPause() {
    super.onPause()
    pollingJob?.cancel()
  }

  private fun startPolling() {
    pollingJob?.cancel()
    pollingJob = viewLifecycleOwner.lifecycleScope.launch {
      while (true) {
        fetchAllFoods()
        delay(3_000)
      }
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
    val foodStock = view.findViewById<TextView>(R.id.dialogFoodStock)
    val foodCategory = view.findViewById<TextView>(R.id.dialogFoodCategory)
    val foodStatus = view.findViewById<TextView>(R.id.dialogFoodStatus)
    val statusDot = view.findViewById<View>(R.id.statusDot)
    val btnAddToCart = view.findViewById<MaterialButton>(R.id.btnAddToCart)
    val btnPlus = view.findViewById<View>(R.id.btnPlus)
    val btnMinus = view.findViewById<View>(R.id.btnMinus)
    val tvQuantity = view.findViewById<TextView>(R.id.tvQuantity)
    val tvSubtotal = view.findViewById<TextView>(R.id.tvSubtotal)
    val btnClose = view.findViewById<View>(R.id.btnCloseDialog)

    currentQuantity = 1
    currentFoodRef = food

    tvQuantity.text = currentQuantity.toString()
    tvSubtotal.text = String.format(Locale.getDefault(), "₱%.2f", food.price * currentQuantity)

    foodName.text = food.name
    foodStall.text = "By ${food.stall_name ?: "Unknown Stall"}"
    foodDesc.text = food.description ?: "No description provided."
    foodPrice.text = String.format(Locale.getDefault(), "₱%.2f", food.price)
    tvSubtotal.text = String.format(Locale.getDefault(), "₱%.2f", food.price)
    foodStock.text = "Stocks: ${food.stock_qty}"
    foodCategory.text = food.category

    if (food.is_available && food.stock_qty > 0) {
      foodStatus.text = "Available"
      statusDot.setBackgroundResource(R.drawable.bg_status_dot_open)
      btnAddToCart.isEnabled = true
      btnAddToCart.alpha = 1.0f
    } else {
      foodStatus.text = if (!food.is_available) "Unavailable" else "Out of Stock"
      statusDot.setBackgroundColor(
        requireContext().getColor(android.R.color.holo_red_dark)
      )
      btnAddToCart.isEnabled = false
      btnAddToCart.alpha = 0.5f
    }

    val imageUrl = Constants.getFullImageUrl(food.image_url)
    Glide.with(this)
      .load(imageUrl)
      .placeholder(R.drawable.food_image)
      .error(R.drawable.food_image)
      .into(foodImage)

    btnPlus.setOnClickListener {
      val maxStock = currentFoodRef?.stock_qty ?: food.stock_qty
      if (currentQuantity < maxStock) {
        currentQuantity++
        tvQuantity.text = currentQuantity.toString()
        tvSubtotal.text = String.format(
          Locale.getDefault(), "₱%.2f",
          (currentFoodRef?.price ?: food.price) * currentQuantity
        )
      } else {
        Toast.makeText(requireContext(), "Max stock reached", Toast.LENGTH_SHORT).show()
      }
    }

    btnMinus.setOnClickListener {
      if (currentQuantity > 1) {
        currentQuantity--
        tvQuantity.text = currentQuantity.toString()
        tvSubtotal.text = String.format(
          Locale.getDefault(), "₱%.2f",
          (currentFoodRef?.price ?: food.price) * currentQuantity
        )
      }
    }

    btnAddToCart.setOnClickListener {
      addToCart(food.item_id, currentQuantity, dialog)
    }

    btnClose.setOnClickListener { dialog.dismiss() }

    currentDialog = dialog
    currentFood = food

    dialog.setOnDismissListener {
      currentDialog = null
      currentFood = null
      currentFoodRef = null
      currentQuantity = 1
    }

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
        val response = RetrofitClient.instance.addToCart(
          "Bearer $token",
          AddToCartRequest(itemId, qty)
        )
        if (response.isSuccessful) {
          Toast.makeText(requireContext(), "Added to cart!", Toast.LENGTH_SHORT).show()
          dialog.dismiss()
        } else {
          val errorBody = response.errorBody()?.string() ?: ""
          Log.e("ADD_TO_CART", "Failed: ${response.code()} - $errorBody")  // paste this output
          val message = when {
            response.code() == 400 && errorBody.contains("stock", ignoreCase = true) -> "Max stock reached."
            response.code() == 400 && errorBody.contains("unavailable", ignoreCase = true) -> "Item is currently unavailable."
            response.code() == 404 -> "Item no longer available."
            else -> "Failed to add to cart."
          }
          Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
      } catch (e: Exception) {
        Log.e("ADD_TO_CART", "Exception: ${e.message}", e)
        Toast.makeText(requireContext(), "Connection error", Toast.LENGTH_SHORT).show()
      }
    }  // ✅ missing closing brace was here
  }

  private fun fetchAllFoods() {
    viewLifecycleOwner.lifecycleScope.launch {
      try {
        val response = RetrofitClient.instance.getAllMenuItems()
        if (response.isSuccessful) {
          val foods = response.body() ?: emptyList()
          adapter.updateData(foods)
          currentFood?.let { openFood ->
            val updated = foods.find { it.item_id == openFood.item_id }
            if (updated != null) rebindDialog(updated)
          }
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

  private fun rebindDialog(updatedFood: FoodItem) {
    val dialog = currentDialog ?: return
    val view = dialog.findViewById<View>(android.R.id.content) ?: return

    currentFoodRef = updatedFood

    view.findViewById<TextView>(R.id.dialogFoodStock)?.text = "Stocks: ${updatedFood.stock_qty}"
    view.findViewById<TextView>(R.id.dialogFoodCategory)?.text = updatedFood.category

    if (currentQuantity > updatedFood.stock_qty) {
      currentQuantity = updatedFood.stock_qty.coerceAtLeast(1)
      view.findViewById<TextView>(R.id.tvQuantity)?.text = currentQuantity.toString()
      view.findViewById<TextView>(R.id.tvSubtotal)?.text =
        String.format(Locale.getDefault(), "₱%.2f", updatedFood.price * currentQuantity)
    }

    val foodStatus = view.findViewById<TextView>(R.id.dialogFoodStatus)
    val statusDot = view.findViewById<View>(R.id.statusDot)
    val btnAddToCart = view.findViewById<MaterialButton>(R.id.btnAddToCart)

    if (updatedFood.is_available && updatedFood.stock_qty > 0) {
      foodStatus?.text = "Available"
      statusDot?.setBackgroundResource(R.drawable.bg_status_dot_open)
      btnAddToCart?.isEnabled = true
      btnAddToCart?.alpha = 1.0f
    } else {
      foodStatus?.text = if (!updatedFood.is_available) "Unavailable" else "Out of Stock"
      statusDot?.setBackgroundColor(
        requireContext().getColor(android.R.color.holo_red_dark)
      )
      btnAddToCart?.isEnabled = false
      btnAddToCart?.alpha = 0.5f
    }
  }
}