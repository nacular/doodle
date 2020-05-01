package com.nectar.doodle.core

import com.nectar.doodle.drawing.Brush
import com.nectar.doodle.focus.FocusTraversalPolicy
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.layout.Insets
import com.nectar.doodle.system.Cursor
import com.nectar.doodle.utils.ObservableList
import com.nectar.doodle.utils.PropertyObservers


/**
 * The top-level surface for presenting [View]s.  An item must be added to the Display (either directly, or
 * as a descendant of the Display) before it can be rendered or interact with the user.
 *
 * @author Nicholas Eddy
 */
interface Display: Iterable<View> {
    override fun iterator() = children.iterator()
    /**
     * The top-level cursor.  This will be the cursor used for [View] hierarchies that do not have one set.
     */
    var cursor: Cursor?

    /** The current size of the Display.  This may be smaller than the client's screen in situations where the application is hosted within a window. */
    val size: Size

    /** The layout applied */
    var layout: Layout?

    /** Insets if any */
    var insets: Insets

    /** The list of top-level items added to the Display */
    val children: ObservableList<View>

    /** Fires when the display cursor changes */
    val cursorChanged: PropertyObservers<Display, Cursor?>

    /** Fires when the display re-sizes */
    val sizeChanged: PropertyObservers<Display, Size>

    var focusTraversalPolicy: FocusTraversalPolicy?

    /** Fills the Display's background with the given brush */
    fun fill (brush: Brush)

    /**
     * @param at the x,y within the Display's coordinate-space
     * @return a View if one is found to contain the given point
     */
    fun child(at: Point): View?

    /**
     * @param view in question
     * @return `true` IFF [view] is a descendant of the Display
     */
    infix fun ancestorOf(view: View): Boolean
}

inline val Display.width  get() = size.width
inline val Display.height get() = size.height

interface InternalDisplay: Display {
    fun doLayout()

    fun repaint()
}
