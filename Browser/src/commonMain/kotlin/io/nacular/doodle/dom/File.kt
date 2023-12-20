package io.nacular.doodle.dom

public expect class File

public expect abstract class FileList {
    public abstract val length: Int
    public fun item(index: Int): File?
}

public expect fun FileList.asList(): List<File>

