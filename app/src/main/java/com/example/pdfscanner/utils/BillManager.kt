package com.example.pdfscanner.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.SkuDetailsParams
import androidx.core.content.edit
import com.example.pdfscanner.R

class BillingManager(private val context: Context) {
    private var billingClient: BillingClient

    private val purchaseUpdateListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                if (purchase.skus.contains(context.resources.getString(R.string.product_id))) {
                    handlePurchase(purchase)
                }
            }
        }
    }

    init {
        billingClient = BillingClient.newBuilder(context)
            .setListener(purchaseUpdateListener)
            .enablePendingPurchases()
            .build()
        startConnection()
    }

    private fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryPurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Retry connection if needed
            }
        })
    }

    fun launchPurchase(activity: Activity) {
        val skuList = listOf(context.resources.getString(R.string.product_id))
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS)

        billingClient.querySkuDetailsAsync(params.build()) { billingResult, skuDetailsList ->
            if (!skuDetailsList.isNullOrEmpty()) {
                Log.d("App_purchase_Price", skuDetailsList[0].price)
                val flowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetailsList[0])
                    .build()
                billingClient.launchBillingFlow(activity, flowParams)
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        // Verify and grant entitlement
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            savePremiumStatus(true)
        }

    }

    private fun queryPurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val isPremium = purchasesList.any { it.products.contains(context.resources.getString(R.string.product_id)) }
                savePremiumStatus(isPremium)
            }
        }
    }

    private fun savePremiumStatus(isPremium: Boolean) {
        val prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        prefs.edit { putBoolean("is_premium", isPremium) }
    }
}
