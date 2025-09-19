package com.example.pdfscanner.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pdfscanner.R
import com.example.pdfscanner.adapters.LanguageItemAdapter
import com.example.pdfscanner.databinding.ActivityLanguageBinding
import com.example.pdfscanner.db.LanguageItem
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.util.Locale
import androidx.core.content.edit
import com.example.pdfscanner.utils.Utils.isPremiumUser
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback

class LanguageActivity : AppCompatActivity() {

    val binding by lazy {
        ActivityLanguageBinding.inflate(layoutInflater)
    }

    private lateinit var languageItemAdapter: LanguageItemAdapter
    private lateinit var rvLanguage: RecyclerView
    private lateinit var listLanguage: ArrayList<LanguageItem>
    private lateinit var ivSelectLanguage: ImageView
    private var positionSelected: Int = 0
    private var languageSelected: String = ""
    private var language: String = ""
    private var interstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val window: Window = window


// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)


// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)


// finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar))

        setContentView(binding.root)

        if (isPremiumUser()) {
            binding.adView.visibility = View.GONE
        } else {
            MobileAds.initialize(this)

            InterstitialAd.load(
                this,
                resources.getString(R.string.interstitial_id),
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        interstitialAd = null
                    }
                }
            )


            MobileAds.initialize(this@LanguageActivity) {}
            val adRequest = AdRequest.Builder().build()
            binding.adView.loadAd(adRequest)
            binding.adView.visibility = View.VISIBLE

        }

        val preferences = getSharedPreferences("Settings", MODE_PRIVATE)
        positionSelected = preferences.getInt("app_position", 0)
        languageSelected = preferences.getString("app_lang", "")!!

        when (positionSelected) {
            0 -> {
                language = "en"
            }

            1 -> {
                language = "ar"
            }

            2 -> {
                language = "zh"
            }

            3 -> {
                language = "ur"
            }
        }

