package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 3/13/20.
 */
expect abstract class DataTransfer {
    open var dropEffect: String
    open var effectAllowed: String
//    open val items: DataTransferItemList
    open val types: Array<out String>
//    open val files: FileList
//    fun setDragImage(image: Element, x: Int, y: Int)
    fun getData(format: String): String
    fun setData(format: String, data: String)
//    fun clearData(format: String = definedExternally)
}