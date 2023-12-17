package com.example.flutter_with_kmm

import android.app.Application
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp

actual class BaseApplication(val app: Application) {

}
internal actual fun httpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient{
    return HttpClient(OkHttp) {
        config()
    }
}