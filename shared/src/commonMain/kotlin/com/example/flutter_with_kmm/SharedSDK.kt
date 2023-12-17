package com.example.flutter_with_kmm

import com.example.flutter_with_kmm.data.SharedRepository
import com.example.flutter_with_kmm.data.SharedRepositoryImpl
import com.example.flutter_with_kmm.data.api.UserApi
import com.example.flutter_with_kmm.data.api.UserApiImpl
import com.example.flutter_with_kmm.data.db.DatabaseDriverFactory
import com.example.flutter_with_kmm.data.db.UserDao
import com.example.flutter_with_kmm.data.db.UserDaoImpl
import com.example.flutter_with_kmm.domain.SDKGateway
import com.example.flutter_with_kmm.domain.SharedInteractor
import com.example.flutter_with_kmm.domain.SharedInteractorImpl
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.Platform
import kotlinx.serialization.json.Json

internal expect fun httpClient(config: HttpClientConfig<*>.() -> Unit = {}): HttpClient

expect class BaseApplication

class SharedSDK(driverFactory: DatabaseDriverFactory,val platform: BaseApplication){

    private val client: HttpClient = httpClient {
        install(ContentNegotiation) {
            /*serializer = KotlinxSerializer(Json {
                ignoreUnknownKeys = true
            })*/
            json(Json {
                this.explicitNulls = false
                //通过data字段区分
                this.encodeDefaults = true
                this.isLenient = true
                this.coerceInputValues = true //强制输入值
                this.ignoreUnknownKeys = true
                this.prettyPrint = true
                this.allowStructuredMapKeys = true
            })
        }
    }

    private val userApi: UserApi = UserApiImpl(client)

    private val userDao: UserDao = UserDaoImpl(driverFactory)

    private val sharedRepository: SharedRepository = SharedRepositoryImpl(userApi, userDao)

    private val serializer = Json { isLenient = true; ignoreUnknownKeys = true }

    private val interactor: SharedInteractor = SharedInteractorImpl(sharedRepository, serializer)

    val gateway: SDKGateway = SDKGateway(interactor)

}