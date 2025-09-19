package com.example.pdfscanner.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.example.pdfscanner.R
import com.example.pdfscanner.databinding.ActivitySplashBinding
import com.example.pdfscanner.utils.Utils.isPremiumUser
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale


class SplashActivity : AppCompatActivity() {

    val binding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }
    private var interstitialAd: InterstitialAd? = null
    private var progressStatus = 0
    private val STORAGE_PERMISSION_CODE = 1001
    private lateinit var handler: Handler
    private lateinit var preferences: SharedPreferences

    private val cameraPermissionRequestLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted: proceed with opening the camera
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
//                gotoNext()
                if (SDK_INT > Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        lifecycleScope.launch {
                            delay(1000)
                            startActivity(
                                Intent(
                                    this@SplashActivity,
                                    MainActivity::class.java
                                ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            )
                            finish()
                        }
                    } else {
                        lifecycleScope.launch {
                            delay(1000)
                            startActivity(
                                Intent(
                                    this@SplashActivity,
                                    PermissionManagementActivity::class.java
                                ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            )
                            finish()
                        }
                    }
                } else {
                    checkAndRequestStoragePermissions()
                }
            } else {
                // Permission denied: inform the user to enable it through settings
                Toast.makeText(
                    this,
                    "Go to settings and enable camera permission to use this feature",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private var positionSelected: Int = 0
    private var languageSelected: String = ""
    private var appOpenAd: AppOpenAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val w: Window = window
        w.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        val window: Window = window


// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)


// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)


// finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white))

        handler = Handler(Looper.getMainLooper())

        preferences = getSharedPreferences("Settings", MODE_PRIVATE)
        if (preferences.getBoolean("first_time", true)) {
            preferences.edit {
                putBoolean("first_time", false)
                putBoolean("in_app_review", true)
            }
        }
        positionSelected = preferences.getInt("app_position", 0)
        languageSelected = preferences.getString("app_lang", "")!!

        setLocale(languageSelected, positionSelected)
        binding.tvHeading.text = resources.getString(R.string.pdf_scanner)
        binding.tvSubHeading.text = resources.getString(R.string.scan_documents)
        binding.tvAds.text = resources.getString(R.string.action_contains_advertisement)

//        loadInterstitial()
//        simulateProgress()

        if (isInternetAvailable(this)) {
            if (isPremiumUser()) {
                showAdWithProgressDelay()
                simulateProgress()
            } else {
                loadAppOpenAd()
                simulateProgress()
            }

        } else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun gotoNext() {
        lifecycleScope.launch {
            delay(1000)
            startActivity(
                Intent(
                    this@SplashActivity,
                    MainActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
            finish()

        }
    }

    @SuppressLint("CommitPrefEdits")
    private fun setLocale(language: String, position: Int) {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val configuration = Configuration()
        configuration.locale = locale
        baseContext.resources.updateConfiguration(
            configuration,
            baseContext.resources.displayMetrics
        )

        getSharedPreferences("Settings", MODE_PRIVATE).edit {
            putString("app_lang", language)
            putInt("app_position", position)
        }
    }

    private fun checkAndRequestStoragePermissions() {
        val readPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        val writePermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (readPermission != PackageManager.PERMISSION_GRANTED || writePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                STORAGE_PERMISSION_CODE
            )
        } else {
            gotoNext()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                gotoNext()
            } else {
                // Permissions denied; inform the user about the necessity of permissions
            }
        }
    }

    private fun loadInterstitial() {
        InterstitialAd.load(
            this,
            resources.getString(R.string.interstitial_id),
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            interstitialAd = null
                            main()
//                            showInterstitial()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            interstitialAd = null
                            main()
//                            showInterstitial()
                        }
                    }
                    showAdWithProgressDelay()
                }

                override fun onAdFailedToLoad(loadError: LoadAdError) {
                    interstitialAd = null
                    main()
//                    showInterstitial()
                }
            }
        )
    }


    private fun simulateProgress() {
        Thread {
            while (progressStatus < 90) {
                progressStatus++
                handler.post { binding.progressBar.progress = progressStatus }
                Thread.sleep(30)
            }
        }.start()
    }

    private fun showAdWithProgressDelay() {
        handler.postDelayed({
            binding.progressBar.progress = 100
            interstitialAd?.show(this) ?: main()
//            interstitialAd?.show(this) ?: showInterstitial()
        }, 1000)  // slight delay to reach full
    }

    private fun main() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            cameraPermissionRequestLauncher.launch(Manifest.permission.CAMERA)
        } else {
            if (SDK_INT > Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    lifecycleScope.launch {
                        delay(0)
                        startActivity(
                            Intent(
                                this@SplashActivity,
                                MainActivity::class.java
                            ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        )
                        finish()
                    }
                } else {
                    lifecycleScope.launch {
                        delay(0)
                        startActivity(
                            Intent(
                                this@SplashActivity,
                                PermissionManagementActivity::class.java
                            ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        )
                        finish()
                    }
                }
            } else {
                checkAndRequestStoragePermissions()
            }

        }
    }

    private fun showInterstitial() {
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                main()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitialAd = null
                main()

            }
        }

        if (interstitialAd != null) {
            interstitialAd?.show(this)
        } else {
            main()
        }
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun loadAppOpenAd() {
        val adRequest = AdRequest.Builder().build()
        AppOpenAd.load(
            this,
            resources.getString(R.string.open_app_id),  // Replace with real ID in production
            adRequest,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    Log.d("AppOpenAd", "Ad Loaded")
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            appOpenAd = null
                            main()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            appOpenAd = null
                            main()
                        }
                    }
                    showAdWithProgressDelay()

                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    appOpenAd = null
                    Log.e("AppOpenAd", "Failed to load: ${loadAdError.message}")
                    main()
                }
            }
        )
    }
}