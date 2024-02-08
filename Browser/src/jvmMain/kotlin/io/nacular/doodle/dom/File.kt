package io.nacular.doodle.dom

internal actual open class Blob: JsAny {
    actual val size    : JsNumber = JsNumber()
    actual val type    : String   = ""
    actual val isClosed: Boolean  = true
}

internal actual class File: Blob() {
    actual val name: String = ""
    actual val lastModified: Int = 0
}
internal actual abstract class FileList {
    actual abstract val length: Int
    actual fun item(index: Int): File? = null
}

internal actual fun FileList.asList(): List<File> = emptyList()

internal actual class ProgressEvent: JsAny {
    actual val total           : JsNumber = JsNumber()
    actual val loaded          : JsNumber = JsNumber()
    actual val lengthComputable: Boolean = false
}

internal actual class FileReader actual constructor(): JsAny {
    actual val result    : JsAny = object: JsAny {}

    actual var onerror   : (             ) -> Unit = {}
    actual var onloadend : (             ) -> Unit = {}
    actual var onprogress: (ProgressEvent) -> Unit = {}

    actual fun readAsText       (delegate: File                  ) {}
    actual fun readAsText       (delegate: File, encoding: String) {}
    actual fun readAsDataURL    (delegate: File                  ) {}
    actual fun readAsArrayBuffer(delegate: File                  ) {}
}

internal actual class ArrayBuffer: JsAny

internal actual class Uint8Array actual constructor(array: ArrayBuffer) {
    actual val length: Int = 0
}

internal actual operator fun Uint8Array.get(index: Int): Byte = 0