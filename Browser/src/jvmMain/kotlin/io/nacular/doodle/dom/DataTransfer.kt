package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 3/13/20.
 */
internal actual abstract class DataTransferItem {
    actual val type: String = ""

    actual fun getAsFile(): File? = null
}

internal actual abstract class DataTransferItemList {
    actual val length: Int = 0
}

internal actual operator fun DataTransferItemList.get(index: Int): DataTransferItem? = null

internal actual abstract class DataTransfer {
    actual var dropEffect   : String = ""
    actual var effectAllowed: String = ""
    actual val types        : JsArray<out JsString> = JsArray() //arrayOf("")
    actual val items        : DataTransferItemList get() = TODO("Not yet implemented")
    actual val files        : FileList get() = object: FileList() {
        override val length get() = 0
    }

    actual fun getData(format: String): String = ""
    actual fun setData(format: String, data: String) {}

    actual fun setDragImage(image: Element, x: Int, y: Int) {}
}