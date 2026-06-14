package com.jrr.jrrkmp_native_ui.data.repository

import co.touchlab.kermit.Logger
import com.jrr.jrrkmp_native_ui.core.logging.redact
import com.jrr.jrrkmp_native_ui.data.api.createMcwsHttpClient
import com.jrr.jrrkmp_native_ui.domain.model.ArtistInfo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private val log = Logger.withTag("repo:ArtistInfo")

class ArtistInfoRepository(
    private val httpClient: HttpClient = createMcwsHttpClient(),
    private val loadProvider: () -> String? = { ARTIST_INFO_PROVIDER_OPENAI },
    private val loadOpenAiApiKey: () -> String?,
    private val loadOllamaBaseUrl: () -> String? = { DEFAULT_OLLAMA_BASE_URL },
    private val loadOllamaModel: () -> String? = { DEFAULT_OLLAMA_MODEL },
) {
    constructor(loadOpenAiApiKey: () -> String?) : this(
        httpClient = createMcwsHttpClient(),
        loadOpenAiApiKey = loadOpenAiApiKey,
    )

    constructor(
        loadProvider: () -> String?,
        loadOpenAiApiKey: () -> String?,
        loadOllamaBaseUrl: () -> String?,
        loadOllamaModel: () -> String?,
    ) : this(
        httpClient = createMcwsHttpClient(),
        loadProvider = loadProvider,
        loadOpenAiApiKey = loadOpenAiApiKey,
        loadOllamaBaseUrl = loadOllamaBaseUrl,
        loadOllamaModel = loadOllamaModel,
    )

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    suspend fun getArtistInfo(artistName: String): ArtistInfo {
        log.d { "getArtistInfo(artist=$artistName)" }
        return when (loadProvider().normalizedProvider()) {
            ARTIST_INFO_PROVIDER_OLLAMA -> getArtistInfoFromOllama(artistName)
            else -> getArtistInfoFromOpenAi(artistName)
        }
    }

    private suspend fun getArtistInfoFromOpenAi(artistName: String): ArtistInfo {
        val apiKey = loadOpenAiApiKey()?.trim().orEmpty()
        if (apiKey.isEmpty()) {
            throw IllegalStateException("Add an OpenAI API key in Settings first")
        }

        try {
            val response = httpClient.post("https://api.openai.com/v1/responses") {
                bearerAuth(apiKey)
                contentType(ContentType.Application.Json)
                setBody(buildRequestBody(artistName))
            }.body<OpenAiResponse>()

            val content = response.outputText
                ?: response.firstTextOutput()
                ?: throw IllegalStateException("AI response did not include artist info")

            val info = json.decodeFromString<ArtistInfoPayload>(content)
            return ArtistInfo(
                artistName = info.artistName.ifBlank { artistName },
                shortBio = info.shortBio,
                bestAlbums = info.bestAlbums,
            )
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            log.e(e) { "getArtistInfo failed artist=$artistName apiKey=${apiKey.redact()}" }
            throw e
        }
    }

    private suspend fun getArtistInfoFromOllama(artistName: String): ArtistInfo {
        val baseUrl = loadOllamaBaseUrl()?.trim()?.trimEnd('/').orEmpty()
        if (baseUrl.isEmpty()) {
            throw IllegalStateException("Add an Ollama URL in Settings first")
        }
        val model = loadOllamaModel()?.trim().orEmpty()
        if (model.isEmpty()) {
            throw IllegalStateException("Add an Ollama model in Settings first")
        }

        try {
            val response = httpClient.post("$baseUrl/api/generate") {
                contentType(ContentType.Application.Json)
                setBody(buildOllamaRequestBody(artistName, model))
            }.body<OllamaGenerateResponse>()

            val content = response.response.ifBlank {
                throw IllegalStateException("Ollama response did not include artist info")
            }
            val info = json.decodeFromString<ArtistInfoPayload>(content)
            return ArtistInfo(
                artistName = info.artistName.ifBlank { artistName },
                shortBio = info.shortBio,
                bestAlbums = info.bestAlbums,
            )
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            log.e(e) { "getArtistInfoFromOllama failed artist=$artistName baseUrl=$baseUrl model=$model" }
            throw e
        }
    }

    private fun buildRequestBody(artistName: String): JsonObject = buildJsonObject {
        put("model", "gpt-4.1-mini")
        put("instructions", "You are a concise music reference assistant. Return factual, neutral artist information.")
        put(
            "input",
            "For the music artist \"$artistName\", write a short 2-3 sentence bio and list 5 widely regarded best albums. Prefer studio albums. Return only JSON.",
        )
        putJsonSchema()
    }

    private fun buildOllamaRequestBody(artistName: String, model: String): JsonObject = buildJsonObject {
        put("model", model)
        put("stream", false)
        put("format", "json")
        put(
            "prompt",
            """
            You are a concise music reference assistant. Return factual, neutral artist information.
            For the music artist "$artistName", write a short 2-3 sentence bio and list 5 widely regarded best albums.
            Prefer studio albums. Return only JSON with exactly these keys:
            {
              "artist_name": "Artist name",
              "short_bio": "Short bio",
              "best_albums": ["Album 1", "Album 2", "Album 3", "Album 4", "Album 5"]
            }
            """.trimIndent(),
        )
    }

    private fun JsonObjectBuilder.putJsonSchema() {
        put(
            "text",
            buildJsonObject {
                put(
                    "format",
                    buildJsonObject {
                        put("type", "json_schema")
                        put("name", "artist_info")
                        put("strict", true)
                        put(
                            "schema",
                            buildJsonObject {
                                put("type", "object")
                                put("additionalProperties", false)
                                put(
                                    "required",
                                    buildJsonArray {
                                        add(JsonPrimitive("artist_name"))
                                        add(JsonPrimitive("short_bio"))
                                        add(JsonPrimitive("best_albums"))
                                    },
                                )
                                put(
                                    "properties",
                                    buildJsonObject {
                                        put("artist_name", buildJsonObject { put("type", "string") })
                                        put("short_bio", buildJsonObject { put("type", "string") })
                                        put(
                                            "best_albums",
                                            buildJsonObject {
                                                put("type", "array")
                                                put("minItems", 1)
                                                put("maxItems", 5)
                                                put("items", buildJsonObject { put("type", "string") })
                                            },
                                        )
                                    },
                                )
                            },
                        )
                    },
                )
            },
        )
    }

    private fun OpenAiResponse.firstTextOutput(): String? =
        output.asSequence()
            .flatMap { item -> item.content.asSequence() }
            .firstNotNullOfOrNull { content -> content.text }
}

