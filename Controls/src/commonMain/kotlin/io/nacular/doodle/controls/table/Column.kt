package io.nacular.doodle.controls.table

import io.nacular.doodle.controls.MutableListModel
import io.nacular.doodle.core.View
import io.nacular.doodle.layout.Constraints
import io.nacular.doodle.utils.ChangeObserver
import io.nacular.doodle.utils.Pool

interface Column<T> {
    val header         : View?
    val width          : Double

    var minWidth       : Double
    var maxWidth       : Double?
    var preferredWidth : Double?
    var cellAlignment  : (Constraints.() -> Unit)?
    var headerAlignment: (Constraints.() -> Unit)?

    fun moveBy(x: Double)
    fun resetPosition()

    val alignmentChanged: Pool<ChangeObserver<Column<T>>>
}

interface MutableColumn<T, R>: Column<R> {
    var editor: TableEditor<T>?

    fun sort          (list: MutableListModel<T>)
    fun sortDescending(list: MutableListModel<T>)
}