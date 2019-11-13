package com.nectar.doodle.controls.table

import com.nectar.doodle.controls.MutableListModel
import com.nectar.doodle.core.View
import com.nectar.doodle.layout.Constraints
import com.nectar.doodle.utils.ChangeObserver
import com.nectar.doodle.utils.Pool

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
    fun sort(list: MutableListModel<T>)
}