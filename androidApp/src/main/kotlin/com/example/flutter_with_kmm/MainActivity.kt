package com.example.flutter_with_kmm

import android.os.Bundle
import android.window.OnBackInvokedDispatcher
import com.example.flutter_with_kmm.utils.HarmonyCheck
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine


class MainActivity : FlutterActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

    }

    /**
     * 由于鸿蒙系统缺失了onBackPressedDispatcher，所以需要自己实现
     */
    val flutterEngineImpl:FlutterEngine? get() =
            this.flutterEngine
}
