package io.nacular.doodle.dom

import org.w3c.dom.asList

public actual typealias File     = org.w3c.files.File
public actual typealias FileList = org.w3c.files.FileList

public actual inline fun FileList.asList(): List<File> = this.asList()