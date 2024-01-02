package com.example.flutter_with_kmm

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import com.example.flutter_with_kmm.data.db.DatabaseDriverFactory
import com.example.flutter_with_kmm.domain.SDKGateway
import com.example.flutter_with_kmm.utils.HarmonyCheck
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngine.EngineLifecycleListener
import io.flutter.embedding.engine.FlutterEngineCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.lighthousegames.logging.logging

class AppDelegate : Application() {

    internal val platform = BaseApplication(this) {
        it.setMethodCallHandler { call, result ->
            try {
                gateway.processCall(call.method, call.arguments, CallHandlerImpl(result))
            } catch (e: Exception) {
                e.printStackTrace()
                result.error("error", e.message, e)
            }
        }
        gateway.setCallbacks(CallbackHandlerImpl(it))
    }
    private val gateway: SDKGateway = SharedSDK(
        driverFactory = DatabaseDriverFactory(this),
        platform = platform
    ).gateway


    override fun onCreate() {
        registerActivityLifecycleCallbacks(activityLifecycleCallback)
        super.onCreate()
    }

    private fun onFlutterCreate(activity: FlutterActivity, engine: FlutterEngine) {
        platform.setupEngine(engine, lifecycle)
    }

    private fun onFlutterResume() {

    }

    private fun onFlutterDestroy() {
        FlutterEngineCache
            .getInstance()
            .remove("global_engine_id")
        platform.destroy(lifecycle)
        gateway.destroy()
    }


    private val activityLifecycleCallback = object : ActivityLifecycleCallbacks {
        override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
//            when (activity) {
//                is ComponentActivity -> {
//                    activity.onBackPressedDispatcher.addCallback(
//                        activity,
//                        object : OnBackPressedCallback(true) {
//                            override fun handleOnBackPressed() {
//                                if (!activity.isFinishing) {
//                                    activity.finish()
//                                }
//                            }
//                        })
//                }
//            }
            when (activity) {
                is MainActivity -> {
                    if (platform.flutterEngine == null) {
                        activity.lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                async {
                                    var count = 0
                                    var flutterEngine: FlutterEngine?
                                    while (run {
                                            flutterEngine = try {
                                                activity.flutterEngineImpl
                                            } catch (e: Exception) {
                                                null
                                            }
                                            flutterEngine
                                        } == null && count++ < 35) {
                                        delay(50)
                                    }
                                    val engine = flutterEngine ?: return@async
                                    onFlutterCreate(activity, engine)
                                }.join()
                            }
                        }
                    }
                }
            }
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        }

        override fun onActivityStarted(activity: Activity) {
        }

        override fun onActivityResumed(activity: Activity) {
            when (activity) {
                is FlutterActivity -> {
                    if (platform.flutterEngine != null) {
                        onFlutterResume()
                    }
                }
            }
        }

        override fun onActivityPaused(activity: Activity) {
        }

        override fun onActivityStopped(activity: Activity) {
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        }

        override fun onActivityDestroyed(activity: Activity) {
            when (activity) {
                is FlutterActivity -> {
                    if (platform.flutterEngine != null) {
                        onFlutterDestroy()
                    }
                }
            }
        }

    }
    private val lifecycle = object : EngineLifecycleListener {
        override fun onPreEngineRestart() {
            onFlutterResume()
        }

        override fun onEngineWillDestroy() {
            onFlutterDestroy()
        }
    }
}


