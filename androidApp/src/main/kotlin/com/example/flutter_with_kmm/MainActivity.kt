package com.example.flutter_with_kmm

import android.os.Bundle
import android.window.OnBackInvokedDispatcher
import com.example.flutter_with_kmm.utils.HarmonyCheck
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import org.lighthousegames.logging.logging


class MainActivity : FlutterActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val platform = (application as AppDelegate).platform
        super.onCreate(savedInstanceState)
    }

    /**
     * 由于鸿蒙系统缺失了onBackPressedCallback类，所以需要自己实现
     */
    val flutterEngineImpl:FlutterEngine? get() =
            this.flutterEngine
}
