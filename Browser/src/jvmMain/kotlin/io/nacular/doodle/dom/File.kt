package io.nacular.doodle.dom

public actual open class Blob: JsAny {
    public actual val size    : JsNumber = JsNumber()
    public actual val type    : String   = ""
    public actual val isClosed: Boolean  = true
}

public actual class File: Blob() {
    public actual val name: String = ""
    public actual val lastModified: Int = 0
}
public actual abstract class FileList {
    public actual abstract val length: Int
    public actual fun item(index: Int): File? = null
}

public actual fun FileList.asList(): List<File> = emptyList()

public actual class ProgressEvent: JsAny {
    public actual val total           : JsNumber = JsNumber()
    public actual val loaded          : JsNumber = JsNumber()
    public actual val lengthComputable: Boolean = false
}

public actual class FileReader actual constructor(): JsAny {
    public actual val result    : JsAny = object: JsAny {}

    public actual var onerror   : (             ) -> Unit = {}
    public actual var onloadend : (             ) -> Unit = {}
    public actual var onprogress: (ProgressEvent) -> Unit = {}

    public actual fun readAsText       (delegate: File                  ) {}
    public actual fun readAsText       (delegate: File, encoding: String) {}
    public actual fun readAsDataURL    (delegate: File                  ) {}
    public actual fun readAsArrayBuffer(delegate: File                  ) {}
}

public actual class Uint8Array actual constructor(array: JsArray<JsNumber>) {
    public actual val length: Int = 0
}

public actual operator fun Uint8Array.get(index: Int): Byte = 0