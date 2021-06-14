package com.example.flutter_with_kmm.data.db

import com.example.flutter_with_kmm.entities.User
import kotlinx.coroutines.flow.Flow

interface UserDao {

    suspend fun saveUser(user: User)

    fun getUserFlow(): Flow<List<User>>

}