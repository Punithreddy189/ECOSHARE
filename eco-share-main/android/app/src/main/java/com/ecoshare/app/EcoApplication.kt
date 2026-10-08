package com.ecoshare.app

import android.app.Application
import com.ecoshare.app.util.ThemeHelper

class EcoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemeHelper.applyTheme(this)
    }
}
