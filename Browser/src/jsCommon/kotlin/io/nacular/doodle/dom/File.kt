@file:Suppress("EXTERNAL_TYPE_EXTENDS_NON_EXTERNAL_TYPE")

package io.nacular.doodle.dom

internal actual open external class Blob: JsAny {
    actual val size    : JsNumber
    actual val type    : String
    actual val isClosed: Boolean
}

internal actual external class File: Blob {
    actual val name: String
    actual val lastModified: Int
}

internal actual abstract external class FileList {
    actual abstract val length: Int
    actual fun item(index: Int): File?
}

internal actual external class ProgressEvent: JsAny {
    actual val total           : JsNumber
    actual val loaded          : JsNumber
    actual val lengthComputable: Boolean
}

internal actual external class FileReader actual constructor(): JsAny {
    actual val result    : JsAny
    actual var onerror   : (             ) -> Unit
    actual var onloadend : (             ) -> Unit
    actual var onprogress: (ProgressEvent) -> Unit

    actual fun readAsText       (delegate: File                  )
    actual fun readAsText       (delegate: File, encoding: String)
    actual fun readAsDataURL    (delegate: File                  )
    actual fun readAsArrayBuffer(delegate: File                  )
}

internal actual external class ArrayBuffer: JsAny

internal actual external class Uint8Array actual constructor(array: ArrayBuffer) {
    actual val length: Int
}

internal actual fun FileList.asList(): List<File> = object: AbstractList<File>() {
    override val size: Int get() = this@asList.length

    override fun get(index: Int): File = when (index) {
        in 0..lastIndex -> this@asList.item(index) as File
        else -> throw IndexOutOfBoundsException("index $index is not in range [0..$lastIndex]")
    }
}
