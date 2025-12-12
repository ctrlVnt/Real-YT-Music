package com.ctrlvnt.rytm.ui

import android.content.Context
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.SessionProvider


class CastOptionsProvider : com.google.android.gms.cast.framework.OptionsProvider {
    fun getCastOptions(appContext: Context?): CastOptions {
        // Register you custom receiver on the Google Cast SDK Developer Console to get this ID.

        val receiverId = ""

        return Builder()
            .setReceiverApplicationId(receiverId)
            .build()
    }

    fun getAdditionalSessionProviders(context: Context?): MutableList<SessionProvider?>? {
        return null
    }

    override fun getCastOptions(p0: Context): CastOptions {
        TODO("Not yet implemented")
    }

    override fun getAdditionalSessionProviders(p0: Context): List<SessionProvider?>? {
        TODO("Not yet implemented")
    }
}