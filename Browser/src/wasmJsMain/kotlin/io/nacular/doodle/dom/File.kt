package io.nacular.doodle.dom

//import org.w3c.dom.asList

//public actual typealias Blob     = org.w3c.files.Blob
//public actual typealias File     = org.w3c.files.File
//public actual typealias FileList = org.w3c.files.FileList

//public actual inline fun FileList.asList(): List<File> = this.asList()

//public actual open external class Blob {
//    public actual val size: JsNumber
//    public actual val type: String
//    public actual val isClosed: Boolean
//}
//
//public actual external class File: Blob {
//    public actual val name: String
//    public actual val lastModified: Int
//}
//
//public actual abstract external class FileList {
//    public actual abstract val length: Int
//    public actual fun item(index: Int): File?
////    public actual fun asList(): List<File>
//}

public actual operator fun Uint8Array.get(index: Int): Byte = jsGet(this, index)

private fun jsGet(array: Uint8Array, index: Int): Byte = js("array[index]")
