package com.example.pdfscanner.activities

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Window
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.WindowInsetsControllerCompat
import com.example.pdfscanner.R
import com.example.pdfscanner.databinding.ActivityPermissionManagementBinding
import kotlin.apply
import kotlin.jvm.java

class PermissionManagementActivity : AppCompatActivity() {

    private val manageFilesLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                startActivity(
                    Intent(
                        this@PermissionManagementActivity,
                        MainActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
                finish()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    val binding by lazy {
        ActivityPermissionManagementBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val window: Window? = window
        window?.statusBarColor = ContextCompat.getColor(this, R.color.status_bar)

        WindowInsetsControllerCompat(window!!, window.decorView).isAppearanceLightStatusBars = true

        binding.btnAllow.setOnClickListener {
            managePermissions()
        }
    }

    private fun managePermissions() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show()
            } else {
                val manageFilesIntent = getStoragePermissionIntent()
                manageFilesLauncher.launch(manageFilesIntent)
            }
        }

    }

    fun getStoragePermissionIntent(): Intent {
        return if (SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = "package:${packageName}".toUri()
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
        }
    }
}