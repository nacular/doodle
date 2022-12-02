package io.nacular.doodle.datatransport

import io.nacular.measured.units.BinarySize
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time
import io.nacular.measured.units.times
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.files.FileReader
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Created by Nicholas Eddy on 12/28/21.
 */
internal class SimpleFile(private val delegate: org.w3c.files.File): LocalFile {
    override val name        : String get() = delegate.name
    override val size        : Measure<BinarySize> get() = delegate.size * BinarySize.bytes
    override val type        : String get() = delegate.type
    override val isClosed    : Boolean get() = delegate.isClosed
    override val lastModified: Measure<Time> get() = delegate.lastModified * Time.milliseconds

    override suspend fun read(progress: (Float) -> Unit): ByteArray? = suspendCoroutine { coroutine ->
        try {
            val reader = FileReader()

            reader.onerror = {
                coroutine.resume(null)
            }

            reader.onloadend = {
                val uint8Array = Uint8Array(reader.result as ArrayBuffer)

                coroutine.resume((0 until uint8Array.length).map {
                    uint8Array[it]
                }.toByteArray())
            }

            reader.onprogress = {
                if (it.lengthComputable) {
                    progress((it.loaded.toDouble() / it.total.toDouble()).toFloat())
                }
            }

            reader.readAsArrayBuffer(delegate)
        } catch (e: CancellationException) {
            coroutine.resumeWithException(e)
        }
    }

    override suspend fun readText(encoding: String?, progress: (Float) -> Unit): String? = suspendCoroutine { coroutine ->
        try {
            val reader = FileReader()

            reader.onerror = {
                coroutine.resume(null)
            }

            reader.onloadend = {
                coroutine.resume(reader.result as String)
            }

            reader.onprogress = {
                if (it.lengthComputable) {
                    progress((it.loaded.toDouble() / it.total.toDouble()).toFloat())
                }
            }

            when (encoding) {
                null -> reader.readAsText(delegate          )
                else -> reader.readAsText(delegate, encoding)
            }

        } catch (e: CancellationException) {
            coroutine.resumeWithException(e)
        }
    }

    override suspend fun readBase64(progress: (Float) -> Unit): String? = suspendCoroutine { coroutine ->
        try {
            val reader = FileReader()

            reader.onerror = {
                coroutine.resume(null)
            }

            reader.onloadend = {
                var encoded = (reader.result as String).replace("^data:(.*,)?".toRegex(), "")

                if ((encoded.length % 4) > 0) {
                    encoded = encoded.padEnd(4 - (encoded.length % 4), '=')
                }

                coroutine.resume(encoded)
            }

            reader.onprogress = {
                if (it.lengthComputable) {
                    progress((it.loaded.toDouble() / it.total.toDouble()).toFloat())
                }
            }

            reader.readAsDataURL(delegate)

        } catch (e: CancellationException) {
            coroutine.resumeWithException(e)
        }
    }
}