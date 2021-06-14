package com.example.flutter_with_kmm

import com.example.flutter_with_kmm.data.api.UserApiImpl
import com.example.flutter_with_kmm.data.api.UserApi
import com.example.flutter_with_kmm.data.SharedRepository
import com.example.flutter_with_kmm.data.SharedRepositoryImpl
import com.example.flutter_with_kmm.data.db.DatabaseDriverFactory
import com.example.flutter_with_kmm.data.db.UserDao
import com.example.flutter_with_kmm.data.db.UserDaoImpl
import com.example.flutter_with_kmm.domain.SDKGateway
import com.example.flutter_with_kmm.domain.SharedInteractor
import com.example.flutter_with_kmm.domain.SharedInteractorImpl
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*

class SharedSDK(driverFactory: DatabaseDriverFactory)  {

    private val client: HttpClient = HttpClient(){
        install(JsonFeature){
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
            })
        }
    }

    private val userApi : UserApi = UserApiImpl(client)

    private val userDao : UserDao = UserDaoImpl(driverFactory)

    private val sharedRepository: SharedRepository = SharedRepositoryImpl(userApi, userDao)

    private val serializer = kotlinx.serialization.json.Json { isLenient = true; ignoreUnknownKeys = true }

    val interactor : SharedInteractor = SharedInteractorImpl(sharedRepository, serializer)

    val gateway : SDKGateway = SDKGateway(interactor)

}