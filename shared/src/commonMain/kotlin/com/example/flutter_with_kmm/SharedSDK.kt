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
import kotlinx.serialization.json.Json
import org.lighthousegames.logging.KmLogging
import org.lighthousegames.logging.LogLevel
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal expect fun SharedSDK.httpClient(config: HttpClientConfig<*>.() -> Unit = {}): HttpClient


class SharedSDK(driverFactory: DatabaseDriverFactory,val platform: BaseApplication){
    companion object{
        internal const val  CHANNEL = "example/platform"
    }

    init {
        KmLogging.setLogLevel(LogLevel.Verbose)
    }
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

    val gateway: SDKGateway = SDKGateway(interactor,platform)

}
internal class AutoUpdateDelegate<T>(private val onChange: (T) -> Unit) :
    ReadWriteProperty<Any?, T?> {
    private var value: T? = null
    override fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        if (this.value != value) {
            this.value = value
            if (value != null)
                onChange(value)
        }
    }
}