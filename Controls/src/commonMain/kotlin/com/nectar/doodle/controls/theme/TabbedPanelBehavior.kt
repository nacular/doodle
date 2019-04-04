package com.nectar.doodle.controls.theme

import com.nectar.doodle.controls.panels.TabbedPanel
import com.nectar.doodle.core.Container
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.theme.Behavior

/**
 * Created by Nicholas Eddy on 4/2/18.
 */

interface TabbedPanelBehavior<T> {
    /**
     * Invoked to render the given [TabbedPanel].
     *
     * @param panel  the Panel being rendered
     * @param canvas the Canvas given to the View during a system call to [View.render]
     */
    fun render(panel: TabbedPanel<T>, canvas: Canvas)

    /**
     * Returns true if the [TabbedPanel] contains point.  This can be used to handle cases when the [Behavior] wants to control hit detection.
     *
     * @param view
     * @param point
     */
    fun contains(panel: TabbedPanel<T>, point: Point): Boolean = point in panel.bounds

    fun install  (panel: TabbedPanel<T>, container: Container)
    fun uninstall(panel: TabbedPanel<T>, container: Container)

    fun selectionChanged(panel: TabbedPanel<T>, container: Container, new: T, newIndex: Int, old: T?, oldIndex: Int?)
}
