package com.example.flutter_with_kmm.data.api

import com.example.flutter_with_kmm.BASE_URL
import com.example.flutter_with_kmm.data.api.entities.UsersDTO
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*

class UserApiImpl constructor(private val client: HttpClient): UserApi {

    override suspend fun users(page: Int, results: Int): UsersDTO {
        return client.get(BASE_URL) {
            parameter("page", page)
            parameter("results", results)
            parameter("seed", "abc")
            accept(ContentType.Application.Json)
        }
    }

}