package com.example.flutter_with_kmm

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp

internal actual fun httpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient{
    return HttpClient(OkHttp) {
        config()
    }
}