package ilio.bark

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
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

    private val http = HttpClient.newHttpClient()
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

        val request = HttpRequest.newBuilder(buildUri(Endpoints.push))
            .header("Content-Type", "application/json")
            .header("Authorization", "Basic $basicAuthToken")
            .POST(HttpRequest.BodyPublishers.ofString(pushAsString))
            .build()
        return try {
            val response = http.send(request, HttpResponse.BodyHandlers.ofString())
            val jsonView = response.headers().allValues("content-type").any { it.contains("application/json") }
            if (!jsonView) {
                throw IllegalArgumentException(response.body())
            }
            val json = Json.decodeFromString<JsonElement>(response.body()).jsonObject

            PushBack(
                code = json["code"]?.jsonPrimitive?.int ?: response.statusCode(),
                timestamp = json["timestamp"]?.jsonPrimitive?.long ?: System.currentTimeMillis(),
                message = json["message"]?.jsonPrimitive?.content ?: ""
            )
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
        val request = HttpRequest.newBuilder(buildUri(Endpoints.ping)).GET().build()
        return try {
            val response = http.send(request, HttpResponse.BodyHandlers.ofString())
            val json = Json.decodeFromString<JsonElement>(response.body()).jsonObject

            Pong(
                code = json["code"]?.jsonPrimitive?.int ?: response.statusCode(),
                timestamp = json["timestamp"]?.jsonPrimitive?.long ?: System.currentTimeMillis(),
                message = json["message"]?.jsonPrimitive?.content ?: ""
            )
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
        val request = HttpRequest.newBuilder(buildUri(Endpoints.info)).GET().build()
        return try {
            val response = http.send(request, HttpResponse.BodyHandlers.ofString())
            val json = Json.decodeFromString<JsonElement>(response.body()).jsonObject

            ServerInfo(
                version = json["version"]?.jsonPrimitive?.content ?: "",
                build = json["build"]?.jsonPrimitive?.content ?: "",
                arch = json["arch"]?.jsonPrimitive?.content ?: "",
                commit = json["commit"]?.jsonPrimitive?.content ?: "",
                devices = json["devices"]?.jsonPrimitive?.long ?: 0,
            )
        } catch (e: Exception) {
            System.err.println(e.stackTraceToString())
            ServerInfo()
        }
    }

    fun health(): Boolean {
        val request = HttpRequest.newBuilder(buildUri(Endpoints.health)).GET().build()
        return try {
            val response = http.send(request, HttpResponse.BodyHandlers.ofString())
            response.body() == "ok"
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