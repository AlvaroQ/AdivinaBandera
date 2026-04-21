package com.alvaroquintana.adivinabandera.application

import android.app.Application
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.svg.SvgDecoder
import com.alvaroquintana.adivinabandera.application.initDI

class AdivinaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SingletonImageLoader.setSafe { context ->
            ImageLoader.Builder(context)
                .components { add(SvgDecoder.Factory()) }
                .build()
        }
        initDI()
    }
}