package com.jrr.jrrkmp_native_ui.data.api

import com.jrr.jrrkmp_native_ui.domain.model.Track
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

@Serializable
internal data class McwsTrackDto(
    @SerialName("Key") val key: String? = null,
    @SerialName("Name") val name: String? = null,
    @SerialName("Artist") val artist: String? = null,
    @SerialName("Album") val album: String? = null,
    @SerialName("Album Artist (auto)") val albumArtistAuto: String? = null,
    @SerialName("Date (readable)") val date: String? = null,
    @SerialName("Genre") val genre: String? = null,
    @SerialName("Duration") val durationSec: JsonElement? = null,
    @SerialName("Track #") val trackNumber: JsonElement? = null,
    @SerialName("Disc #") val discNumber: JsonElement? = null,
    @SerialName("Total Discs") val totalDiscs: JsonElement? = null,
    @SerialName("Total Tracks") val totalTracks: JsonElement? = null,
    @SerialName("Bitrate") val bitrate: JsonElement? = null,
    @SerialName("Bit depth") val bitDepth: JsonElement? = null,
    @SerialName("Sample Rate") val sampleRate: JsonElement? = null,
    @SerialName("Channels") val channels: JsonElement? = null,
    @SerialName("File Type") val fileType: String? = null,
    @SerialName("Filename") val filePath: String? = null,
    @SerialName("Filename (path)") val folderPath: String? = null,
    @SerialName("Number Plays") val numberPlays: JsonElement? = null
)

internal fun McwsTrackDto.toDomainTrack(): Track? {
    val fileKey = key ?: return null
    if (fileKey.isEmpty()) return null

    val durationDouble = durationSec?.jsonPrimitive?.contentOrNull?.toDoubleOrNull() ?: 0.0
    val trackNum = trackNumber?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 0
    val discNum = discNumber?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 1
    val totDiscs = totalDiscs?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 1
    val totTracks = totalTracks?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 1
    val bitr = bitrate?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 0
    val bitDep = bitDepth?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 0
    val sampRate = sampleRate?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 0
    val chan = channels?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 2
    val numPlays = numberPlays?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 0

    return Track(
        fileKey = fileKey,
        name = name ?: "Unknown",
        artist = artist ?: "Unknown",
        album = album ?: "Unknown",
        albumArtist = albumArtistAuto ?: artist ?: "Unknown",
        date = date ?: "",
        genre = genre ?: "Unknown",
        durationMs = (durationDouble * 1000).toLong(),
        trackNumber = trackNum,
        discNumber = discNum,
        totalDiscs = totDiscs,
        totalTracks = totTracks,
        bitrate = bitr,
        bitDepth = bitDep,
        sampleRate = sampRate,
        channels = chan,
        fileType = fileType ?: "Unknown",
        filePath = filePath ?: "",
        folderPath = folderPath ?: "",
        numberPlays = numPlays
    )
}
