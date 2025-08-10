package com.enchantreader.app

import android.app.Application
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class EnchantReaderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PDFBoxResourceLoader.init(applicationContext)
    }
}
