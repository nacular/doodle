@file:Suppress("EXPECTED_EXTERNAL_DECLARATION", "WRONG_MODIFIER_TARGET")

package io.nacular.doodle.dom

internal expect open external class Blob: JsAny {
    val size    : JsNumber
    val type    : String
    val isClosed: Boolean
}

internal expect external class File: Blob {
    val name: String
    val lastModified: Int
}

internal expect abstract external class FileList {
    abstract val length: Int
    fun item(index: Int): File?
}

internal expect fun FileList.asList(): List<File>

internal expect external class ProgressEvent: JsAny {
    val total           : JsNumber
    val loaded          : JsNumber
    val lengthComputable: Boolean
}

internal expect external class FileReader(): JsAny {
    val result    : JsAny
    var onerror   : (             ) -> Unit
    var onloadend : (             ) -> Unit
    var onprogress: (ProgressEvent) -> Unit

    fun readAsText       (delegate: File                  )
    fun readAsText       (delegate: File, encoding: String)
    fun readAsDataURL    (delegate: File                  )
    fun readAsArrayBuffer(delegate: File                  )
}

internal expect external class ArrayBuffer: JsAny

internal expect external class Uint8Array(array: ArrayBuffer) {
    val length: Int
}

internal expect operator fun Uint8Array.get(index: Int): Byte
