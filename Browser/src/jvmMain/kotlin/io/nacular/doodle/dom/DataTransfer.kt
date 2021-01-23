package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 3/13/20.
 */
public actual abstract class DataTransfer {
    //    fun clearData(format: String = definedExternally)
    public actual open var dropEffect: String = ""
    public actual open var effectAllowed: String = ""
    public actual open val types: Array<out String> = arrayOf("")

    public actual fun getData(format: String): String = ""

    public actual fun setData(format: String, data: String) {}
}