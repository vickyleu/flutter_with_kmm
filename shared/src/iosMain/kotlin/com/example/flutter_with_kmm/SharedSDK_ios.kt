package com.example.flutter_with_kmm

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.darwin.Darwin


internal actual fun httpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient {
    return HttpClient(Darwin) {
        config()
    }
}