const val ARTIST_INFO_PROVIDER_OPENAI = "openai"
const val ARTIST_INFO_PROVIDER_OLLAMA = "ollama"
const val DEFAULT_OLLAMA_BASE_URL = "http://localhost:11434"
const val DEFAULT_OLLAMA_MODEL = "llama3.1"

private fun String?.normalizedProvider(): String =
    when (this?.trim()?.lowercase()) {
        ARTIST_INFO_PROVIDER_OLLAMA -> ARTIST_INFO_PROVIDER_OLLAMA
        else -> ARTIST_INFO_PROVIDER_OPENAI
    }

@Serializable
private data class OpenAiResponse(
    @SerialName("output_text") val outputText: String? = null,
    val output: List<OpenAiOutputItem> = emptyList(),
)

@Serializable
private data class OpenAiOutputItem(
    val content: List<OpenAiContent> = emptyList(),
)

@Serializable
private data class OpenAiContent(
    val type: String? = null,
    val text: String? = null,
    @SerialName("refusal") val refusal: String? = null,
)

@Serializable
private data class OllamaGenerateResponse(
    val response: String = "",
)

@Serializable
private data class ArtistInfoPayload(
    @SerialName("artist_name") val artistName: String,
    @SerialName("short_bio") val shortBio: String,
    @SerialName("best_albums") val bestAlbums: List<String>,
)
