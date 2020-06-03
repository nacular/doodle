package com.nectar.doodle.core

import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Size


/**
 * Represents graphical data within the boundaries of a [View].  An example is the icon used by a button to
 * indicate its function.
 *
 * @author Nicholas Eddy
 */
interface Icon<in T: View> {

    /**
     * The icon's size for the given view.
     *
     * @param view to get size for
     */
    fun size(view: T): Size

    /**
     * Renders the icon onto the surface of the given [View],
     * with the icon's top-left at the point indicated.  The top-left
     * location is that of the icon's bounding-box.
     *
     * @param view   The View this icon is to represent
     * @param canvas The View's Canvas
     * @param at     Where the top-left corner of the icon should be rendered
     */
    fun render(view: T, canvas: Canvas, at: Point)
}