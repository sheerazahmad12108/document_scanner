package com.example.pdfscanner.utils

import android.app.Application
import com.example.pdfscanner.utils.Utils.checkSubscriptionStatus
import com.example.pdfscanner.utils.Utils.getPrice

class ApplicationClass : Application() {

    companion object {
        var preference: Preference? = null
        lateinit var instance: ApplicationClass
            private set

    }

    override fun onCreate() {
        super.onCreate()
        instance=this
        preference = Preference(applicationContext)
        getPrice(applicationContext)
        checkSubscriptionStatus(applicationContext)
//        Fresco.initialize(this)
//        Firebase.initialize(this)

    }

}