package io.nacular.doodle.core

import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size


/**
 * Represents graphical data associated with an item. An example is the icon used by a button to
 * indicate its function.
 *
 * @author Nicholas Eddy
 */
public interface Icon<in T: Any> {

    /**
     * The icon's size for the given value.
     *
     * @param view to get size for
     */
    public fun size(view: T): Size // FIXME: Rename view -> item in 0.10.0

    /**
     * Renders the icon onto the given [canvas], with the icon's top-left at the point indicated. The top-left
     * location is that of the icon's bounding-box.
     *
     * @param view   The item this icon is to represent
     * @param canvas The Canvas onto which this icon is being rendered
     * @param at     Where the top-left corner of the icon should be rendered
     */
    public fun render(view: T, canvas: Canvas, at: Point) // FIXME: Rename view -> item in 0.10.0
}