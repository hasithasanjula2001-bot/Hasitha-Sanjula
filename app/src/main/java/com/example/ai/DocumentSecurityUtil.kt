package com.example.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest

data class PreparedDocument(
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val sha256Hex: String,
    val bitmap: Bitmap?,
    val rawBytes: ByteArray?,
    val textContent: String?,
    val sourceUriString: String
)

object DocumentSecurityUtil {
    private const val MAX_INLINE_BYTES = 18L * 1024L * 1024L // Keep safely under 20MB limit
    private const val MAX_IMAGE_DIMENSION = 1800

    fun createCameraCaptureUri(context: Context): Uri {
        val scansDir = File(context.cacheDir, "scans").apply { mkdirs() }
        val file = File(scansDir, "lc_scan_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun prepareDocument(context: Context, uri: Uri, fallbackMime: String? = null): Result<PreparedDocument> {
        return runCatching {
            val resolver = context.contentResolver
            val detectedMime = resolver.getType(uri) ?: fallbackMime ?: "application/octet-stream"
            val displayName = resolveDisplayName(context, uri) ?: "trade_document"

            val rawBytes = resolver.openInputStream(uri)?.use { input ->
                val buffer = ByteArrayOutputStream()
                val chunk = ByteArray(16 * 1024)
                var total = 0L
                while (true) {
                    val read = input.read(chunk)
                    if (read == -1) break
                    total += read
                    if (total > MAX_INLINE_BYTES) {
                        throw IllegalArgumentException("Document exceeds the 18 MB secure verification limit.")
                    }
                    buffer.write(chunk, 0, read)
                }
                buffer.toByteArray()
            } ?: throw IllegalStateException("Unable to open document stream from device.")

            if (rawBytes.isEmpty()) {
                throw IllegalArgumentException("Selected document is empty.")
            }

            val sha256Hex = computeSha256(rawBytes)

            val isImage = detectedMime.startsWith("image/")
            val isText = detectedMime.startsWith("text/") ||
                displayName.endsWith(".txt", ignoreCase = true) ||
                displayName.endsWith(".mt700", ignoreCase = true)

            val bitmap = if (isImage) decodeScaledBitmap(rawBytes) else null
            val textContent = if (isText) rawBytes.toString(Charsets.UTF_8) else null

            PreparedDocument(
                fileName = displayName,
                mimeType = detectedMime,
                sizeBytes = rawBytes.size.toLong(),
                sha256Hex = sha256Hex,
                bitmap = bitmap,
                rawBytes = if (!isImage && !isText) rawBytes else null,
                textContent = textContent,
                sourceUriString = uri.toString()
            )
        }
    }

    fun computeSha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString(separator = "") { "%02x".format(it) }
    }

    fun verifyUriHash(context: Context, uriString: String, expectedSha256: String): Result<Boolean> {
        return runCatching {
            val uri = Uri.parse(uriString)
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IllegalStateException("Original file is no longer accessible at its URI.")
            computeSha256(bytes).equals(expectedSha256, ignoreCase = true)
        }
    }

    private fun resolveDisplayName(context: Context, uri: Uri): String? {
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) return cursor.getString(idx)
                }
            }
        }
        return uri.lastPathSegment?.substringAfterLast('/')
    }

    private fun decodeScaledBitmap(bytes: ByteArray): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        val maxDim = maxOf(bounds.outWidth, bounds.outHeight)
        var sampleSize = 1
        while (maxDim / sampleSize > MAX_IMAGE_DIMENSION) {
            sampleSize *= 2
        }
        val opts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
    }
}
