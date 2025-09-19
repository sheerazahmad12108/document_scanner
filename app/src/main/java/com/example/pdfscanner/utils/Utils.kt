package com.example.pdfscanner.utils

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.provider.Settings
import android.util.Log
import android.view.Window
import android.view.WindowManager.LayoutParams
import androidx.core.content.edit
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.example.pdfscanner.R


object Utils {
    fun isNotificationServiceRunning(context: Context): Boolean {
        val contentResolver = context.contentResolver
        val enabledNotificationListeners =
            Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        val packageName = context.packageName
        return enabledNotificationListeners != null && enabledNotificationListeners.contains(
            packageName
        )
    }

    fun appInstalledOrNot(context: Context, uri: String?): Boolean {
        val pm = context.packageManager
        try {
            pm.getPackageInfo(uri!!, PackageManager.GET_ACTIVITIES)
            return true
        } catch (_: PackageManager.NameNotFoundException) {
            return false
        }
    }

    fun shareApp(context: Context) {
        val myapp = Intent(Intent.ACTION_SEND)
        myapp.type = "text/plain"
        myapp.putExtra(
            Intent.EXTRA_TEXT, """Download this awesome app
 https://play.google.com/store/apps/details?id=${context.packageName} 
"""
        )
        context.startActivity(myapp)
    }

    fun rateUs(context: Context) {
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    ("market://details?id=" + context.packageName).toUri()
                )
            )
        } catch (_: ActivityNotFoundException) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    ("https://play.google.com/store/apps/details?id=" + context.packageName).toUri()
                )
            )
        }
    }

    fun showLoader(context: Context): Dialog {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.loader_screen)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
//        dialog.show()

        return dialog
    }

    fun getPrice(context: Context){
        val billingClient = BillingClient.newBuilder(context)
            .setListener { _, _ -> }
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val params = QueryProductDetailsParams.newBuilder()
                        .setProductList(
                            listOf(
                                QueryProductDetailsParams.Product.newBuilder()
                                    .setProductId(context.resources.getString(R.string.product_id)) // Your product ID
                                    .setProductType(BillingClient.ProductType.SUBS)
                                    .build()
                            )
                        ).build()

                    billingClient.queryProductDetailsAsync(params) { result, productDetailsList ->
                        if (result.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                            val offerDetails = productDetailsList[0]
                                .subscriptionOfferDetails?.firstOrNull()

                            val price = offerDetails
                                ?.pricingPhases
                                ?.pricingPhaseList
                                ?.lastOrNull { it.priceAmountMicros > 0L }
                                ?.formattedPrice

                            Log.d("PRICE", "Price is: $price")
                            val prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
                            prefs.edit { putString("price", price.toString()) }
//                            myPrice = price.toString()
                        }
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Retry connection if needed
            }
        })
    }

    fun Context.isPremiumUser(): Boolean {
        val prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        return prefs.getBoolean("is_premium", false)
    }

    fun checkSubscriptionStatus(context: Context) {
        val billingClient = BillingClient.newBuilder(context)
            .enablePendingPurchases()
            .setListener { _, _ -> }
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val params = QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()

                    billingClient.queryPurchasesAsync(params) { _, purchasesList ->
                        val isSubscribed = purchasesList.any { purchase ->
                            purchase.products.contains(context.resources.getString(R.string.product_id))
                        }
                        savePremiumStatus(isSubscribed, context)
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Optional: handle retry
            }
        })
    }

    private fun savePremiumStatus(isPremium: Boolean, context: Context) {
        val prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        prefs.edit { putBoolean("is_premium", isPremium) }
    }

}