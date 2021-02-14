package io.nacular.doodle.core

import io.nacular.doodle.core.ContentDirection.RightLeft
import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.Paint
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
public interface Display: Iterable<View> {
    override fun iterator(): MutableIterator<View> = children.iterator()
    /**
     * The top-level cursor.  This will be the cursor used for [View] hierarchies that do not have one set.
     */
    public var cursor: Cursor?

    /** The current size of the Display.  This may be smaller than the client's screen in situations where the application is hosted within a window. */
    public val size: Size

    /** The layout applied */
    public var layout: Layout?

    /** Insets if any */
    public var insets: Insets

    /**
     * Affine transform applied to the View.  This transform does not affect the Display's [size] or how it is handled by [Layout].
     * Hit-detection is handled correctly such that the pointer intersects with the Display as expected after transformation.
     * So no additional handling is necessary in general. The default is [Identity]
     */
    public var transform: AffineTransform

    /** The list of top-level items added to the Display */
    public val children: ObservableList<View>

    /** Fires when the display cursor changes */
    public val cursorChanged: PropertyObservers<Display, Cursor?>

    /** Fires when the display re-sizes */
    public val sizeChanged: PropertyObservers<Display, Size>

    public var focusTraversalPolicy: FocusTraversalPolicy?

    /** Fires when [contentDirection] changes. */
    public val contentDirectionChanged: Pool<ChangeObserver<Display>>

    /**
     * Indicates the direction of content within the Display. This is used to support right-to-left locales.
     * Top-level Views without a [View.localContentDirection] specified will inherit this value and pass it on to their
     * descendants that have no explicit value.
     */
    public var contentDirection: ContentDirection

    /**
     * Indicates whether the Display should be mirrored (as though transformed using [AffineTransform.flipHorizontally]),
     * when the [contentDirection] is [RightLeft].
     *
     * Apps should set this to `false` if they want more control over how top-level Views are displayed.
     *
     * Defaults to `true`
     */
    public var mirrorWhenRightLeft: Boolean

    /** Fires when [mirrored] changes. */
    public val mirroringChanged: Pool<ChangeObserver<Display>>

    /**
     * `true` if [contentDirection] == [RightLeft] && [mirrorWhenRightLeft]
     */
    public val mirrored: Boolean get() = contentDirection == RightLeft && mirrorWhenRightLeft

    /**
     * Maps a [Point] within the Display to absolute coordinate-space.
     *
     * @param point to be mapped
     * @returns a Point relative to the un-transformed [Display]
     */
    public fun toAbsolute(point: Point): Point

    /**
     * Maps a [Point] from absolute coordinate-space un-transformed [Display], into this Display's coordinate-space.
     * The result is different form the input if the Display's [transform] is not [Identity].
     *
     * @param point to be mapped
     * @returns a Point relative to this Display
     */
    public fun fromAbsolute(point: Point): Point

    /** Fills the Display's background with the given fill */
    public fun fill(fill: Paint)

    /**
     * @param at the x,y within the Display's coordinate-space
     * @return a View if one is found to contain the given point
     */
    public fun child(at: Point): View?

    /**
     * @param view in question
     * @return `true` IFF [view] is a descendant of the Display
     */
    public infix fun ancestorOf(view: View): Boolean

    /** Prompts the Display to layout its children if it has a Layout installed. */
    public fun relayout()
}

/**
 * The width of the Display
 * @see Display.size
 */
public inline val Display.width: Double get() = size.width

/**
 * The height of the Display
 * @see Display.size
 */
public inline val Display.height: Double get() = size.height

public inline fun Display.fill(color: Color): Unit = fill(ColorPaint(color))

/**
 * Adds [view] to the Display.
 *
 * @param view to be added
 */
public inline operator fun Display.plusAssign(view: View): Unit = children.plusAssign(view)

/**
 * Adds the given [views] to the Display.
 *
 * @param views to be added
 */
public inline operator fun Display.plusAssign(views: Iterable<View>): Unit = children.plusAssign(views)

/**
 * Removes [view] from the Display.
 *
 * @param view to be removed
 */
public inline operator fun Display.minusAssign(view: View): Unit = children.minusAssign(view)

/**
 * Removes the given [views] from the Display.
 *
 * @param views to be removed
 */
public inline operator fun Display.minusAssign(views: Iterable<View>): Unit = children.minusAssign(views)

public interface InternalDisplay: Display {
    public fun repaint()
}
