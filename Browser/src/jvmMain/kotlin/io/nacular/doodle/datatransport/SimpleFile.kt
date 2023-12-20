package io.nacular.doodle.datatransport

import io.nacular.doodle.dom.File
import io.nacular.doodle.utils.zeroMillis
import io.nacular.measured.units.BinarySize.Companion.bytes
import io.nacular.measured.units.times

internal actual class SimpleFile actual constructor(delegate: File): LocalFile {
    override val name = ""
    override val size = 0 * bytes
    override val type = ""
    override val isClosed = false
    override val lastModified = zeroMillis

    override suspend fun read(progress: (Float) -> Unit): ByteArray? = null
    override suspend fun readText(encoding: String?, progress: (Float) -> Unit): String? = null
    override suspend fun readBase64(progress: (Float) -> Unit): String? = null
}