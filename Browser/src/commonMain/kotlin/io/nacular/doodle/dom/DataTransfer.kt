package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 3/13/20.
 */
public expect abstract class DataTransferItem {
    public val kind: String
    public val type: String
    public fun getAsString(_callback: ((String) -> Unit)?)
    public fun getAsFile(): File?
}

public expect abstract class DataTransferItemList {
    public val length: Int
}

public expect operator fun DataTransferItemList.get(index: Int): DataTransferItem?

public expect abstract class DataTransfer {
    public var dropEffect   : String
    public var effectAllowed: String
    public val types        : Array<out String>
    public val items        : DataTransferItemList
    public val files        : FileList

    public fun getData     (format: String                    ): String
    public fun setData     (format: String, data: String      )
    public fun setDragImage(image : Element, x: Int, y: Int   )
}