package com.yutahnahsyah.upsmartcanteen.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yutahnahsyah.upsmartcanteen.R
import com.yutahnahsyah.upsmartcanteen.RetrofitClient
import com.yutahnahsyah.upsmartcanteen.UpdateCartItemRequest
import com.yutahnahsyah.upsmartcanteen.adapter.CartItemAdapter
import com.yutahnahsyah.upsmartcanteen.data.model.FoodItem
import com.google.android.material.button.MaterialButton
import com.yutahnahsyah.upsmartcanteen.PlaceOrderRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class CartDetailsFragment : Fragment() {

  private val args: CartDetailsFragmentArgs by navArgs()
  private var tvTotalBottom: TextView? = null
  private var tvItemCount: TextView? = null
  private var etOrderRemarks: EditText? = null
  private var rvCartItems: RecyclerView? = null
  private var adapter: CartItemAdapter? = null
  private var currentStallItems: List<FoodItem> = emptyList()

  private var pollingJob: Job? = null

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_cart_details, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val btnBack = view.findViewById<View>(R.id.btnBack)
    val tvStoreNameHeader = view.findViewById<TextView>(R.id.tvStoreNameHeader)
    val btnAddMore = view.findViewById<View>(R.id.btnAddMore)
    val btnPlaceOrder = view.findViewById<MaterialButton>(R.id.btnConfirmPayment)

    rvCartItems = view.findViewById(R.id.rvCartItems)
    tvTotalBottom = view.findViewById(R.id.tvTotalBottom)
    tvItemCount = view.findViewById(R.id.tvItemCount)
    etOrderRemarks = view.findViewById(R.id.etOrderRemarks)

    tvStoreNameHeader?.text = args.storeName

    btnBack?.setOnClickListener {
      findNavController().navigateUp()
    }

    btnAddMore?.setOnClickListener {
      val stallId = currentStallItems.firstOrNull()?.stall_id ?: run {
        findNavController().navigateUp()
        return@setOnClickListener
      }
      val bundle = Bundle().apply {
        putString("storeName", args.storeName)
        putInt("stallId", stallId)
      }
      findNavController().navigate(R.id.action_nav_cart_details_to_nav_store_food, bundle)
    }

    rvCartItems?.layoutManager = LinearLayoutManager(requireContext())

    adapter = CartItemAdapter(
      items = mutableListOf(),
      onQuantityChanged = {
        updateTotals(items = adapter?.getItems() ?: emptyList())
      },
      onItemRemoved = { removedItem ->
        removeCartItem(removedItem.cart_item_id)
      },
      onQuantityUpdated = { cartItemId, quantity ->
        updateCartItemQuantity(cartItemId, quantity)
      }
    )
    rvCartItems?.adapter = adapter

    fetchCartItems()

    btnPlaceOrder?.setOnClickListener {
      val items = adapter?.getItems() ?: emptyList()
      if (items.isNotEmpty()) {
        placeOrder()
      } else {
        Toast.makeText(requireContext(), "Cart is empty", Toast.LENGTH_SHORT).show()
      }
    }
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
        delay(3_000)
        fetchCartItems(isPolling = true)
      }
    }
  }

  private fun removeCartItem(cartItemId: Int) {
    val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val token = sharedPref.getString("auth_token", null) ?: return

    viewLifecycleOwner.lifecycleScope.launch {
      try {
        val response = RetrofitClient.instance.removeCartItem("Bearer $token", cartItemId)
        if (!response.isSuccessful) {
          Log.e("CART_DETAILS", "Remove failed: ${response.code()}")
          Toast.makeText(requireContext(), "Failed to remove item", Toast.LENGTH_SHORT).show()
        } else {
          updateTotals(adapter?.getItems() ?: emptyList())
        }
      } catch (e: Exception) {
        Log.e("CART_DETAILS", "Remove exception", e)
        Toast.makeText(requireContext(), "Connection error", Toast.LENGTH_SHORT).show()
      }
    }
  }

  private fun updateCartItemQuantity(cartItemId: Int, quantity: Int) {
    val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val token = sharedPref.getString("auth_token", null) ?: return

    viewLifecycleOwner.lifecycleScope.launch {
      try {
        RetrofitClient.instance.updateCartItem(
          "Bearer $token",
          cartItemId,
          UpdateCartItemRequest(quantity)
        )
      } catch (e: Exception) {
        Log.e("CART_DETAILS", "Update quantity exception", e)
      }
    }
  }

  private fun fetchCartItems(isPolling: Boolean = false) {
    val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val token = sharedPref.getString("auth_token", null) ?: return

    viewLifecycleOwner.lifecycleScope.launch {
      try {
        val response = RetrofitClient.instance.getMyCart("Bearer $token")
        if (response.isSuccessful) {
          val allItems = response.body() ?: emptyList()
          val stallItems = allItems.filter { it.stall_name == args.storeName }

          if (isPolling) {
            adapter?.syncStock(stallItems)
            updateTotals(adapter?.getItems() ?: emptyList())
          } else {
            currentStallItems = stallItems
            adapter?.updateData(stallItems)
            updateTotals(stallItems)
          }

          validateCart(token)
        } else {
          Log.e("CART_DETAILS", "Error: ${response.code()}")
        }
      } catch (e: Exception) {
        Log.e("CART_DETAILS", "Exception", e)
      }
    }
  }

  private fun validateCart(token: String) {
    viewLifecycleOwner.lifecycleScope.launch {
      try {
        val response = RetrofitClient.instance.validateCart("Bearer $token")
        if (response.isSuccessful) {
          val result = response.body() ?: return@launch
          adapter?.markUnavailableItems(result.unavailableItems)

          when {
            result.stallInactive -> showStallBanner("This stall is no longer available.", "#B71C1C")
            result.stallClosed -> showStallBanner("This stall is currently closed.", "#E65100")
            else -> hideStallBanner()
          }
        }
      } catch (e: Exception) {
        Log.e("CART_DETAILS", "Validate exception", e)
      }
    }
  }

  private fun showStallBanner(message: String, colorHex: String) {
    view?.findViewById<TextView>(R.id.tvStallStatusBanner)?.apply {
      text = message
      setBackgroundColor(android.graphics.Color.parseColor(colorHex))
      visibility = View.VISIBLE
    }
  }

  private fun hideStallBanner() {
    view?.findViewById<TextView>(R.id.tvStallStatusBanner)?.visibility = View.GONE
  }

  private fun placeOrder() {
    val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val token = sharedPref.getString("auth_token", null) ?: return

    if (adapter?.hasUnavailableItems() == true) {
      Toast.makeText(
        requireContext(),
        "Please remove unavailable items before placing your order.",
        Toast.LENGTH_LONG
      ).show()
      return
    }

    androidx.appcompat.app.AlertDialog.Builder(requireContext())
      .setTitle("Confirm Order")
      .setMessage("Are you sure you want to place this order?")
      .setPositiveButton("Yes, Place Order") { _, _ ->
        submitOrder(token)
      }
      .setNegativeButton("Cancel", null)
      .show()
  }

  private fun submitOrder(token: String) {
    viewLifecycleOwner.lifecycleScope.launch {
      try {
        val validationResponse = RetrofitClient.instance.validateCart("Bearer $token")
        if (validationResponse.isSuccessful) {
          val result = validationResponse.body()
          when {
            result?.stallInactive == true -> {
              Toast.makeText(requireContext(), "This stall is no longer available.", Toast.LENGTH_LONG).show()
              return@launch
            }
            result?.stallClosed == true -> {
              Toast.makeText(requireContext(), "This stall is currently closed.", Toast.LENGTH_LONG).show()
              return@launch
            }
          }
        }
      } catch (e: Exception) {
        Log.e("CART_DETAILS", "Validation exception", e)
      }

      val itemsToOrder = adapter?.getItems() ?: emptyList()
      if (itemsToOrder.isEmpty()) {
        Toast.makeText(requireContext(), "Cart is empty", Toast.LENGTH_SHORT).show()
        return@launch
      }

      val stallId = itemsToOrder.firstOrNull()?.stall_id ?: 0
      val remarks = etOrderRemarks?.text?.toString()?.trim() ?: ""
      val request = PlaceOrderRequest(stall_id = stallId, payment_type = "cash", order_remarks = remarks)

      try {
        val response = RetrofitClient.instance.placeOrder("Bearer $token", request)
        if (response.isSuccessful) {
          val order = response.body()?.order

          val itemsSummary = itemsToOrder.joinToString("\n") {
            "${it.quantity}x ${it.name}  ₱${String.format(Locale.getDefault(), "%.2f", it.price * it.quantity)}"
          }
          val total = itemsToOrder.sumOf { it.price * it.quantity }

          ReceiptBottomSheetFragment.newInstance(
            orderId = order?.order_id ?: 0,
            stallName = args.storeName,
            items = itemsSummary,
            total = total,
            payment = "Cash",
            remarks = remarks
          ).show(parentFragmentManager, "ReceiptBottomSheet")

        } else {
          val errorMsg = response.errorBody()?.string() ?: ""
          Log.e("PLACE_ORDER", "Error: $errorMsg")
          val message = when {
            errorMsg.contains("closed", ignoreCase = true) -> "This stall is currently closed."
            errorMsg.contains("available", ignoreCase = true) -> "This stall is no longer available."
            else -> "Failed to place order."
          }
          Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
      } catch (e: Exception) {
        Log.e("PLACE_ORDER", "Exception", e)
        Toast.makeText(requireContext(), "Connection error", Toast.LENGTH_SHORT).show()
      }
    }
  }

  private fun updateTotals(items: List<FoodItem>) {
    val total = items.sumOf { it.price * it.quantity }
    val priceString = String.format(Locale.getDefault(), "₱%.2f", total)
    tvTotalBottom?.text = priceString
    tvItemCount?.text = "${items.size} items"
  }
}