package com.example.pdfscanner.fragments

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.RatingBar.OnRatingBarChangeListener
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pdfscanner.R
import com.example.pdfscanner.activities.LanguageActivity
import com.example.pdfscanner.databinding.DialogRatingBinding
import com.example.pdfscanner.databinding.FragmentSettingsBinding
import com.example.pdfscanner.utils.Utils.isPremiumUser
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private var positionSelected: Int = 0
    private var languageSelected: String = ""
    private var language = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val window: Window? = requireActivity().window
        window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.status_bar)

        WindowInsetsControllerCompat(window!!, window.decorView).isAppearanceLightStatusBars = true

        binding = FragmentSettingsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(requireContext().isPremiumUser()){
            binding.adView.visibility = View.GONE
            binding.rlPremium.visibility = View.GONE
        } else {
            MobileAds.initialize(requireContext()) {}
            val adRequest = AdRequest.Builder().build()
            binding.adView.loadAd(adRequest)
            binding.adView.visibility = View.VISIBLE
            binding.rlPremium.visibility = View.VISIBLE
        }

        getList()

        val preferences = requireContext().getSharedPreferences("Settings", MODE_PRIVATE)
        positionSelected = preferences.getInt("app_position", 0)
        languageSelected = preferences.getString("app_lang", "")!!

        binding.tvSelectedLanguage.text = language[positionSelected]

        binding.rlLanguage.setOnClickListener {
            startActivity(
                Intent(
                    activity,
                    LanguageActivity::class.java
                )
            )
        }

        binding.rlPrivacy.setOnClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                "https://sites.google.com/view/connect-apps-privacy-policy/home".toUri()
            )
            startActivity(browserIntent)
        }

        binding.rlSendfeedback.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_settings_to_feedbackFragment)
        }

        binding.rlRateApp.setOnClickListener {
//            Toast.makeText(requireContext(),"Clicked", Toast.LENGTH_LONG).show()
//            showInAppReview(requireActivity())
            showInfoDialog(requireContext())
        }

        binding.rlPremium.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_settings_to_purchaseFragment)
        }

        binding.rlFavorite.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_settings_to_navigation_favorite)
        }

        binding.rlShare.setOnClickListener {
            shareApp()
        }

        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
        var pinfo: PackageInfo? = null
        try {
            pinfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
//        val versionNumber: Int = pinfo?.versionCode ?: 0
        val versionName: String = pinfo?.versionName ?: ""

        "Version: $versionName".also { binding.tvVersionCode.text = it }

    }

    private fun getList() {
        language.add(resources.getString(R.string.english))
        language.add(resources.getString(R.string.china))
        language.add(resources.getString(R.string.french))
        language.add(resources.getString(R.string.arabic))
        language.add(resources.getString(R.string.dutch))
        language.add(resources.getString(R.string.hindi))
        language.add(resources.getString(R.string.spanish))
        language.add(resources.getString(R.string.bengali))
        language.add(resources.getString(R.string.russian))
        language.add(resources.getString(R.string.portuguese))
        language.add(resources.getString(R.string.urdu))
        language.add(resources.getString(R.string.indonesian))
        language.add(resources.getString(R.string.japanese))
        language.add(resources.getString(R.string.turkish))
        language.add(resources.getString(R.string.korean))
        language.add(resources.getString(R.string.english))
    }

    private fun shareApp() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.setType("text/plain")
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My application name")
            var shareMessage = "\nLet me recommend you this application\n\n"
            shareMessage =
                shareMessage + "https://play.google.com/store/apps/details?id=" + "com.all.clear.pdf.scanner.mobile.app" + "\n\n"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "choose one"))
        } catch (_: Exception) {
            //e.toString();
        }
    }

    private fun showInfoDialog(context: Context) {
        val dialog = Dialog(context)
        val dialogBinding: DialogRatingBinding = DialogRatingBinding.inflate(
            LayoutInflater.from(context)
        )
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window!!.setLayout(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
        )
        dialogBinding.ratingBar.onRatingBarChangeListener =
            OnRatingBarChangeListener { _, rating, _ ->
                if (rating == 5.0f || rating == 4.0f) {
                    dialogBinding.ivEmoji.setImageResource(R.drawable.emoji_extra_happy)
                    dialogBinding.btnRatings.text = resources.getString(R.string.rate_on_playstore)
                } else if (rating == 3.0f || rating == 2.0f) {
                    dialogBinding.ivEmoji.setImageResource(R.drawable.emoji_happy)
                    dialogBinding.btnRatings.text = resources.getString(R.string.rate)
                } else {
                    dialogBinding.ivEmoji.setImageResource(R.drawable.emoji_sad)
                    dialogBinding.btnRatings.text = resources.getString(R.string.rate)
                }
                Log.d("rating: ", dialogBinding.ratingBar.rating.toString())
            }
        dialogBinding.btnRatings.setOnClickListener {
            if (dialogBinding.ratingBar.rating > 3.0f) {
                val uri = "market://details?id=${requireContext().packageName}".toUri()
                val goToMarket = Intent(Intent.ACTION_VIEW, uri)
                goToMarket.addFlags(
                    Intent.FLAG_ACTIVITY_NO_HISTORY or
                            Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                )
                try {
                    startActivity(goToMarket)
                } catch (_: ActivityNotFoundException) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            "https://play.google.com/store/apps/details?id=${requireContext().packageName}".toUri()
                        )
                    )
                }
            } else if (dialogBinding.ratingBar.rating > 0 && dialogBinding.ratingBar.rating <= 3.0) {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("info@connect-techno.com"))
                    putExtra(Intent.EXTRA_SUBJECT, "Feedback")
                    type = "message/rfc822" // Mime type for email
                }

                startActivity(sendIntent)
            }
        }

        dialog.show()
    }
}