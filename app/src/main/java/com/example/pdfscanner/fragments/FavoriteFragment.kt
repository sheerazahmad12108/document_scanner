package com.example.pdfscanner.fragments

import android.database.CursorWindow
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.findNavController
import com.example.pdfscanner.R
import com.example.pdfscanner.adapters.FavoriteViewPagerAdapter
import com.example.pdfscanner.adapters.FavoriteViewPagerAdapterNew
import com.example.pdfscanner.adapters.ViewPagerAdapterNew
import com.example.pdfscanner.databinding.FragmentFavoriteBinding
import com.example.pdfscanner.fragments.favoritetabs.ImageFavoriteFragment
import com.example.pdfscanner.fragments.favoritetabs.PDFFavoriteFragment
import com.example.pdfscanner.fragments.historytabs.ImageHistoryFragment
import com.example.pdfscanner.fragments.historytabs.PdfHistoryFragment
import com.google.android.material.tabs.TabLayoutMediator
import java.lang.reflect.Field

class FavoriteFragment : Fragment() {

    private lateinit var binding: FragmentFavoriteBinding
    private lateinit var adapter: FavoriteViewPagerAdapter
    private lateinit var adapterNew: FavoriteViewPagerAdapterNew


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFavoriteBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val window: Window? = requireActivity().window
        window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.white)

        WindowInsetsControllerCompat(window!!, window.decorView).isAppearanceLightStatusBars = true

        try {
            val field: Field = CursorWindow::class.java.getDeclaredField("sCursorWindowSize")
            field.isAccessible = true
            field.set(null, 100 * 1024 * 1024) //the 100MB is the new size
        } catch (e: Exception) {
            e.printStackTrace()
        }

//        // Initializing the ViewPagerAdapter
//        adapter = activity?.supportFragmentManager?.let { FavoriteViewPagerAdapter(it) }!!
//
//        // add fragment to the list
//        adapter.addFragment(PDFFavoriteFragment(), resources.getString(R.string.file))
//        adapter.addFragment(ImageFavoriteFragment(), resources.getString(R.string.image))
//        binding.vpFavorite.adapter = adapter
//        binding.tabsFavorite.setupWithViewPager(binding.vpFavorite)

        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        val fragments = listOf(
            PDFFavoriteFragment(),
            ImageFavoriteFragment()
        )

        adapterNew = FavoriteViewPagerAdapterNew(requireActivity(), fragments)
        binding.vpFavorite.adapter = adapterNew

        TabLayoutMediator(binding.tabsFavorite, binding.vpFavorite) { tab, position ->
            tab.text = when (position) {
                0 -> resources.getString(R.string.pdf_new)
                1 -> resources.getString(R.string.jpg_new)
                else -> null
            }
        }.attach()
    }


}