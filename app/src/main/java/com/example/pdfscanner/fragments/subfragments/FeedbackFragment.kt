package com.example.pdfscanner.fragments.subfragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.findNavController
import com.example.pdfscanner.R
import com.example.pdfscanner.databinding.FragmentFeedbackBinding
import com.example.pdfscanner.utils.Utils.isPremiumUser
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds


class FeedbackFragment : Fragment() {

    private lateinit var binding: FragmentFeedbackBinding
    private var problemEncountered: String = ""
    private var btnEnable: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentFeedbackBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val window: Window? = requireActivity().window
        window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.status_bar)

        WindowInsetsControllerCompat(window!!, window.decorView).isAppearanceLightStatusBars = true

        if(requireContext().isPremiumUser()){
            binding.adView.visibility = View.GONE
        } else {
            MobileAds.initialize(requireContext()) {}
            val adRequest = AdRequest.Builder().build()
            binding.adView.loadAd(adRequest)
            binding.adView.visibility = View.VISIBLE
        }

        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.tvSubmit.setOnClickListener {

            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_EMAIL, arrayOf("info@connect-techno.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Feedback")
                putExtra(Intent.EXTRA_TEXT, "Problem Encountered: $problemEncountered. \n More Details:${
                    binding.edtFeedback.text.trim()
                }.")
                type = "message/rfc822" // Mime type for email
            }

            startActivityForResult(sendIntent,101)
        }

        binding.edtFeedback.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

            override fun afterTextChanged(s: Editable?) {
                enableSubmitButton(btnEnable, binding.edtFeedback)
            }
        })

        binding.mcvCrash.setOnClickListener {
            problemEncountered = binding.tvCrash.text.toString()
            btnEnable = true
            enableSubmitButton(true, binding.edtFeedback)

            binding.mcvCrash.setCardBackgroundColor(resources.getColor(R.color.red))
            binding.tvCrash.setTextColor(resources.getColor(R.color.white))

            binding.mcvAppNotResponding.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvAppNotResponding.setTextColor(resources.getColor(R.color.black))

            binding.mcvAdds.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvAdds.setTextColor(resources.getColor(R.color.black))

            binding.mcvFunctionalDisable.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvFunctionalDisable.setTextColor(resources.getColor(R.color.black))

            binding.mcvFilterNotApplied.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvFilterNotApplied.setTextColor(resources.getColor(R.color.black))

            binding.mcvPremiumNotWorking.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvPremiumNotWorking.setTextColor(resources.getColor(R.color.black))

            binding.mcvHowToUse.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvHowToUse.setTextColor(resources.getColor(R.color.black))

            binding.mcvSuggestions.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvSuggestions.setTextColor(resources.getColor(R.color.black))

        }

        binding.mcvAppNotResponding.setOnClickListener {
            problemEncountered = binding.tvAppNotResponding.text.toString()
            btnEnable = true
            enableSubmitButton(true, binding.edtFeedback)

            binding.mcvAppNotResponding.setCardBackgroundColor(resources.getColor(R.color.red))
            binding.tvAppNotResponding.setTextColor(resources.getColor(R.color.white))

            binding.mcvCrash.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvCrash.setTextColor(resources.getColor(R.color.black))

            binding.mcvAdds.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvAdds.setTextColor(resources.getColor(R.color.black))

            binding.mcvFunctionalDisable.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvFunctionalDisable.setTextColor(resources.getColor(R.color.black))

            binding.mcvFilterNotApplied.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvFilterNotApplied.setTextColor(resources.getColor(R.color.black))

            binding.mcvPremiumNotWorking.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvPremiumNotWorking.setTextColor(resources.getColor(R.color.black))

            binding.mcvHowToUse.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvHowToUse.setTextColor(resources.getColor(R.color.black))

            binding.mcvSuggestions.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvSuggestions.setTextColor(resources.getColor(R.color.black))

        }

        binding.mcvAdds.setOnClickListener {
            problemEncountered = binding.tvAdds.text.toString()
            btnEnable = true
            enableSubmitButton(true, binding.edtFeedback)

            binding.mcvAdds.setCardBackgroundColor(resources.getColor(R.color.red))
            binding.tvAdds.setTextColor(resources.getColor(R.color.white))

            binding.mcvCrash.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvCrash.setTextColor(resources.getColor(R.color.black))

            binding.mcvAppNotResponding.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvAppNotResponding.setTextColor(resources.getColor(R.color.black))

            binding.mcvFunctionalDisable.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvFunctionalDisable.setTextColor(resources.getColor(R.color.black))

            binding.mcvFilterNotApplied.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvFilterNotApplied.setTextColor(resources.getColor(R.color.black))

            binding.mcvPremiumNotWorking.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvPremiumNotWorking.setTextColor(resources.getColor(R.color.black))

            binding.mcvHowToUse.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvHowToUse.setTextColor(resources.getColor(R.color.black))

            binding.mcvSuggestions.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvSuggestions.setTextColor(resources.getColor(R.color.black))

        }

        binding.mcvFunctionalDisable.setOnClickListener {
            problemEncountered = binding.tvFunctionalDisable.text.toString()
            btnEnable = true
            enableSubmitButton(true, binding.edtFeedback)

            binding.mcvFunctionalDisable.setCardBackgroundColor(resources.getColor(R.color.red))
            binding.tvFunctionalDisable.setTextColor(resources.getColor(R.color.white))

            binding.mcvCrash.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvCrash.setTextColor(resources.getColor(R.color.black))

            binding.mcvAppNotResponding.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvAppNotResponding.setTextColor(resources.getColor(R.color.black))

            binding.mcvAdds.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvAdds.setTextColor(resources.getColor(R.color.black))

            binding.mcvFilterNotApplied.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvFilterNotApplied.setTextColor(resources.getColor(R.color.black))

            binding.mcvPremiumNotWorking.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvPremiumNotWorking.setTextColor(resources.getColor(R.color.black))

            binding.mcvHowToUse.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvHowToUse.setTextColor(resources.getColor(R.color.black))

            binding.mcvSuggestions.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvSuggestions.setTextColor(resources.getColor(R.color.black))

        }

        binding.mcvFilterNotApplied.setOnClickListener {
            problemEncountered = binding.tvFilterNotApplied.text.toString()
            btnEnable = true
            enableSubmitButton(true, binding.edtFeedback)

            binding.mcvFilterNotApplied.setCardBackgroundColor(resources.getColor(R.color.red))
            binding.tvFilterNotApplied.setTextColor(resources.getColor(R.color.white))

            binding.mcvCrash.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvCrash.setTextColor(resources.getColor(R.color.black))

            binding.mcvAppNotResponding.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvAppNotResponding.setTextColor(resources.getColor(R.color.black))

            binding.mcvAdds.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvAdds.setTextColor(resources.getColor(R.color.black))

            binding.mcvFunctionalDisable.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvFunctionalDisable.setTextColor(resources.getColor(R.color.black))

            binding.mcvPremiumNotWorking.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvPremiumNotWorking.setTextColor(resources.getColor(R.color.black))

            binding.mcvHowToUse.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvHowToUse.setTextColor(resources.getColor(R.color.black))

            binding.mcvSuggestions.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvSuggestions.setTextColor(resources.getColor(R.color.black))

        }

        binding.mcvPremiumNotWorking.setOnClickListener {
            problemEncountered = binding.tvPremiumNotWorking.text.toString()
            btnEnable = true
            enableSubmitButton(true, binding.edtFeedback)

            binding.mcvPremiumNotWorking.setCardBackgroundColor(resources.getColor(R.color.red))
            binding.tvPremiumNotWorking.setTextColor(resources.getColor(R.color.white))

            binding.mcvCrash.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvCrash.setTextColor(resources.getColor(R.color.black))

            binding.mcvAppNotResponding.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvAppNotResponding.setTextColor(resources.getColor(R.color.black))

            binding.mcvAdds.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvAdds.setTextColor(resources.getColor(R.color.black))

            binding.mcvFilterNotApplied.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvFilterNotApplied.setTextColor(resources.getColor(R.color.black))

            binding.mcvFunctionalDisable.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvFunctionalDisable.setTextColor(resources.getColor(R.color.black))

            binding.mcvHowToUse.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvHowToUse.setTextColor(resources.getColor(R.color.black))

            binding.mcvSuggestions.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvSuggestions.setTextColor(resources.getColor(R.color.black))

        }

        binding.mcvHowToUse.setOnClickListener {
            problemEncountered = binding.tvHowToUse.text.toString()
            btnEnable = true
            enableSubmitButton(true, binding.edtFeedback)

            binding.mcvHowToUse.setCardBackgroundColor(resources.getColor(R.color.red))
            binding.tvHowToUse.setTextColor(resources.getColor(R.color.white))

            binding.mcvCrash.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvCrash.setTextColor(resources.getColor(R.color.black))

            binding.mcvAppNotResponding.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvAppNotResponding.setTextColor(resources.getColor(R.color.black))

            binding.mcvAdds.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvAdds.setTextColor(resources.getColor(R.color.black))

            binding.mcvFunctionalDisable.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvFunctionalDisable.setTextColor(resources.getColor(R.color.black))

            binding.mcvPremiumNotWorking.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvPremiumNotWorking.setTextColor(resources.getColor(R.color.black))

            binding.mcvFilterNotApplied.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvFilterNotApplied.setTextColor(resources.getColor(R.color.black))

            binding.mcvSuggestions.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvSuggestions.setTextColor(resources.getColor(R.color.black))

        }

        binding.mcvSuggestions.setOnClickListener {
            problemEncountered = binding.tvSuggestions.text.toString()
            btnEnable = true
            enableSubmitButton(true, binding.edtFeedback)

            binding.mcvSuggestions.setCardBackgroundColor(resources.getColor(R.color.red))
            binding.tvSuggestions.setTextColor(resources.getColor(R.color.white))

            binding.mcvCrash.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvCrash.setTextColor(resources.getColor(R.color.black))

            binding.mcvAppNotResponding.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvAppNotResponding.setTextColor(resources.getColor(R.color.black))

            binding.mcvAdds.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvAdds.setTextColor(resources.getColor(R.color.black))

            binding.mcvFunctionalDisable.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvFunctionalDisable.setTextColor(resources.getColor(R.color.black))

            binding.mcvPremiumNotWorking.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvPremiumNotWorking.setTextColor(resources.getColor(R.color.black))

            binding.mcvHowToUse.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvHowToUse.setTextColor(resources.getColor(R.color.black))

            binding.mcvFilterNotApplied.setCardBackgroundColor(resources.getColor(R.color.light_gray_new))
            binding.tvFilterNotApplied.setTextColor(resources.getColor(R.color.black))

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            101 -> {
                findNavController().popBackStack()
            }
        }
    }

    fun enableSubmitButton(btnEnable: Boolean, editText: EditText) {
        if (btnEnable && editText.text.isNotEmpty()) {
            binding.tvSubmit.isEnabled = true
//            binding.tvSubmit.setTextColor(resources.getColor(R.color.white))
//            binding.tvSubmit.setBackgroundResource(R.drawable.btn_save_bg)
        } else {
            binding.tvSubmit.isEnabled = false
//            binding.tvSubmit.setTextColor(resources.getColor(R.color.light_gray))
//            binding.tvSubmit.setBackgroundResource(R.drawable.btn_cancel_bg_new)
        }

    }

}