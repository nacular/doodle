package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 3/13/20.
 */
public expect abstract class DataTransfer {
    public open var dropEffect: String
    public open var effectAllowed: String
//    open val items: DataTransferItemList
public open val types: Array<out String>
//    open val files: FileList
//    fun setDragImage(image: Element, x: Int, y: Int)
    public fun getData(format: String): String
    public fun setData(format: String, data: String)
//    fun clearData(format: String = definedExternally)
}