package com.example.pdfscanner.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionUtils {

    var permissions: Array<String> = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )


    var notificationsPermission: Array<String> = arrayOf(
        Manifest.permission.POST_NOTIFICATIONS
    )

    var permissions13: Array<String> = arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO
    )


    fun hasPermissions(context: Context?, permissions: Array<String>): Boolean {
        if (permissions != null) {
            for (per in permissions) {
                if (ContextCompat.checkSelfPermission(
                        context!!,
                        per
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return true
                }
            }
        }
        return false
    }

}