package io.nacular.doodle.dom

internal actual typealias DataTransfer         = org.w3c.dom.DataTransfer
internal actual typealias DataTransferItem     = org.w3c.dom.DataTransferItem
internal actual typealias DataTransferItemList = org.w3c.dom.DataTransferItemList

public actual operator fun DataTransferItemList.get(index: Int): DataTransferItem? = this[index]
