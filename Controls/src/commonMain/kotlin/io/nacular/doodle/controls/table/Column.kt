package io.nacular.doodle.controls.table

import io.nacular.doodle.core.View
import io.nacular.doodle.layout.Constraints
import io.nacular.doodle.utils.ChangeObserver
import io.nacular.doodle.utils.Pool

public interface Column<T> {
    public val header         : View?
    public val width          : Double

    public var minWidth       : Double
    public var maxWidth       : Double?
    public var preferredWidth : Double?
    public var cellAlignment  : (Constraints.() -> Unit)?
    public var headerAlignment: (Constraints.() -> Unit)?

    public fun moveBy(x: Double)
    public fun resetPosition()

    public val alignmentChanged: Pool<ChangeObserver<Column<T>>>
}

public interface MutableColumn<T, R>: Column<R> {
    public var editor    : TableEditor<T>?
    public var comparator: Comparator<T>?
}