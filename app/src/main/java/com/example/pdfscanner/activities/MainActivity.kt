package com.example.pdfscanner.activities

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.pdfscanner.R
import com.example.pdfscanner.databinding.ActivityMainBinding
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale

class MainActivity : AppCompatActivity() {

    val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    private lateinit var preferences: android.content.SharedPreferences
    private var positionSelected: Int = 0
    private var languageSelected: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val window: Window = window


// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)


// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)


// finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this, android.R.color.white))

        preferences = getSharedPreferences("Settings", MODE_PRIVATE)
        positionSelected = preferences.getInt("app_position", 0)
        languageSelected = preferences.getString("app_lang", "")!!

        setLocale(languageSelected, positionSelected)

        setContentView(binding.root)


        MobileAds.initialize(this) { /* optional status callback */ }
        bottomNav = binding.bottomNav
        // Find the NavController
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController
        // Set up Bottom Navigation Bar with NavController
        bottomNav.setupWithNavController(navController)
        // Enhance user experience by clearing the back stack
        bottomNavItemChangeListener(bottomNav)
//        navController.addOnDestinationChangedListener { controler, destination, argument ->
//            if (controler.currentDestination?.id == R.id.extractTextFragment) {
//                bottomNav.visibility = View.GONE
//            } else if (controler.currentDestination?.id == R.id.signatureFragment) {
//                bottomNav.visibility = View.GONE
//            } else if (controler.currentDestination?.id == R.id.idCardScannerFragment) {
//                bottomNav.visibility = View.GONE
//            } else if (controler.currentDestination?.id == R.id.actionFragment) {
//                bottomNav.visibility = View.GONE
//            } else if (controler.currentDestination?.id == R.id.fileViewerFragment) {
//                bottomNav.visibility = View.GONE
//            } else {
//                bottomNav.visibility = View.VISIBLE
//            }
//        }

    }

    private fun bottomNavItemChangeListener(navView: BottomNavigationView) {
        navView.setOnItemSelectedListener { item ->
            if (item.itemId != navView.selectedItemId) {
                navController.popBackStack(item.itemId, inclusive = true, saveState = false)
                navController.navigate(item.itemId)
            }
            true
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
}