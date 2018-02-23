package com.nectar.doodle.core

import com.nectar.doodle.drawing.Brush
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.layout.Insets
import com.nectar.doodle.system.Cursor
import com.nectar.doodle.utils.ObservableList
import com.nectar.doodle.utils.PropertyObservers


/**
 * The top-level surface for presenting [Gizmo]s.  An item must be added to the Display (either directly, or
 * as a descendant of the Display) before it can be rendered or interact with the user.
 */
interface Display: Iterable<Gizmo> {

    override fun iterator(): Iterator<Gizmo> = children.iterator()

    /**
     * The top-level cursor.  This will be the cursor used for Gizmo hierarchies that do not have one set.
     */
    var cursor: Cursor?

    /** The current size of the Display.  This may be smaller than the client's screen in situations where the application is hosted within a window. */
    val size: Size

    /** The layout applied */
    var layout: Layout?

    /** Insets if any */
    var insets: Insets

    /** The list of top-level items added to the Display */
    val children: ObservableList<Gizmo, Gizmo>

    /** Fires when the display re-sizes */
    val sizeChanged: PropertyObservers<Display, Size>

    /**
     * Gets the Gizmo's z-index.
     *
     * @param of The Gizmo
     * @return The z-index (-1 if the Gizmo is not a child)
     */
    fun zIndex(of: Gizmo): Int

    /**
     * Sets the Gizmo's z-index if it is a child.
     *
     * @param of the Gizmo
     * @param to the new z-index
     */
    fun setZIndex(of: Gizmo, to: Int)

//  var focusTraversalPolicy: FocusTraversalPolicy

    fun fill      (brush: Brush)
    fun child     (at   : Point): Gizmo?
    fun isAncestor(gizmo: Gizmo): Boolean

    fun doLayout()
}
