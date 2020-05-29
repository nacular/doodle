package com.nectar.doodle.core

import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Point

/**
 * A Behavior can be used by [View]s and [Theme]s to allow delegation of the [View.render] call and other characteristics of the [View].
 * This way, a [View] can support pluggable visual style and behavior.
 */
interface Behavior<in T: View> {
    /**
     * Allows the Behavior to override the View's [View.clipCanvasToBounds] property.
     *
     * @param view being controlled
     * @see View.clipCanvasToBounds
     */
    fun clipCanvasToBounds(view: T): Boolean = view.clipCanvasToBounds_

    /**
     * Invoked to render the given [View].
     *
     * @param view being controlled
     * @param canvas given to the View during a system call to [View.render]
     */
    fun render(view: T, canvas: Canvas) {}

    /**
     * Returns true if the point is within the [View]'s bounds. This can be used to handle cases
     * when the [Behavior] wants to control hit detection.
     *
     * @param view being controlled
     * @param point to check (in view's parent's coordinate system)
     */
    fun contains(view: T, point: Point): Boolean = point in view.bounds

    /**
     * Called when the Behavior is applied to a [View].
     *
     * @param view to control
     */
    fun install(view: T) {}

    /**
     * Called when the Behavior is removed from a [View].
     *
     * @param view being controlled
     */
    fun uninstall(view: T) {}
}