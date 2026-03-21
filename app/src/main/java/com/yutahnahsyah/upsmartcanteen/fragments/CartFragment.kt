package com.yutahnahsyah.upsmartcanteen.fragments

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yutahnahsyah.upsmartcanteen.R
import com.yutahnahsyah.upsmartcanteen.RetrofitClient
import com.yutahnahsyah.upsmartcanteen.adapter.StallCartAdapter
import com.yutahnahsyah.upsmartcanteen.data.model.StallCart
import kotlinx.coroutines.launch

class CartFragment : Fragment() {

    private lateinit var adapter: StallCartAdapter
    private lateinit var rvCart: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // FIXED: Safely handle back button. In fragment_cart.xml, there is no btnBack,
        // but we'll use a safe call just in case it's added later or exists in some versions.
        view.findViewById<View>(R.id.btnBack)?.setOnClickListener {
            findNavController().navigateUp()
        }

        rvCart = view.findViewById(R.id.recyclerView)
        rvCart.layoutManager = LinearLayoutManager(requireContext())
        
        adapter = StallCartAdapter(emptyList()) { selectedStallCart ->
            val bundle = Bundle().apply {
                putString("storeName", selectedStallCart.stallName)
            }
            findNavController().navigate(R.id.action_nav_cart_to_nav_cart_details, bundle)
        }
        rvCart.adapter = adapter

        setupSwipeActions()
        fetchCartItems()
    }

    private fun setupSwipeActions() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val stallCart = adapter.getStallCartAt(position)
                
                if (direction == ItemTouchHelper.LEFT) {
                    deleteStallCart(stallCart, position)
                } else {
                    Log.d("CART_NAV", "Swiping Right for Stall: ${stallCart.stallName} (ID: ${stallCart.stallId})")
                    if (stallCart.stallId != 0) {
                        val bundle = Bundle().apply {
                            putString("storeName", stallCart.stallName)
                            putInt("stallId", stallCart.stallId)
                        }
                        findNavController().navigate(R.id.action_nav_cart_to_nav_store_food, bundle)
                    } else {
                        Toast.makeText(requireContext(), "Error: Invalid Stall ID", Toast.LENGTH_SHORT).show()
                    }
                    adapter.notifyItemChanged(position)
                }
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                val itemView = viewHolder.itemView
                val itemHeight = itemView.bottom - itemView.top
                val p = Paint()
                val cornerRadius = 40f 

                if (dX < 0) { // Swiping Left (Delete)
                    p.color = Color.parseColor("#D32F2F")
                    val background = RectF(itemView.right.toFloat() + dX, itemView.top.toFloat() + 20, itemView.right.toFloat() - 20, itemView.bottom.toFloat() - 20)
                    c.drawRoundRect(background, cornerRadius, cornerRadius, p)
                    
                    val icon = ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_delete)
                    icon?.let {
                        val iconSize = 64
                        val margin = (itemHeight - iconSize) / 2
                        it.setBounds(itemView.right - margin - iconSize, itemView.top + margin, itemView.right - margin, itemView.bottom - margin)
                        it.setTint(Color.WHITE)
                        it.draw(c)
                    }
                } else if (dX > 0) { // Swiping Right (Add)
                    p.color = Color.parseColor("#2D5016")
                    val background = RectF(itemView.left.toFloat() + 20, itemView.top.toFloat() + 20, dX, itemView.bottom.toFloat() - 20)
                    c.drawRoundRect(background, cornerRadius, cornerRadius, p)
                    
                    val icon = ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_input_add)
                    icon?.let {
                        val iconSize = 64
                        val margin = (itemHeight - iconSize) / 2
                        it.setBounds(itemView.left + margin, itemView.top + margin, itemView.left + margin + iconSize, itemView.bottom - margin)
                        it.setTint(Color.WHITE)
                        it.draw(c)
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rvCart)
    }

    private fun deleteStallCart(stallCart: StallCart, position: Int) {
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("auth_token", null)

        if (token == null) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Log.d("CART_DELETE", "Requesting deletion for Stall ID: ${stallCart.stallId}")
                val response = RetrofitClient.instance.clearStallCart("Bearer $token", stallCart.stallId)
                
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Cart for ${stallCart.stallName} deleted", Toast.LENGTH_SHORT).show()
                    fetchCartItems()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("CART_DELETE", "Server Error (${response.code()}): $errorBody")
                    Toast.makeText(requireContext(), "Error deleting cart (Code: ${response.code()})", Toast.LENGTH_SHORT).show()
                    adapter.notifyItemChanged(position)
                }
            } catch (e: Exception) {
                Log.e("CART_DELETE", "Exception", e)
                adapter.notifyItemChanged(position)
            }
        }
    }

    private fun fetchCartItems() {
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("auth_token", null)

        if (token == null) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getMyCart("Bearer $token")
                if (response.isSuccessful) {
                    val items = response.body() ?: emptyList()
                    val groupedCarts = items.groupBy { it.stall_id }
                        .map { (stallId, foodItems) ->
                            StallCart(
                                stallId = stallId,
                                stallName = foodItems.firstOrNull()?.stall_name ?: "Unknown Stall",
                                items = foodItems,
                                subtotal = foodItems.sumOf { it.price }
                            )
                        }
                    adapter.updateData(groupedCarts)
                }
            } catch (e: Exception) {
                Log.e("CART_FETCH", "Exception", e)
            }
        }
    }
}
