package com.example.flutter_with_kmm.domain

import com.example.flutter_with_kmm.BaseApplication
import com.example.flutter_with_kmm.utils.DateTimeFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

expect suspend fun SDKGateway.isInternetGranted(): Boolean
internal expect fun SDKGateway.platformDestroy()

class SDKGateway(
    internal val interactor: SharedInteractor,
    internal val platform: BaseApplication
) {

    final val noLimitScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * 对外暴露的协程作用域
     */
    internal var scope = lazy {
        return@lazy _scope!!
    }
        private set

    /**
     * 内部使用的协程作用域,会cancel掉,不影响外部使用,同时能控制协程的生命周期
     */
    private var _scope: CoroutineScope? = null

    init {
        resignScope()
    }

    private fun resignScope(){
        _scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @Throws(Exception::class)
    fun processCall(method: String, arguments: Any?, callHandler: CallHandler) {
        scope.value.launch {
            try {
                when (method) {
                    "users" -> {
                        @Suppress("UNCHECKED_CAST")
                        val args = arguments as List<Int>
                        val page = args[0]
                        val results = args[1]
                        val result = interactor.users(page, results)
                        withContext(Dispatchers.Main) {
                            callHandler.success(result)
                        }
                    }

                    "saveUser" -> {
                        val userJson = arguments as String
                        interactor.saveUser(userJson)
                        withContext(Dispatchers.Main) {
                            callHandler.success(true)
                        }
                    }

                    "isInternetGranted" -> {
                        val dtf = DateTimeFormatter("yyyy-MM-dd HH:mm:ss")
                        try {
                            val isGranted = isInternetGranted()
                            withContext(Dispatchers.Main) {
                                callHandler.success(isGranted)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            withContext(Dispatchers.Main) {
                                callHandler.error("${e.message}")
                            }
                        }

                    }

                    else -> {
                        withContext(Dispatchers.Main) {
                            callHandler.error("Method not implemented")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callHandler.error(e.message)
                }
            }
        }
    }

    fun setCallbacks(callback: CallbackHandler) {
        if(_scope == null){
            resignScope()
            scope = lazy {
               return@lazy _scope!!
            }
        }
        interactor.setUsersUpdatesListener {
            callback.invokeMethod("users", it)
        }
        interactor.setNativeCallbackListener { method, map ->
            val arguments: Map<String, Any> = mapOf("method" to method, "args" to map)
            callback.invokeMethod("nativeCallback", arguments)
        }
    }

    fun destroy() {
        scope.value.cancel()
        _scope=null
        platformDestroy()
        interactor.destroy()
    }

}