@file:Suppress("EXPECTED_EXTERNAL_DECLARATION", "WRONG_MODIFIER_TARGET")

package io.nacular.doodle.dom

public expect open external class Blob: JsAny {
    public val size    : JsNumber
    public val type    : String
    public val isClosed: Boolean
}

public expect external class File: Blob {
    public val name: String
    public val lastModified: Int
}

public expect abstract external class FileList {
    public abstract val length: Int
    public fun item(index: Int): File?
}

public expect fun FileList.asList(): List<File>

public expect external class ProgressEvent: JsAny {
    public val total           : JsNumber
    public val loaded          : JsNumber
    public val lengthComputable: Boolean
}

public expect external class FileReader(): JsAny {
    public val result    : JsAny
    public var onerror   : (             ) -> Unit
    public var onloadend : (             ) -> Unit
    public var onprogress: (ProgressEvent) -> Unit

    public fun readAsText       (delegate: File                  )
    public fun readAsText       (delegate: File, encoding: String)
    public fun readAsDataURL    (delegate: File                  )
    public fun readAsArrayBuffer(delegate: File                  )
}

public expect external class Uint8Array(array: JsArray<JsNumber>) {
    public val length: Int
}

public expect operator fun Uint8Array.get(index: Int): Byte
