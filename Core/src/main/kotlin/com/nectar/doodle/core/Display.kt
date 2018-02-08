package com.nectar.doodle.core

import com.nectar.doodle.drawing.Brush
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.layout.Insets
import com.nectar.doodle.system.Cursor
import com.nectar.doodle.utils.ObservableList
import com.nectar.doodle.utils.PropertyObservers


interface Display: Iterable<Gizmo> {

    override fun iterator(): Iterator<Gizmo> = children.iterator()

    var cursor: Cursor?

    val size                : Size
    var layout              : Layout?
    var insets              : Insets
    val children            : ObservableList<Gizmo, Gizmo>
    val sizeChanged         : PropertyObservers<Display, Size>
    var minimumSize         : Size
//  var focusTraversalPolicy: FocusTraversalPolicy

    fun fill      (brush: Brush)
    fun child     (at   : Point): Gizmo?
    fun isAncestor(gizmo: Gizmo): Boolean

    fun doLayout()
}
