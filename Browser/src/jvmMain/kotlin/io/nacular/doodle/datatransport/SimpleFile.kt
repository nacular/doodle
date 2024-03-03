package io.nacular.doodle.datatransport

import io.nacular.doodle.dom.File
import io.nacular.doodle.utils.zeroMillis
import io.nacular.measured.units.BinarySize.Companion.bytes
import io.nacular.measured.units.times

internal actual class SimpleFile actual constructor(delegate: File): LocalFile {
    actual override val name = ""
    actual override val size = 0 * bytes
    actual override val type = ""
    actual override val isClosed = false
    actual override val lastModified = zeroMillis

    actual override suspend fun read(progress: (Float) -> Unit): ByteArray? = null
    actual override suspend fun readText(encoding: String?, progress: (Float) -> Unit): String? = null
    actual override suspend fun readBase64(progress: (Float) -> Unit): String? = null
}