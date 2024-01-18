package io.nacular.doodle.dom

public actual open external class Blob: JsAny {
    public actual val size    : JsNumber
    public actual val type    : String
    public actual val isClosed: Boolean
}

public actual external class File: Blob {
    public actual val name: String
    public actual val lastModified: Int
}

public actual abstract external class FileList {
    public actual abstract val length: Int
    public actual fun item(index: Int): File?
}

public actual external class ProgressEvent: JsAny {
    public actual val total           : JsNumber
    public actual val loaded          : JsNumber
    public actual val lengthComputable: Boolean
}

public actual external class FileReader actual constructor(): JsAny {
    public actual val result    : JsAny
    public actual var onerror   : (             ) -> Unit
    public actual var onloadend : (             ) -> Unit
    public actual var onprogress: (ProgressEvent) -> Unit

    public actual fun readAsText       (delegate: File                  )
    public actual fun readAsText       (delegate: File, encoding: String)
    public actual fun readAsDataURL    (delegate: File                  )
    public actual fun readAsArrayBuffer(delegate: File                  )
}

public actual external class ArrayBuffer: JsAny

public actual external class Uint8Array actual constructor(array: ArrayBuffer) {
    public actual val length: Int
}

public actual fun FileList.asList(): List<File> = object: AbstractList<File>() {
    override val size: Int get() = this@asList.length

    override fun get(index: Int): File = when (index) {
        in 0..lastIndex -> this@asList.item(index) as File
        else -> throw IndexOutOfBoundsException("index $index is not in range [0..$lastIndex]")
    }
}
