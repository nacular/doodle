package com.nectar.doodle.controls.theme

import com.nectar.doodle.controls.panels.TabbedPanel
import com.nectar.doodle.core.Container
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.theme.Renderer

/**
 * Created by Nicholas Eddy on 4/2/18.
 */

interface TabbedPanelUI<T> {
    /**
     * Invoked to render the given [View].
     *
     * @param view  the View being rendered
     * @param canvas the Canvas given to the View during a system call to [View.render]
     */
    fun render(panel: TabbedPanel<T>, canvas: Canvas)

    /**
     * Returns true if the [View] contains point.  This can be used to handle cases when the [Renderer] wants to control hit detection.
     *
     * @param view
     * @param point
     */
    fun contains(panel: TabbedPanel<T>, point: Point): Boolean = point in panel.bounds

    fun install  (panel: TabbedPanel<T>, container: Container)
    fun uninstall(panel: TabbedPanel<T>, container: Container)

    fun selectionChanged(panel: TabbedPanel<T>, container: Container, new: T, newIndex: Int, old: T?, oldIndex: Int?)
}
