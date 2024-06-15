package io.nacular.doodle.dom

internal actual abstract external class DataTransferItem {
    actual val kind: String
    actual val type: String
    actual fun getAsString(_callback: ((String) -> Unit)?)
    actual fun getAsFile(): File?
}

internal actual abstract external class DataTransferItemList {
    actual val length: Int
}

internal actual abstract external class DataTransfer {
    actual var dropEffect   : String
    actual var effectAllowed: String
    actual val types        : JsArray<out JsString>
    actual val items        : DataTransferItemList
    actual val files        : FileList

    actual fun getData     (format: String                 ): String
    actual fun setData     (format: String, data: String   )
    actual fun setDragImage(image : Element, x: Int, y: Int)
}