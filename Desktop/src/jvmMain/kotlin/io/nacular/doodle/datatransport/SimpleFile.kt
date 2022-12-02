package io.nacular.doodle.datatransport

import io.nacular.measured.units.BinarySize
import io.nacular.measured.units.Time
import io.nacular.measured.units.times
import kotlinx.coroutines.CancellationException
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.Charset
import java.util.Base64
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class SimpleFile(private val delegate: File): LocalFile {
    override val name: String get() = delegate.name
    override val size         get() = delegate.length() * BinarySize.bytes
    override val type         get() = delegate.extension
    override val isClosed     get() = false
    override val lastModified get() = delegate.lastModified() * Time.milliseconds

    override suspend fun read(progress: (Float) -> Unit): ByteArray? = suspendCoroutine { coroutine ->
        try {
            val result = ByteArrayOutputStream()
            val fileSize = delegate.length()
            var totalRead = 0L

            delegate.forEachBlock { buffer, bytesRead ->
                result.write(buffer)
                totalRead += bytesRead
                progress((fileSize / totalRead).toFloat())
            }

            coroutine.resume(result.toByteArray())
        } catch (e: CancellationException) {
            coroutine.resumeWithException(e)
        }
    }

    override suspend fun readText(encoding: String?, progress: (Float) -> Unit): String? = suspendCoroutine { coroutine ->
        try {
            val result = StringBuilder()
            val fileSize = delegate.length()
            var totalRead = 0L

            delegate.forEachLine(Charset.forName(encoding)) {
                result.append(it)
                totalRead += it.length
                progress((fileSize / totalRead).toFloat())
            }

            coroutine.resume(result.toString())
        } catch (e: CancellationException) {
            coroutine.resumeWithException(e)
        }
    }

    override suspend fun readBase64(progress: (Float) -> Unit): String? = Base64.getEncoder().encodeToString(read(progress))
}