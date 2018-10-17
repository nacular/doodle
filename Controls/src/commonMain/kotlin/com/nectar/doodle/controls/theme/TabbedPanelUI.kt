package com.nectar.doodle.controls.theme

import com.nectar.doodle.controls.panels.TabbedPanel
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.theme.Renderer

/**
 * Created by Nicholas Eddy on 4/2/18.
 */

interface TabbedPanelUI: Renderer<TabbedPanel> {
    interface ItemUIGenerator {
        operator fun invoke(panel: TabbedPanel, tab: Gizmo, display: Gizmo?, index: Int, title: String?, selected: Boolean): Gizmo
    }

    interface ItemPositioner {
        val tabContainerSize: Double

        operator fun invoke(panel: TabbedPanel, tab: Gizmo, display: Gizmo?, index: Int, title: String?, selected: Boolean): Rectangle
    }

    val positioner : ItemPositioner
    val uiGenerator: ItemUIGenerator
}
