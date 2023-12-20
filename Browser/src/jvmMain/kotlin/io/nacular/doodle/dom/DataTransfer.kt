package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 3/13/20.
 */
public actual abstract class DataTransferItem {
    public actual val kind: String = ""
    public actual val type: String = ""

    public actual fun getAsString(_callback: ((String) -> Unit)?) {}
    public actual fun getAsFile(): File? = null
}

public actual abstract class DataTransferItemList {
    public actual val length: Int = 0
}

public actual operator fun DataTransferItemList.get(index: Int): DataTransferItem? = null

public actual abstract class DataTransfer {
    public actual var dropEffect   : String = ""
    public actual var effectAllowed: String = ""
    public actual val types        : Array<out String> = arrayOf("")
    public actual val items        : DataTransferItemList get() = TODO("Not yet implemented")
    public actual val files        : FileList get() = object: FileList() {
        override val length get() = 0
    }

    public actual fun getData(format: String): String = ""
    public actual fun setData(format: String, data: String) {}

    public actual fun setDragImage(image: Element, x: Int, y: Int) {}
}