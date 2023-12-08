package com.example.flutter_with_kmm.data.api

import com.example.flutter_with_kmm.BASE_URL
import com.example.flutter_with_kmm.data.api.entities.UsersDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.ContentType

class UserApiImpl(private val client: HttpClient) : UserApi {
    override suspend fun users(page: Int, results: Int): UsersDTO {
        return client.get(BASE_URL) {
            parameter("page", page)
            parameter("results", results)
            parameter("seed", "abc")
            accept(ContentType.Application.Json)
        }.body()
    }
}