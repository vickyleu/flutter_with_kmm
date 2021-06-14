package com.example.flutter_with_kmm.data.api

import com.example.flutter_with_kmm.data.api.entities.UsersDTO

interface UserApi {

    suspend fun users(page: Int, results: Int): UsersDTO

}