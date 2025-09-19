package com.example.pdfscanner.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.database.CursorWindow
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.pdfscanner.R
import com.example.pdfscanner.SharedViewModel
import com.example.pdfscanner.adapters.ViewPagerAdapterNew
import com.example.pdfscanner.databinding.FragmentHistoryBinding
import com.example.pdfscanner.fragments.historytabs.ImageHistoryFragment
import com.example.pdfscanner.fragments.historytabs.PdfHistoryFragment
import com.google.android.material.tabs.TabLayoutMediator
import java.lang.reflect.Field


class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var adapterNew: ViewPagerAdapterNew
    private lateinit var sharedViewModel: SharedViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    @SuppressLint("DiscouragedPrivateApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val window: Window? = requireActivity().window
        window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.white)

        WindowInsetsControllerCompat(window!!, window.decorView).isAppearanceLightStatusBars = true

        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        try {
            val field: Field = CursorWindow::class.java.getDeclaredField("sCursorWindowSize")
            field.isAccessible = true
            field.set(null, 100 * 1024 * 1024) //the 100MB is the new size
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val fragments = listOf(
            PdfHistoryFragment(),
            ImageHistoryFragment()
        )

        adapterNew = ViewPagerAdapterNew(requireActivity(), fragments)
        binding.vpHistory.adapter = adapterNew

        TabLayoutMediator(binding.tabs, binding.vpHistory) { tab, position ->
            tab.text = when (position) {
                0 -> resources.getString(R.string.pdf_new)
                1 -> resources.getString(R.string.jpg_new)
                else -> null
            }
        }.attach()

        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.ivMergeIcon.setOnClickListener {
            showCustomPopup(it)
        }
    }

    private fun showPopupMenu(anchor: View) {
        val popupMenu = PopupMenu(requireContext(), anchor)
        popupMenu.menuInflater.inflate(R.menu.history_menu, popupMenu.menu)

        // Show icons in popup (force show using reflection)
        try {
            val fields = popupMenu.javaClass.declaredFields
            for (field in fields) {
                if ("mPopup" == field.name) {
                    field.isAccessible = true
                    val menuPopupHelper = field.get(popupMenu)
                    val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
                    val setForceIcons = classPopupHelper.getMethod("setForceShowIcon", Boolean::class.java)
                    setForceIcons.invoke(menuPopupHelper, true)
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_filter -> {
                    Toast.makeText(context, "Filter clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.action_sort -> {
                    Toast.makeText(context, "Sort by clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.action_grid -> {
                    Toast.makeText(context, "Grid clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun showCustomPopup(anchorView: View) {
        val inflater = LayoutInflater.from(requireContext())
        val popupView = inflater.inflate(R.layout.custom_menu_popup, null)

        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.elevation = 10f

        // Click listeners
        popupView.findViewById<LinearLayout>(R.id.menu_filter).setOnClickListener {
            sharedViewModel.onFilterClicked()
//            Toast.makeText(requireContext(), "Filter clicked", Toast.LENGTH_SHORT).show()
            popupWindow.dismiss()
        }

        popupView.findViewById<LinearLayout>(R.id.menu_sort).setOnClickListener {
            sharedViewModel.onSortClicked()
            popupWindow.dismiss()
        }

        popupView.findViewById<LinearLayout>(R.id.menu_grid).setOnClickListener {
            Toast.makeText(requireContext(), "Grid clicked", Toast.LENGTH_SHORT).show()
            popupWindow.dismiss()
        }

        // Show below the anchor view
        popupWindow.showAsDropDown(anchorView, -250, 30, Gravity.END)
    }

}