package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 3/13/20.
 */
actual abstract class DataTransfer {
    //    fun clearData(format: String = definedExternally)
    actual open var dropEffect = ""
    actual open var effectAllowed = ""
    actual open val types: Array<out String> = arrayOf("")

    actual fun getData(format: String) = ""

    actual fun setData(format: String, data: String) {}
}