package com.jrr.jrrkmp_native_ui.data.api

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import co.touchlab.kermit.Logger
import com.jrr.jrrkmp_native_ui.core.di.appContainer
import com.jrr.jrrkmp_native_ui.core.network.acceptAllHostnameVerifier
import com.jrr.jrrkmp_native_ui.core.network.trustAllSslSocketFactory
import com.jrr.jrrkmp_native_ui.core.network.trustAllTrustManager
import com.jrr.jrrkmp_native_ui.data.db.entity.DownloadedTrackEntity
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

private val log = Logger.withTag("playback:DownloadWorker")

class DownloadWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val fileKey = inputData.getString("file_key") ?: return Result.failure()
        val jobId = inputData.getInt("job_id", -1)
        if (jobId == -1) return Result.failure()
        log.i { "doWork fileKey=$fileKey jobId=$jobId" }

        val container = context.appContainer
        val db = container.database
        val jobDao = db.downloadJobDao()
        val trackDao = db.downloadedTrackDao()

        // 1. Get the job entity
        val job = jobDao.getJobById(jobId) ?: return Result.failure()

        // Update state to downloading
        jobDao.update(job.copy(state = "DOWNLOADING", startedAt = System.currentTimeMillis()))

        // Server transcodes to the selected quality on the fly. The downloaded
        // file's real format therefore matches the conversion (flac/opus), not
        // the library's original fileType — so persist that below too.
        val quality = container.facade.currentLocalAudioQuality
        log.i { "doWork quality=${quality.name} fileKey=$fileKey" }

        // 2. Download + artwork URLs come from the facade (single source of
        // truth: active server + quality + Channels=2; downloads omit
        // Playback=1). Empty means no active server — WorkManager may run this
        // after the connection is gone (or after process death, before it's
        // restored); fail rather than target a stale saved server.
        val downloadUrl = container.facade.streamUrl(fileKey, playback = false)
        if (downloadUrl.isEmpty()) {
            log.w { "doWork: no active server, failing job fileKey=$fileKey" }
            jobDao.update(job.copy(state = "FAILED"))
            return Result.failure()
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .sslSocketFactory(trustAllSslSocketFactory, trustAllTrustManager)
            .hostnameVerifier(acceptAllHostnameVerifier)
            .build()

        val request = Request.Builder()
            .url(downloadUrl)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    jobDao.update(job.copy(state = "FAILED"))
                    return Result.failure()
                }

                val body = response.body ?: run {
                    jobDao.update(job.copy(state = "FAILED"))
                    return Result.failure()
                }

                val totalBytes = body.contentLength()
                if (totalBytes > 0) {
                    jobDao.update(job.copy(state = "DOWNLOADING", bytesTotal = totalBytes))
                }

                // Create local directories
                val downloadsDir = File(context.filesDir, "downloads")
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }

                // The transcoded stream is delivered in the conversion format,
                // so name/store it accordingly (flac or opus).
                val extension = quality.conversion
                val tempFile = File(downloadsDir, "temp_${fileKey}.$extension")
                val finalFile = File(downloadsDir, "${fileKey}.$extension")

                var bytesCopied = 0L
                val buffer = ByteArray(8192)

                body.byteStream().use { inputStream ->
                    FileOutputStream(tempFile).use { outputStream ->
                        var bytes = inputStream.read(buffer)
                        var lastProgressUpdate = System.currentTimeMillis()

                        while (bytes >= 0) {
                            outputStream.write(buffer, 0, bytes)
                            bytesCopied += bytes

                            // Throttle progress updates to avoid overloading Room/SQLite database
                            val now = System.currentTimeMillis()
                            if (now - lastProgressUpdate > 300) {
                                val currentJob = jobDao.getJobById(jobId)
                                if (currentJob != null) {
                                    jobDao.update(
                                        currentJob.copy(
                                            state = "DOWNLOADING",
                                            bytesDownloaded = bytesCopied,
                                            bytesTotal = if (totalBytes > 0) totalBytes else bytesCopied
                                        )
                                    )
                                }
                                setProgress(workDataOf("bytes_downloaded" to bytesCopied, "bytes_total" to totalBytes))
                                lastProgressUpdate = now
                            }

                            bytes = inputStream.read(buffer)
                        }
                    }
                }

                // Rename temp file to final file
                if (tempFile.exists()) {
                    if (finalFile.exists()) {
                        finalFile.delete()
                    }
                    tempFile.renameTo(finalFile)
                }

                // Download album artwork (full size, from the facade)
                try {
                    val imageUrl = container.facade.fullArtworkUrl(fileKey)
                    val imageRequest = Request.Builder().url(imageUrl).build()
                    client.newCall(imageRequest).execute().use { imageResponse ->
                        if (imageResponse.isSuccessful) {
                            val imgBody = imageResponse.body
                            if (imgBody != null) {
                                val artFile = File(downloadsDir, "art_${fileKey}.jpg")
                                val tempArtFile = File(downloadsDir, "temp_art_${fileKey}.jpg")
                                imgBody.byteStream().use { input ->
                                    FileOutputStream(tempArtFile).use { output ->
                                        input.copyTo(output)
                                    }
                                }
                                if (tempArtFile.exists()) {
                                    if (artFile.exists()) {
                                        artFile.delete()
                                    }
                                    tempArtFile.renameTo(artFile)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    log.w(e) { "artwork download failed fileKey=$fileKey" }
                }

                // 3. Add to downloaded_tracks. Copy the library identity fields
                // (date, disc/track numbers, folderPath) from the job — they
                // feed Track.albumGroupId, so dropping them would merge
                // same-named albums in the offline library. The technical
                // fields describe the on-disk file instead: the server
                // transcoded it (fileType = conversion, stereo via Channels=2),
                // so the original bitrate/bitDepth/sampleRate no longer apply.
                val downloadedTrack = DownloadedTrackEntity(
                    fileKey = fileKey,
                    name = job.name,
                    artist = job.artist,
                    album = job.album,
                    albumArtist = job.albumArtist,
                    date = job.date,
                    durationMs = job.durationMs,
                    trackNumber = job.trackNumber,
                    genre = job.genre,
                    discNumber = job.discNumber,
                    totalDiscs = job.totalDiscs,
                    totalTracks = job.totalTracks,
                    bitrate = 0,
                    bitDepth = 0,
                    sampleRate = 0,
                    channels = 2,
                    fileType = quality.conversion,
                    filePath = finalFile.absolutePath,
                    folderPath = job.folderPath
                )
                trackDao.insert(downloadedTrack)

                // 4. Remove the job from queue
                jobDao.delete(job)

                return Result.success()
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            // WorkManager cancelled the worker (e.g. disconnect cancels all
            // downloads) — propagate; the job row is already gone.
            throw e
        } catch (e: Exception) {
            log.e(e) { "doWork failed fileKey=$fileKey jobId=$jobId" }
            val currentJob = jobDao.getJobById(jobId)
            if (currentJob != null) {
                jobDao.update(currentJob.copy(state = "FAILED"))
            }
            return Result.failure()
        }
    }
}
