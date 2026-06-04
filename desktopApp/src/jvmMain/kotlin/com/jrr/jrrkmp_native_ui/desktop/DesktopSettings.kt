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

    private companion object {
        const val KEY_LAST_ZONE = "last_active_zone_id"
        const val KEY_HAS_SERVERS = "has_saved_servers"
        const val KEY_AUDIO_QUALITY = "local_audio_quality"
    }
}
