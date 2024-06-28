@file:Suppress("EXPECTED_EXTERNAL_DECLARATION", "WRONG_MODIFIER_TARGET")

package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 3/13/20.
 */
internal expect abstract external class DataTransferItem {
    internal val kind: String
    internal val type: String
    internal fun getAsString(_callback: ((String) -> Unit)?)
    internal fun getAsFile(): File?
}

internal expect abstract external class DataTransferItemList {
    internal val length: Int
}

internal expect operator fun DataTransferItemList.get(index: Int): DataTransferItem?

internal expect abstract external class DataTransfer {
    internal var dropEffect   : String
    internal var effectAllowed: String
    internal val types        : JsArray<out JsString>
    internal val items        : DataTransferItemList
    internal val files        : FileList

    internal fun getData     (format: String                    ): String
    internal fun setData     (format: String, data: String      )
    internal fun setDragImage(image : Element, x: Int, y: Int   )
}