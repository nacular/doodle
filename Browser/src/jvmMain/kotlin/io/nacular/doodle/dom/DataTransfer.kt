package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 3/13/20.
 */
internal actual abstract class DataTransferItem {
    internal actual val kind: String = ""
    internal actual val type: String = ""

    internal actual fun getAsString(_callback: ((String) -> Unit)?) {}
    internal actual fun getAsFile(): File? = null
}

internal actual abstract class DataTransferItemList {
    internal actual val length: Int = 0

    internal actual operator fun get(index: Int): DataTransferItem?  = null
}

internal actual abstract class DataTransfer {
    internal actual var dropEffect   : String = ""
    internal actual var effectAllowed: String = ""
    internal actual val types        : JsArray<out JsString> = JsArray() //arrayOf("")
    internal actual val items        : DataTransferItemList get() = TODO("Not yet implemented")
    internal actual val files        : FileList get() = object: FileList() {
        override val length get() = 0
    }

    internal actual fun getData(format: String): String = ""
    internal actual fun setData(format: String, data: String) {}

    internal actual fun setDragImage(image: Element, x: Int, y: Int) {}
}