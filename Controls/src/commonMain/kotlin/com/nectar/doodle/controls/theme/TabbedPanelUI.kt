package com.nectar.doodle.controls.theme

import com.nectar.doodle.controls.panels.TabbedPanel
import com.nectar.doodle.core.View
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.theme.Renderer

/**
 * Created by Nicholas Eddy on 4/2/18.
 */

interface TabbedPanelUI: Renderer<TabbedPanel> {
    interface TabRenderer {
        operator fun invoke(panel: TabbedPanel, tab: View, display: View?, index: Int, title: String?, selected: Boolean): View
    }

    interface ItemPositioner {
        val viewPortArea: Rectangle

        operator fun invoke(panel: TabbedPanel, tab: View, display: View?, index: Int, title: String?, selected: Boolean): Rectangle
    }

    val positioner : ItemPositioner
    val tabRenderer: TabRenderer
}
