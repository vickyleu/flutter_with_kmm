package com.example.flutter_with_kmm.utils

import com.example.flutter_with_kmm.NoProguard
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializerOrNull
import org.lighthousegames.logging.logging
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class Pref<T : Any>(private val key: String?, private val default: T) {
    private val settings = Settings()

    private val logger = logging("Pref")

    operator fun getValue(any: Any, property: KProperty<*>): T {
        val s: T = when (default::class) {
            Int::class -> {
                if (settings.hasKey(key = (key ?: property.name))) {
                    settings.getInt(key = (key ?: property.name), default as Int) as T
                } else {
                    default
                }
            }

            Long::class -> {
                if (settings.hasKey(key = (key ?: property.name))) {
                    settings.getLong(key = (key ?: property.name), default as Long) as T
                } else {
                    default
                }
            }

            String::class -> {
                if (settings.hasKey(key = (key ?: property.name))) {
                    settings.getString(key = (key ?: property.name), default as String) as T
                } else {
                    default
                }
            }

            Float::class -> {
                if (settings.hasKey(key = (key ?: property.name))) {
                    settings.getFloat(key = (key ?: property.name), default as Float) as T
                } else {
                    default
                }
            }

            Double::class -> {
                if (settings.hasKey(key = (key ?: property.name))) {
                    settings.getDouble(key = (key ?: property.name), default as Double) as T
                } else {
                    default
                }
            }

            Boolean::class -> {
                if (settings.hasKey(key = (key ?: property.name))) {
                    settings.getBoolean(key = (key ?: property.name), default as Boolean) as T
                } else {
                    default
                }
            }

            NoProguard::class -> {
                val content: String? = settings.getStringOrNull(key = (key ?: property.name))
                if (content != null) {
                    default::class.serializerOrNull()?.let {
                        // 我想将NoProguard的序列化存储为文本信息,这里是从文本信息中还原出来
                        try {
                            Json.decodeFromString(it.nullable, content) as? T
                        } catch (e: Exception) {
                            null
                        }
                    } ?: default
                } else {
                    default
                }
            }

            else -> throw IllegalArgumentException("Invalid type!")
        }
        return s
//        return Paper.book().read(key ?: property.name, default)?:default
    }

    operator fun setValue(any: Any, property: KProperty<*>, value: T) {
        when (default::class) {
            Int::class -> {
                settings.set(key = (key ?: property.name), value = value as Int)
            }

            Long::class -> {
                settings.set(key = (key ?: property.name), value = value as Long)
            }

            String::class -> {
                settings.set(key = (key ?: property.name), value = value as String)
            }

            Float::class -> {
                settings.set(key = (key ?: property.name), value = value as Float)
            }

            Double::class -> {
                settings.set(key = (key ?: property.name), value = value as Double)
            }

            Boolean::class -> {
                settings.set(key = (key ?: property.name), value = value as Boolean)
            }

            NoProguard::class -> {
                @Suppress("UNCHECKED_CAST")
                (default::class.serializerOrNull() as? KSerializer<T>)?.apply {
                    try {
                        val json = Json.encodeToString(this@apply, value)
                        settings.set(key = (key ?: property.name), value = json)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            else -> throw IllegalArgumentException("Invalid type!")
        }
//        settings.set(key=(key ?: property.name), value=value)
//        Paper.book().write(key ?: property.name, value)
    }
}

//使用基本类型之外的model，记得继承IUoocNoProguard！！
inline fun <reified AnyClz, reified T : Any> AnyClz.pref(default: T, key: String? = null) =
    Pref(key, default)
