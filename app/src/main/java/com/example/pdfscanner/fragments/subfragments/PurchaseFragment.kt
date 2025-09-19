package com.example.pdfscanner.fragments.subfragments

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.findNavController
import com.example.pdfscanner.R
import com.example.pdfscanner.databinding.FragmentPurchaseBinding
import com.example.pdfscanner.utils.BillingManager

class PurchaseFragment : Fragment() {

    private lateinit var binding: FragmentPurchaseBinding
    private lateinit var billingManager: BillingManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentPurchaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val window: Window? = requireActivity().window
        window?.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.white)

        WindowInsetsControllerCompat(window!!, window.decorView).isAppearanceLightStatusBars = true

        billingManager = BillingManager(requireContext())

        binding.shimmerLayout.startShimmer()

        val preferences = requireContext().getSharedPreferences("Settings", MODE_PRIVATE)
        val price = preferences.getString("price", "").toString()

        binding.tvTry3Days.text = resources.getString(R.string.try_3_days, price)

        binding.ivClose.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.tvPrivacy.setOnClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                "https://sites.google.com/view/connect-apps-privacy-policy/home".toUri()
            )
            startActivity(browserIntent)
        }
        binding.tvTermsUse.setOnClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                "https://sites.google.com/view/connect-apps-term-conditions/home".toUri()
            )
            startActivity(browserIntent)
        }

        binding.tvStartTrail.setOnClickListener {
            billingManager.launchPurchase(requireActivity())
        }

    }

}