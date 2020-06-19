package io.nacular.doodle.core

import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.Fill
import io.nacular.doodle.focus.FocusTraversalPolicy
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.utils.ObservableList
import io.nacular.doodle.utils.PropertyObservers


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

    /**
     * Affine transform applied to the View.  This transform does not affect the Display's [size] or how it is handled by [Layout].
     * Hit-detection is handled correctly such that the pointer intersects with the Display as expected after transformation.
     * So no additional handling is necessary in general. The default is [Identity]
     */
    var transform: AffineTransform

    /** The list of top-level items added to the Display */
    val children: ObservableList<View>

    /** Fires when the display cursor changes */
    val cursorChanged: PropertyObservers<Display, Cursor?>

    /** Fires when the display re-sizes */
    val sizeChanged: PropertyObservers<Display, Size>

    var focusTraversalPolicy: FocusTraversalPolicy?

    /** Fills the Display's background with the given fill */
    fun fill (fill: Fill)

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

    /** Prompts the Display to layout its children if it has a Layout installed. */
    fun relayout()
}

inline val Display.width  get() = size.width
inline val Display.height get() = size.height

interface InternalDisplay: Display {
    fun repaint()
}
