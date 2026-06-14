package com.jrr.jrrkmp_native_ui.desktop

import com.jrr.jrrkmp_native_ui.presentation.viewmodel.MainShellSettings
import java.util.prefs.Preferences

/**
 * Desktop settings store backed by [java.util.prefs.Preferences] (the analogue
 * of Android's SharedPreferences). Implements [MainShellSettings] and adds the
 * zone/audio-quality keys the facade persists.
 */
class DesktopSettings : MainShellSettings {
    private val prefs: Preferences = Preferences.userRoot().node("com/jrr/jrrkmp_native_ui")

    override fun getLastActiveZoneId(): String? = prefs.get(KEY_LAST_ZONE, null)
    override fun setLastActiveZoneId(zoneId: String?) {
        if (zoneId == null) prefs.remove(KEY_LAST_ZONE) else prefs.put(KEY_LAST_ZONE, zoneId)
    }

    override fun getHasSavedServers(): Boolean = prefs.getBoolean(KEY_HAS_SERVERS, false)
    override fun setHasSavedServers(hasSaved: Boolean) = prefs.putBoolean(KEY_HAS_SERVERS, hasSaved)

    fun getLocalAudioQuality(): String? = prefs.get(KEY_AUDIO_QUALITY, null)
    fun setLocalAudioQuality(value: String) = prefs.put(KEY_AUDIO_QUALITY, value)

    fun getArtistInfoProvider(): String? = prefs.get(KEY_ARTIST_INFO_PROVIDER, null)
    fun setArtistInfoProvider(value: String?) {
        if (value == null) prefs.remove(KEY_ARTIST_INFO_PROVIDER) else prefs.put(KEY_ARTIST_INFO_PROVIDER, value)
    }

    fun getOpenAiApiKey(): String? = prefs.get(KEY_OPENAI_API_KEY, null)
    fun setOpenAiApiKey(value: String?) {
        if (value == null) prefs.remove(KEY_OPENAI_API_KEY) else prefs.put(KEY_OPENAI_API_KEY, value)
    }

    fun getOllamaBaseUrl(): String? = prefs.get(KEY_OLLAMA_BASE_URL, null)
    fun setOllamaBaseUrl(value: String?) {
        if (value == null) prefs.remove(KEY_OLLAMA_BASE_URL) else prefs.put(KEY_OLLAMA_BASE_URL, value)
    }

    fun getOllamaModel(): String? = prefs.get(KEY_OLLAMA_MODEL, null)
    fun setOllamaModel(value: String?) {
        if (value == null) prefs.remove(KEY_OLLAMA_MODEL) else prefs.put(KEY_OLLAMA_MODEL, value)
    }

    private companion object {
        const val KEY_LAST_ZONE = "last_active_zone_id"
        const val KEY_HAS_SERVERS = "has_saved_servers"
        const val KEY_AUDIO_QUALITY = "local_audio_quality"
        const val KEY_ARTIST_INFO_PROVIDER = "artist_info_provider"
        const val KEY_OPENAI_API_KEY = "openai_api_key"
        const val KEY_OLLAMA_BASE_URL = "ollama_base_url"
        const val KEY_OLLAMA_MODEL = "ollama_model"
    }
}
