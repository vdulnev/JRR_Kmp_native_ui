package com.jrr.jrrkmp_native_ui.data.repository

import co.touchlab.kermit.Logger
import com.jrr.jrrkmp_native_ui.core.logging.redact
import com.jrr.jrrkmp_native_ui.data.api.createMcwsHttpClient
import com.jrr.jrrkmp_native_ui.domain.model.ArtistInfo
import com.jrr.jrrkmp_native_ui.domain.model.DiscographyAlbum
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private val log = Logger.withTag("repo:ArtistInfo")

class ArtistInfoRepository(
    httpClient: HttpClient = createMcwsHttpClient(),
    private val loadProvider: () -> String? = { ARTIST_INFO_PROVIDER_OPENAI },
    private val loadOpenAiApiKey: () -> String?,
    private val loadClaudeApiKey: () -> String? = { null },
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
        loadClaudeApiKey: () -> String?,
        loadOllamaBaseUrl: () -> String?,
        loadOllamaModel: () -> String?,
    ) : this(
        httpClient = createMcwsHttpClient(),
        loadProvider = loadProvider,
        loadOpenAiApiKey = loadOpenAiApiKey,
        loadClaudeApiKey = loadClaudeApiKey,
        loadOllamaBaseUrl = loadOllamaBaseUrl,
        loadOllamaModel = loadOllamaModel,
    )

    /**
     * A full discography takes the model a while to write — well past the engine
     * default socket timeout — so this repository talks through its own copy of
     * the shared client with a generous timeout. `config` reuses the underlying
     * engine, so this costs nothing but the plugin.
     */
    private val client: HttpClient = httpClient.config {
        install(HttpTimeout) {
            requestTimeoutMillis = REQUEST_TIMEOUT_MS
            socketTimeoutMillis = REQUEST_TIMEOUT_MS
            connectTimeoutMillis = CONNECT_TIMEOUT_MS
        }
    }

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    suspend fun getArtistInfo(artistName: String): ArtistInfo {
        log.d { "getArtistInfo(artist=$artistName)" }
        return when (loadProvider().normalizedProvider()) {
            ARTIST_INFO_PROVIDER_OLLAMA -> getArtistInfoFromOllama(artistName)
            ARTIST_INFO_PROVIDER_CLAUDE -> getArtistInfoFromClaude(artistName)
            else -> getArtistInfoFromOpenAi(artistName)
        }
    }

    private suspend fun getArtistInfoFromOpenAi(artistName: String): ArtistInfo {
        val apiKey = loadOpenAiApiKey()?.trim().orEmpty()
        if (apiKey.isEmpty()) {
            throw IllegalStateException("Add an OpenAI API key in Settings first")
        }

        try {
            val response = client.post("https://api.openai.com/v1/responses") {
                bearerAuth(apiKey)
                contentType(ContentType.Application.Json)
                setBody(buildRequestBody(artistName))
            }.body<OpenAiResponse>()

            if (response.incomplete()) {
                log.w { "OpenAI stopped early (${response.incompleteDetails?.reason}) artist=$artistName" }
            }
            val content = response.outputText
                ?: response.firstTextOutput()
                ?: throw IllegalStateException("AI response did not include artist info")

            return content.toArtistInfo(artistName)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            log.e(e) { "getArtistInfo failed artist=$artistName apiKey=${apiKey.redact()}" }
            throw e
        }
    }

    private suspend fun getArtistInfoFromClaude(artistName: String): ArtistInfo {
        val apiKey = loadClaudeApiKey()?.trim().orEmpty()
        if (apiKey.isEmpty()) {
            throw IllegalStateException("Add a Claude API key in Settings first")
        }

        try {
            val response = client.post("https://api.anthropic.com/v1/messages") {
                header("x-api-key", apiKey)
                header("anthropic-version", ANTHROPIC_API_VERSION)
                contentType(ContentType.Application.Json)
                setBody(buildClaudeRequestBody(artistName))
            }.body<ClaudeResponse>()

            if (response.stopReason == "max_tokens") {
                log.w { "Claude hit max_tokens before finishing artist=$artistName" }
            }
            val content = response.firstTextOutput()
                ?: throw IllegalStateException("Claude response did not include artist info")

            return content.extractJsonObject().toArtistInfo(artistName)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            log.e(e) { "getArtistInfoFromClaude failed artist=$artistName apiKey=${apiKey.redact()}" }
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
            val response = client.post("$baseUrl/api/generate") {
                contentType(ContentType.Application.Json)
                setBody(buildOllamaRequestBody(artistName, model))
            }.body<OllamaGenerateResponse>()

            val content = response.response.ifBlank {
                throw IllegalStateException("Ollama response did not include artist info")
            }
            return content.extractJsonObject().toArtistInfo(artistName)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            log.e(e) { "getArtistInfoFromOllama failed artist=$artistName baseUrl=$baseUrl model=$model" }
            throw e
        }
    }

    /**
     * Decodes a provider's JSON payload into the domain model. A truncated
     * generation shows up here as a parse failure, so translate it into
     * something a user can act on rather than leaking `SerializationException`.
     */
    private fun String.toArtistInfo(artistName: String): ArtistInfo {
        val info = try {
            json.decodeFromString<ArtistInfoPayload>(this)
        } catch (e: SerializationException) {
            log.e(e) { "artist info payload did not parse artist=$artistName (len=$length)" }
            throw IllegalStateException(
                "The AI answer was cut off before the discography finished. Try again.",
            )
        }
        return ArtistInfo(
            artistName = info.artistName.ifBlank { artistName },
            origin = info.origin.trim(),
            activeYears = info.activeYears.trim(),
            genres = info.genres.map { it.trim() }.filter { it.isNotEmpty() },
            biography = info.biography.trim(),
            discography = info.discography
                .filter { it.title.isNotBlank() }
                .map { album ->
                    DiscographyAlbum(
                        title = album.title.trim(),
                        year = album.year.trim(),
                        kind = album.kind.trim(),
                        history = album.history.trim(),
                        insight = album.insight.trim(),
                    )
                },
        )
    }

    private fun buildRequestBody(artistName: String): JsonObject = buildJsonObject {
        put("model", OPENAI_MODEL)
        put("max_output_tokens", MAX_OUTPUT_TOKENS)
        put("instructions", SYSTEM_PROMPT)
        put("input", profilePrompt(artistName) + "\nReturn only JSON.")
        putJsonSchema()
    }

    private fun buildClaudeRequestBody(artistName: String): JsonObject = buildJsonObject {
        put("model", CLAUDE_MODEL)
        put("max_tokens", MAX_OUTPUT_TOKENS)
        put("system", SYSTEM_PROMPT)
        put(
            "messages",
            buildJsonArray {
                add(
                    buildJsonObject {
                        put("role", "user")
                        put("content", profilePrompt(artistName) + "\n" + JSON_SHAPE_INSTRUCTION)
                    },
                )
            },
        )
    }

    private fun buildOllamaRequestBody(artistName: String, model: String): JsonObject = buildJsonObject {
        put("model", model)
        put("stream", false)
        put("format", "json")
        put("options", buildJsonObject { put("num_predict", MAX_OUTPUT_TOKENS) })
        put(
            "prompt",
            SYSTEM_PROMPT + "\n\n" + profilePrompt(artistName) + "\n" + JSON_SHAPE_INSTRUCTION,
        )
    }

    private fun profilePrompt(artistName: String): String =
        """
        Write a detailed profile of the music artist or group "$artistName".

        "biography": 4-6 paragraphs, separated by blank lines, covering the whole
        arc of their activity — where and how they formed, every significant
        line-up or label change, each distinct creative period and how the sound
        moved between them, the peaks and the lean years, and how the story ends:
        the break-up and what the members did next, or what they are doing now if
        they are still active.

        "discography": EVERY album they officially released, in chronological
        order from the debut to the most recent one (or to their final release if
        they have stopped). Do not stop after the famous ones and do not
        abbreviate with "and others" — list them all, however many there are.
        Cover all studio albums, plus the live albums, EPs and soundtracks that
        matter to the story. For each release give:
          - "title": the album title, without the year
          - "year": year of first release, as a string
          - "kind": one of Studio, Live, EP, Compilation, Soundtrack
          - "history": 2-3 sentences — when and where it was recorded, who
            produced it, the line-up, what was going on in the band at the time,
            and how it was received
          - "insight": one non-obvious fact about the record that a fan would
            enjoy knowing

        Also fill "active_years" (e.g. "1976-1991" or "1994-present"), "origin"
        (city and country) and "genres" (2-4 of them).

        Stay factual and neutral. If you are not sure of a detail, leave it out or
        say it is disputed rather than inventing it.
        """.trimIndent()

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
                        put("schema", artistInfoJsonSchema())
                    },
                )
            },
        )
    }

    private fun artistInfoJsonSchema(): JsonObject = buildJsonObject {
        put("type", "object")
        put("additionalProperties", false)
        put("required", stringArray("artist_name", "origin", "active_years", "genres", "biography", "discography"))
        put(
            "properties",
            buildJsonObject {
                put("artist_name", stringSchema())
                put("origin", stringSchema())
                put("active_years", stringSchema())
                put(
                    "genres",
                    buildJsonObject {
                        put("type", "array")
                        put("items", stringSchema())
                    },
                )
                put("biography", stringSchema())
                put(
                    "discography",
                    buildJsonObject {
                        put("type", "array")
                        put(
                            "items",
                            buildJsonObject {
                                put("type", "object")
                                put("additionalProperties", false)
                                put("required", stringArray("title", "year", "kind", "history", "insight"))
                                put(
                                    "properties",
                                    buildJsonObject {
                                        put("title", stringSchema())
                                        put("year", stringSchema())
                                        put("kind", stringSchema())
                                        put("history", stringSchema())
                                        put("insight", stringSchema())
                                    },
                                )
                            },
                        )
                    },
                )
            },
        )
    }

    private fun stringSchema(): JsonObject = buildJsonObject { put("type", "string") }

    private fun stringArray(vararg values: String) = buildJsonArray {
        values.forEach { add(JsonPrimitive(it)) }
    }

    private fun OpenAiResponse.firstTextOutput(): String? =
        output.asSequence()
            .flatMap { item -> item.content.asSequence() }
            .firstNotNullOfOrNull { content -> content.text }

    private fun OpenAiResponse.incomplete(): Boolean = incompleteDetails?.reason != null

    private fun ClaudeResponse.firstTextOutput(): String? =
        content.firstNotNullOfOrNull { block -> block.text?.takeIf { it.isNotBlank() } }

    /**
     * Claude returns plain text, and despite the "JSON only" instruction may wrap
     * the object in prose or ```json fences. Narrow to the outermost `{…}` so the
     * payload decodes cleanly.
     */
    private fun String.extractJsonObject(): String {
        val start = indexOf('{')
        val end = lastIndexOf('}')
        return if (start in 0 until end) substring(start, end + 1) else this
    }
}

