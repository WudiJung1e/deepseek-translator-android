package com.example.translation.data

import io.ktor.client.HttpClient
import io.ktor.client.statement.bodyAsText
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class DeepSeekApi(
    private val apiKey: String,
    private val client: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                },
            )
        }
    },
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun translate(text: String, targetLanguage: String): String {
        require(apiKey.isNotBlank()) {
            "请先在设置中填写 DeepSeek API Key"
        }

        val requestBody = json.encodeToString(
            ChatRequest.serializer(),
            ChatRequest(
                messages = listOf(
                    ChatMessage(
                        role = "system",
                        content = "You are a professional translation engine. Translate accurately and only return the translated text.",
                    ),
                    ChatMessage(
                        role = "user",
                        content = "Translate the following text into $targetLanguage. Only return the translation:\n\n$text",
                    ),
                ),
            ),
        )

        val response = client.post("https://api.deepseek.com/chat/completions") {
            bearerAuth(apiKey)
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(requestBody)
        }
        val responseText = response.bodyAsText()

        if (!response.status.isSuccess()) {
            error("DeepSeek 请求失败：HTTP ${response.status.value}\n$responseText")
        }

        return json.decodeFromString(ChatResponse.serializer(), responseText)
            .choices
            .firstOrNull()
            ?.message
            ?.content
            ?.trim()
            .orEmpty()
            .ifBlank { error("DeepSeek 没有返回翻译结果") }
    }
}

@Serializable
private data class ChatRequest(
    val model: String = "deepseek-v4-flash",
    val messages: List<ChatMessage>,
    val thinking: ThinkingConfig = ThinkingConfig(type = "disabled"),
    val temperature: Double = 0.2,
    val stream: Boolean = false,
)

@Serializable
private data class ThinkingConfig(
    val type: String,
)

@Serializable
private data class ChatMessage(
    val role: String,
    val content: String,
)

@Serializable
private data class ChatResponse(
    val choices: List<Choice> = emptyList(),
)

@Serializable
private data class Choice(
    val message: ChatMessage,
    @SerialName("finish_reason")
    val finishReason: String? = null,
)