//        setLocale(languageSelected, positionSelected)

        val locale = Locale(language)
        Locale.setDefault(locale)

        val configuration = Configuration()
        configuration.locale = locale
        baseContext.resources.updateConfiguration(
            configuration, baseContext.resources.displayMetrics
        )

        binding.tvLanguage.text = getString(R.string.language)
        binding.btnSave.text = getString(R.string.save)

        rvLanguage = findViewById(R.id.rv_language)
        ivSelectLanguage = findViewById(R.id.ivBack)
        getList()
        setAdapter()

        ivSelectLanguage.setOnClickListener {
            onBackPressed()
//            getSharedPreferences("Settings", MODE_PRIVATE).edit {
//                putBoolean("on_boarding", true)
//            }
//            showInterstitial()
        }

        binding.btnSave.setOnClickListener {
            val locale = Locale(language)
            Locale.setDefault(locale)

            val configuration = Configuration()
            configuration.locale = locale
            baseContext.resources.updateConfiguration(
                configuration, baseContext.resources.displayMetrics
            )
            getSharedPreferences("Settings", MODE_PRIVATE).edit {
                putString("app_lang", languageSelected)
                putInt("app_position", positionSelected)
//                putBoolean("on_boarding", true)
            }
//            getSharedPreferences("Settings", MODE_PRIVATE).edit {
//                putBoolean("on_boarding", true)
//            }
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//            finishAffinity()
            if (isPremiumUser()) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finishAffinity()
            } else {
                showInterstitial()
            }
        }

    }

    private fun setAdapter() {

        languageItemAdapter = LanguageItemAdapter(listLanguage)
        rvLanguage.layoutManager = LinearLayoutManager(this)
        rvLanguage.adapter = languageItemAdapter

        languageItemAdapter.setSingleSelection(positionSelected)
        // Applying OnClickListener to our Adapter
        languageItemAdapter.setOnClickListener(object : LanguageItemAdapter.OnClickListener {
            override fun onClick(position: Int, model: LanguageItem?) {
                when (position) {
                    0 -> {
                        setLocale("en", position)
//                        recreate()
                    }

                    1 -> {
                        setLocale("ar", position)
//                        recreate()
                    }

                    2 -> {
                        setLocale("zh", position)
//                        recreate()
                    }
//
                    3 -> {
                        setLocale("ur", position)
//                        recreate()
                    }
//
//                    4 -> {
//                        setLocale("de", position)
//                        recreate()
//                    }
////
//                    5 -> {
//                        setLocale("hi", position)
//                        recreate()
//                    }
////
//                    6 -> {
//                        setLocale("es", position)
//                        recreate()
//                    }
////
//                    7 -> {
//                        setLocale("bn", position)
//                        recreate()
//                    }
////
//                    8 -> {
//                        setLocale("ru", position)
//                        recreate()
//                    }
////
//                    9 -> {
//                        setLocale("pt", position)
//                        recreate()
//                    }
////
//                    10 -> {
//                        setLocale("ur", position)
//                        recreate()
//                    }
////
//                    11 -> {
//                        setLocale("in", position)
//                        recreate()
//                    }
////
//                    12 -> {
//                        setLocale("ja", position)
//                        recreate()
//                    }
////
//                    13 -> {
//                        setLocale("tr", position)
//                        recreate()
//                    }
//
//                    14 -> {
//                        setLocale("ko", position)
//                        recreate()
//                    }
//
//                    15 -> {
//                        setLocale("vi", position)
//                        recreate()
//                    }
//
//                    16 -> {
//                        setLocale("tr", position)
//                        recreate()
//                    }
//
//                    17 -> {
//                        setLocale("ko", position)
//                        recreate()
//                    }
//
//                    18 -> {
//                        setLocale("vi", position)
//                        recreate()
//                    }

                }
//                Toast.makeText(applicationContext, "Clicked Item: $position", Toast.LENGTH_LONG)
//                    .show()
            }
        })
    }

    private fun getList() {
        listLanguage = ArrayList()
        listLanguage.add(LanguageItem(R.drawable.united_states, getString(R.string.english)))
        listLanguage.add(LanguageItem(R.drawable.saudi_arabia, getString(R.string.arabic)))
        listLanguage.add(LanguageItem(R.drawable.china, getString(R.string.china)))
        listLanguage.add(LanguageItem(R.drawable.pakistan, getString(R.string.urdu)))
//        listLanguage.add(LanguageItem(R.drawable.france, getString(R.string.french)))
//        listLanguage.add(LanguageItem(R.drawable.germany, getString(R.string.dutch)))
//        listLanguage.add(LanguageItem(R.drawable.india, getString(R.string.hindi)))
//        listLanguage.add(LanguageItem(R.drawable.spain, getString(R.string.spanish)))
//        listLanguage.add(LanguageItem(R.drawable.bangladesh, getString(R.string.bengali)))
//        listLanguage.add(LanguageItem(R.drawable.russia, getString(R.string.russian)))
//        listLanguage.add(LanguageItem(R.drawable.portugal, getString(R.string.portuguese)))
//        listLanguage.add(LanguageItem(R.drawable.indonesia, getString(R.string.indonesian)))
//        listLanguage.add(LanguageItem(R.drawable.japan, getString(R.string.japanese)))
//        listLanguage.add(LanguageItem(R.drawable.turkey, getString(R.string.turkish)))
//        listLanguage.add(LanguageItem(R.drawable.south_korea, getString(R.string.korean)))
//        listLanguage.add(LanguageItem(R.drawable.vietnam, getString(R.string.vietnamese)))
    }

    @SuppressLint("CommitPrefEdits")
    private fun setLocale(language: String, position: Int) {
//        val locale = Locale(language)
//        Locale.setDefault(locale)
//
//        val configuration = Configuration()
//        configuration.locale = locale
//        baseContext.resources.updateConfiguration(
//            configuration, baseContext.resources.displayMetrics
//        )
//
//        getSharedPreferences("Settings", MODE_PRIVATE).edit {
//            putString("app_lang", language)
//            putInt("app_position", position)
//        }

        languageSelected = language
        positionSelected = position

        this.language = language
    }

    private fun showInterstitial() {
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                val intent = Intent(this@LanguageActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitialAd = null
                val intent = Intent(this@LanguageActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        if (interstitialAd != null) {
            interstitialAd?.show(this@LanguageActivity)
        } else {
            val intent = Intent(this@LanguageActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}