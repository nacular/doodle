package io.nacular.doodle.core

import io.nacular.doodle.core.ContentDirection.RightLeft
import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.Fill
import io.nacular.doodle.focus.FocusTraversalPolicy
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.utils.ChangeObserver
import io.nacular.doodle.utils.ObservableList
import io.nacular.doodle.utils.Pool
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

    /** Fires when [contentDirection] changes. */
    val contentDirectionChanged: Pool<ChangeObserver<Display>>

    /**
     * Indicates the direction of content within the Display. This is used to support right-to-left locales.
     * Top-level Views without a [View.localContentDirection] specified will inherit this value and pass it on to their
     * descendants that have no explicit value.
     */
    var contentDirection: ContentDirection

    /**
     * Indicates whether the Display should be mirrored (as though transformed using [AffineTransform.flipHorizontally]),
     * when the [contentDirection] is [RightLeft].
     *
     * Apps should set this to `false` if they want more control over how top-level Views are displayed.
     *
     * Defaults to `true`
     */
    var mirrorWhenRightLeft: Boolean

    /** Fires when [mirrored] changes. */
    val mirroringChanged: Pool<ChangeObserver<Display>>

    /**
     * `true` if [contentDirection] == [RightLeft] && [mirrorWhenRightLeft]
     */
    val mirrored get() = contentDirection == RightLeft && mirrorWhenRightLeft

    /**
     * Maps a [Point] within the Display to absolute coordinate-space.
     *
     * @param point to be mapped
     * @returns a Point relative to the un-transformed [Display]
     */
    fun toAbsolute(point: Point): Point

    /**
     * Maps a [Point] from absolute coordinate-space un-transformed [Display], into this Display's coordinate-space.
     * The result is different form the input if the Display's [transform] is not [Identity].
     *
     * @param point to be mapped
     * @returns a Point relative to this Display
     */
    fun fromAbsolute(point: Point): Point

    /** Fills the Display's background with the given fill */
    fun fill(fill: Fill)

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

/**
 * The width of the Display
 * @see Display.size
 */
inline val Display.width  get() = size.width

/**
 * The height of the Display
 * @see Display.size
 */
inline val Display.height get() = size.height

interface InternalDisplay: Display {
    fun repaint()
}
