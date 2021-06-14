package com.example.flutter_with_kmm.data

import com.example.flutter_with_kmm.entities.User
import kotlinx.coroutines.flow.Flow

interface SharedRepository {

    suspend fun users(page: Int, results: Int): List<User>

    suspend fun saveUser(user: User)

    fun getUserFlow(): Flow<List<User>>

}