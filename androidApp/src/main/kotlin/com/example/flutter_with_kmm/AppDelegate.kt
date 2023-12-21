package com.example.flutter_with_kmm

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.example.flutter_with_kmm.data.db.DatabaseDriverFactory
import com.example.flutter_with_kmm.domain.SDKGateway
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngine.EngineLifecycleListener
import io.flutter.embedding.engine.FlutterEngineCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.isAccessible

class AppDelegate : Application() {
    private val platform = BaseApplication(this) {
        it.setMethodCallHandler { call, result ->
            try {
                gateway.processCall(call.method, call.arguments, CallHandlerImpl(result))
            }catch (e:Exception){
                e.printStackTrace()
                result.error("error",e.message,e)
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
            when (activity) {
                is FlutterActivity -> {
                    if (platform.flutterEngine == null) {
                        val clazz = activity::class
                        activity.lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                val getFlutterEngine =
                                    clazz.functions.firstOrNull { it.name == "getFlutterEngine" }
                                        ?.let {
                                            it.isAccessible = true
                                            it
                                        }
                                if (getFlutterEngine != null) {
                                    async {
                                        delay(100)
                                        val engine = (try {
                                            (getFlutterEngine.call(activity) as? FlutterEngine)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            null
                                        }) ?: return@async
                                        onFlutterCreate(activity, engine)
                                    }.join()
                                }
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