const val ARTIST_INFO_PROVIDER_OPENAI = "openai"
const val ARTIST_INFO_PROVIDER_CLAUDE = "claude"
const val ARTIST_INFO_PROVIDER_OLLAMA = "ollama"
const val DEFAULT_OLLAMA_BASE_URL = "http://localhost:11434"
const val DEFAULT_OLLAMA_MODEL = "llama3.1"

/** Anthropic Messages API version pin (sent as the `anthropic-version` header). */
private const val ANTHROPIC_API_VERSION = "2023-06-01"

private const val OPENAI_MODEL = "gpt-4.1-mini"

/** Claude model used for artist-info lookups — the fast, low-cost tier holds
 *  plenty of music history for a discography write-up. */
private const val CLAUDE_MODEL = "claude-haiku-4-5-20251001"

/** A prolific act can run to 30+ releases; each one costs ~120 tokens to write up. */
private const val MAX_OUTPUT_TOKENS = 8192

/** Writing a full discography routinely takes over a minute. */
private const val REQUEST_TIMEOUT_MS = 240_000L
private const val CONNECT_TIMEOUT_MS = 30_000L

private const val SYSTEM_PROMPT =
    "You are a knowledgeable music historian writing a thorough, factual, neutral artist profile. " +
        "You are exhaustive about discographies: you list every release, not just the well-known ones."

