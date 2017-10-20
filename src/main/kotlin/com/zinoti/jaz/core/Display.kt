//package com.zinoti.jaz.core
//
//import com.zinoti.jaz.containers.Padding
//import com.zinoti.jaz.drawing.Brush
//import com.zinoti.jaz.event.BoundsListener
//import com.zinoti.jaz.event.ContainerListener
//import com.zinoti.jaz.event.PropertyListener
//import com.zinoti.jaz.focus.FocusTraversalPolicy
//import com.zinoti.jaz.geometry.Dimension
//import com.zinoti.jaz.system.Cursor
//
//
//interface Display: Iterable<Gizmo> {
//
//    override fun iterator(): Iterator<Gizmo> = children.iterator()
//
//    var cursor: Cursor
//
//    val size                : Dimension
//    var layout              : Layout?
//    var padding             : Padding
//    val children            : MutableList<Gizmo>
//    var minimumSize         : Dimension
//    var focusTraversalPolicy: FocusTraversalPolicy
//
//    fun fill(brush: Brush)
//
//    operator fun plus (listener: BoundsListener   ): Display
//    operator fun minus(listener: BoundsListener   ): Display
//
//    operator fun plus (listener: ContainerListener): Display
//    operator fun minus(listener: ContainerListener): Display
//
//    operator fun plus (listener: PropertyListener ): Display
//    operator fun minus(listener: PropertyListener ): Display
//}
