package com.jrr.jrrkmp_native_ui.data.api

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.jrr.jrrkmp_native_ui.JrrDependencies
import com.jrr.jrrkmp_native_ui.data.repository.ServerRepository
import com.jrr.jrrkmp_native_ui.core.network.SslHelper
import com.jrr.jrrkmp_native_ui.data.db.entity.DownloadedTrackEntity
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class DownloadWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val fileKey = inputData.getString("file_key") ?: return Result.failure()
        val jobId = inputData.getInt("job_id", -1)
        if (jobId == -1) return Result.failure()

        val db = JrrDependencies.getDatabase(context)
        val jobDao = db.downloadJobDao()
        val trackDao = db.downloadedTrackDao()
        val serverRepository = JrrDependencies.getServerRepository(context)

        // 1. Get the job entity
        val job = jobDao.getJobById(jobId) ?: return Result.failure()

        // Update state to downloading
        jobDao.update(job.copy(state = "DOWNLOADING", startedAt = System.currentTimeMillis()))

        // 2. Resolve server parameters
        val activeServer = serverRepository.getLastUsedServer() ?: return Result.failure()
        val host = activeServer.host
        val scheme = if (activeServer.useSsl) "https" else "http"
        val port = if (activeServer.useSsl) activeServer.sslPort else activeServer.port
        val token = activeServer.authToken ?: ""

        val downloadUrl = "$scheme://$host:$port/MCWS/v1/File/GetFile?File=$fileKey&Token=$token"

        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .sslSocketFactory(SslHelper.sslSocketFactory, SslHelper.trustAllTrustManager)
            .hostnameVerifier(SslHelper.hostnameVerifier)
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

                val extension = if (job.fileType.isNotEmpty()) job.fileType.lowercase() else "mp3"
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

                // Download album artwork
                try {
                    val imageUrl = "$scheme://$host:$port/MCWS/v1/File/GetImage?File=$fileKey&Token=$token"
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
                    e.printStackTrace()
                }

                // 3. Add to downloaded_tracks
                val downloadedTrack = DownloadedTrackEntity(
                    fileKey = fileKey,
                    name = job.name,
                    artist = job.artist,
                    album = job.album,
                    albumArtist = job.albumArtist,
                    date = "",
                    durationMs = job.durationMs,
                    trackNumber = job.trackNumber,
                    genre = job.genre,
                    discNumber = 1,
                    totalDiscs = 1,
                    totalTracks = 1,
                    bitrate = 0,
                    bitDepth = 0,
                    sampleRate = 0,
                    channels = 2,
                    fileType = job.fileType,
                    filePath = finalFile.absolutePath,
                    folderPath = ""
                )
                trackDao.insert(downloadedTrack)

                // 4. Remove the job from queue
                jobDao.delete(job)

                return Result.success()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val currentJob = jobDao.getJobById(jobId)
            if (currentJob != null) {
                jobDao.update(currentJob.copy(state = "FAILED"))
            }
            return Result.failure()
        }
    }
}
