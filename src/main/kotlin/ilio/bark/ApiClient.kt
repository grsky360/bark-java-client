package ilio.bark

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.io.entity.HttpEntities
import java.net.URI
import java.util.*

@OptIn(ExperimentalSerializationApi::class)
class ApiClient private constructor(private val baseApi: String, private val basicAuthToken: String, private val pusher: String) {

    companion object {
        fun newClient(baseApi: String, basicAuthToken: String = "", pusher: String = "Bark"): ApiClient {
            return ApiClient(baseApi, basicAuthToken, pusher)
        }

        fun newClient(baseApi: String, username: String = "", password: String = "", pusher: String = "Bark"): ApiClient {
            return ApiClient(baseApi, Base64.getEncoder().encodeToString("$username:$password".toByteArray()), pusher)
        }
    }

    private val http = HttpClients.createDefault()
    private val json = Json {
        encodeDefaults = true
        explicitNulls = false
    }

    fun push(push: Push): PushBack {
        push.validate()

        val newPush = push.copy(
            title = push.title.ifBlank { pusher },
            body = push.body.ifBlank { "" }
        )

        val pushAsString = json.encodeToString(newPush)

        val request = HttpPost(buildUri(Endpoints.push)).apply {
            addHeader("Authorization", "Basic $basicAuthToken")
            entity = HttpEntities.create(pushAsString, ContentType.APPLICATION_JSON)
        }
        return try {
            http.execute(request).use {
                if (it.entity.contentType != "application/json") {
                    throw IllegalArgumentException(it.entity.content.reader().readText())
                }
                val json = Json.decodeFromStream<JsonElement>(it.entity.content).jsonObject

                PushBack(
                    code = json["code"]?.jsonPrimitive?.int ?: it.code,
                    timestamp = json["timestamp"]?.jsonPrimitive?.long ?: System.currentTimeMillis(),
                    message = json["message"]?.jsonPrimitive?.content ?: ""
                )
            }
        } catch (e: Exception) {
            System.err.println(e.stackTraceToString())

            PushBack(
                code = 500,
                timestamp = System.currentTimeMillis() / 1000,
                message = e.message ?: ""
            )
        }
    }

    fun ping(): Pong {
        val request = HttpGet(buildUri(Endpoints.ping))
        return try {
            http.execute(request).use {
                val json = Json.decodeFromStream<JsonElement>(it.entity.content).jsonObject

                Pong(
                    code = json["code"]?.jsonPrimitive?.int ?: it.code,
                    timestamp = json["timestamp"]?.jsonPrimitive?.long ?: System.currentTimeMillis(),
                    message = json["message"]?.jsonPrimitive?.content ?: ""
                )
            }

        } catch (e: Exception) {
            System.err.println(e.stackTraceToString())

            Pong(
                code = 500,
                timestamp = System.currentTimeMillis() / 1000,
                message = e.message ?: ""
            )
        }
    }

    fun info(): ServerInfo {
        val request = HttpGet(buildUri(Endpoints.info)).apply {
            addHeader("Authorization", "Basic $basicAuthToken")
        }
        return try {
            http.execute(request).use {
                val json = Json.decodeFromStream<JsonElement>(it.entity.content).jsonObject

                ServerInfo(
                    version = json["version"]?.jsonPrimitive?.content ?: "",
                    build = json["build"]?.jsonPrimitive?.content ?: "",
                    arch = json["arch"]?.jsonPrimitive?.content ?: "",
                    commit = json["commit"]?.jsonPrimitive?.content ?: "",
                    devices = json["devices"]?.jsonPrimitive?.long ?: 0,
                )
            }
        } catch (e: Exception) {
            System.err.println(e.stackTraceToString())
            ServerInfo()
        }
    }

    fun health(): Boolean {
        val request = HttpGet(buildUri(Endpoints.health))
        return try {
            http.execute(request).use {
                it.entity.content.reader().readText() == "ok"
            }
        } catch (e: Exception) {
            System.err.println(e.stackTraceToString())
            false
        }
    }

    private fun buildUri(uri: String): URI {
        return URI(baseApi + uri)
    }
}

data class PushBack(
    val code: Int,
    val timestamp: Long = System.currentTimeMillis() / 1000,
    val message: String = "",
) {
    val success: Boolean get() = code == 200
}

data class Pong(
    val code: Int,
    val timestamp: Long = System.currentTimeMillis() / 1000,
    val message: String = "",
) {
    val pong: Boolean get() = code == 200 && message == "pong"
}

data class ServerInfo(
    val version: String = "",
    val build: String = "",
    val arch: String = "",
    val commit: String = "",
    val devices: Long = 0
)