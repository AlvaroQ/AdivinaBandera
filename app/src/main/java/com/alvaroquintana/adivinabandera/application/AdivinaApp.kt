package com.alvaroquintana.adivinabandera.application

import android.app.Application
import com.alvaroquintana.adivinabandera.application.initDI

class AdivinaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initDI()
    }
}