/** Shared JSON contract for the providers that have no structured-output mode. */
private const val JSON_SHAPE_INSTRUCTION = """
Respond with only a JSON object (no prose, no markdown fences) with exactly these keys:
{
  "artist_name": "Artist name",
  "origin": "City, Country",
  "active_years": "1976-1991",
  "genres": ["genre", "genre"],
  "biography": "4-6 paragraphs separated by blank lines",
  "discography": [
    {
      "title": "Album title",
      "year": "1979",
      "kind": "Studio",
      "history": "2-3 sentences on how the record came about and how it landed",
      "insight": "One non-obvious fact about it"
    }
  ]
}
"""

private fun String?.normalizedProvider(): String =
    when (this?.trim()?.lowercase()) {
        ARTIST_INFO_PROVIDER_OLLAMA -> ARTIST_INFO_PROVIDER_OLLAMA
        ARTIST_INFO_PROVIDER_CLAUDE -> ARTIST_INFO_PROVIDER_CLAUDE
        else -> ARTIST_INFO_PROVIDER_OPENAI
    }

@Serializable
private data class OpenAiResponse(
    @SerialName("output_text") val outputText: String? = null,
    val output: List<OpenAiOutputItem> = emptyList(),
    @SerialName("incomplete_details") val incompleteDetails: OpenAiIncompleteDetails? = null,
)

@Serializable
private data class OpenAiIncompleteDetails(
    val reason: String? = null,
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
private data class ClaudeResponse(
    val content: List<ClaudeContent> = emptyList(),
    @SerialName("stop_reason") val stopReason: String? = null,
)

@Serializable
private data class ClaudeContent(
    val type: String? = null,
    val text: String? = null,
)

@Serializable
private data class OllamaGenerateResponse(
    val response: String = "",
)

@Serializable
private data class ArtistInfoPayload(
    @SerialName("artist_name") val artistName: String = "",
    val origin: String = "",
    @SerialName("active_years") val activeYears: String = "",
    val genres: List<String> = emptyList(),
    val biography: String = "",
    val discography: List<DiscographyAlbumPayload> = emptyList(),
)

@Serializable
private data class DiscographyAlbumPayload(
    val title: String = "",
    val year: String = "",
    val kind: String = "",
    val history: String = "",
    val insight: String = "",
)
