package io.nacular.doodle.dom

public actual class File {}

public actual abstract class FileList {
    public actual abstract val length: Int
    public actual fun item(index: Int): File? = null
}

public actual fun FileList.asList(): List<File> = emptyList()