package io.nacular.doodle.datatransport

import io.nacular.doodle.dom.File
import io.nacular.measured.units.BinarySize
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time

internal expect class SimpleFile(delegate: File): LocalFile {
    override val name        : String
    override val size        : Measure<BinarySize>
    override val type        : String
    override val isClosed    : Boolean
    override val lastModified: Measure<Time>

    override suspend fun read(progress: (Float) -> Unit): ByteArray?

    override suspend fun readText(encoding: String?, progress: (Float) -> Unit): String?

    override suspend fun readBase64(progress: (Float) -> Unit): String?
}