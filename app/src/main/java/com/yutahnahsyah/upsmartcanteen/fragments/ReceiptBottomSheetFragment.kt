package com.yutahnahsyah.upsmartcanteen.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import androidx.navigation.fragment.NavHostFragment
import com.yutahnahsyah.upsmartcanteen.R
import java.util.Locale

class ReceiptBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(
            orderId: Int,
            stallName: String,
            items: String,
            total: Double,
            payment: String,
            remarks: String
        ): ReceiptBottomSheetFragment {
            return ReceiptBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putInt("orderId", orderId)
                    putString("stallName", stallName)
                    putString("items", items)
                    putDouble("total", total)
                    putString("payment", payment)
                    putString("remarks", remarks)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_receipt_bottom_sheet, container, false)

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? BottomSheetDialog
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            behavior.isDraggable = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = requireArguments()

        view.findViewById<TextView>(R.id.tvReceiptStallName).text = args.getString("stallName")
        view.findViewById<TextView>(R.id.tvReceiptItems).text = args.getString("items")
        view.findViewById<TextView>(R.id.tvReceiptTotal).text =
            String.format(Locale.getDefault(), "₱%.2f", args.getDouble("total"))
        view.findViewById<TextView>(R.id.tvReceiptPayment).text =
            args.getString("payment")?.replaceFirstChar { it.uppercase() }
        view.findViewById<TextView>(R.id.tvReceiptOrderId).text = "#${args.getInt("orderId")}"

        val remarks = args.getString("remarks") ?: ""
        val rowRemarks = view.findViewById<View>(R.id.rowRemarks)
        if (remarks.isBlank()) {
            rowRemarks.visibility = View.GONE
        } else {
            view.findViewById<TextView>(R.id.tvReceiptRemarks).text = remarks
        }

        // X button — clear back stack and go to stalls
        view.findViewById<View>(R.id.btnCloseReceipt).setOnClickListener {
            dismiss()
            val navHostFragment = requireActivity().supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController

            navController.popBackStack(R.id.nav_stall, false)

            requireActivity().findViewById<View>(R.id.bottomNavCard).visibility = View.VISIBLE
            requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav).apply {
                visibility = View.VISIBLE
                menu.findItem(R.id.nav_stall)?.isChecked = true
            }
        }

        // View My Orders button
        view.findViewById<MaterialButton>(R.id.btnReceiptDone).setOnClickListener {
            dismiss()
            val navHostFragment = requireActivity().supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController

            navController.popBackStack(R.id.nav_stall, false)
            navController.navigate(R.id.nav_history)

            requireActivity().findViewById<View>(R.id.bottomNavCard).visibility = View.VISIBLE
            requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav).apply {
                visibility = View.VISIBLE
                menu.findItem(R.id.nav_profile)?.isChecked = true
            }
        }
    }
}