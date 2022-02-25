package io.nacular.doodle.core

import io.nacular.doodle.core.View.ClipPath
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.Point

/**
 * A Behavior can be used by [View]s and [Theme][io.nacular.doodle.theme.Theme]s to allow delegation of the [View.render]
 * call and other characteristics of the [View]. This way, a [View] can support pluggable visual style and behavior.
 */
public interface Behavior<in T: View> {
    /**
     * Allows the Behavior to override the View's [View.clipCanvasToBounds] property.
     *
     * @param view being controlled
     * @see View.clipCanvasToBounds
     */
    public fun clipCanvasToBounds(view: T): Boolean = view.clipCanvasToBounds_

    /**
     * Allows the Behavior to override the View's [View.childrenClipPath] property.
     *
     * @param view being controlled
     * @see View.childrenClipPath
     */
    public fun childrenClipPath(view: T): ClipPath? = view.childrenClipPath_

    /**
     * Allows the Behavior to override the View's [View.mirrorWhenRightLeft] property.
     *
     * @param view being controlled
     * @see View.mirrorWhenRightLeft
     */
    public fun mirrorWhenRightToLeft(view: T): Boolean = view.mirrorWhenRightLeft

    /**
     * Invoked to render the given [View].
     *
     * @param view being controlled
     * @param canvas given to the View during a system call to [View.render]
     */
    public fun render(view: T, canvas: Canvas) {}

    /**
     * Returns true if the point is within the [View]'s bounds. This can be used to handle cases
     * when the [Behavior] wants to control hit detection.
     *
     * @param view being controlled
     * @param point to check (in view's parent's coordinate system)
     */
    public fun contains(view: T, point: Point): Boolean = point in view.bounds

    /**
     * Called when the Behavior is applied to a [View].
     *
     * @param view to control
     */
    public fun install(view: T) {}

    /**
     * Called when the Behavior is removed from a [View].
     *
     * @param view being controlled
     */
    public fun uninstall(view: T) {}
}